# LucaBridge on k3s — One-VPS Deployment & K8s Practice Checklist

Run the real LucaBridge stack on **one cheap VPS** using k3s (real, CNCF-conformant
Kubernetes), then spin up a **multi-node practice cluster** with k3d on the same box —
all for HK$0 beyond the VPS you already pay for.

This does **not** replace `docker-compose.yml`. Compose stays your fast local-dev path.
This is the "learn Kubernetes + deploy the same app" track.

**Stack being deployed:** Postgres 16 (StatefulSet) · MinIO · Spring Boot 3 backend ·
React Router v7 SSR frontend · Traefik ingress (replaces nginx).

---

## 0. Prerequisites

- [ ] A VPS with **≥ 4 GB RAM** (8 GB Contabo is ideal; 2 GB works but tight). Ubuntu 22.04/24.04.
- [ ] A domain name pointed (A record) at the VPS public IP — needed for ingress + SSL.
- [ ] SSH root/sudo access.
- [ ] GitHub account (images will live in GHCR — you already have CI wired for this).

---

## 1. Install k3s (single node) — 1 command

```bash
curl -sfL https://get.k3s.io | sh -
```

This installs k3s with **Traefik ingress**, **local-path storage**, and a **built-in
LoadBalancer** already running. Verify:

- [ ] `sudo k3s kubectl get nodes` → node shows `Ready`
- [ ] Copy kubeconfig so plain `kubectl` works:
  ```bash
  mkdir -p ~/.kube
  sudo cat /etc/rancher/k3s/k3s.yaml > ~/.kube/config
  sudo chown $USER ~/.kube/config
  kubectl get pods -A          # Traefik + coredns + local-path should be Running
  ```

> On a low-RAM box you can drop k3s's own monitoring later, but leave Traefik — the
> ingress depends on it.

---

## 2. Build & push images to GHCR

k3s pulls images from a registry (unlike compose's `build:`). Your GitHub Actions /
Jenkinsfile already build these — just make sure they push to GHCR with these names:

- `ghcr.io/YOUR_GH_USER/lucabridge-backend`
- `ghcr.io/YOUR_GH_USER/lucabridge-frontend`

Manual one-off (from the repo root):

```bash
echo $GHCR_PAT | docker login ghcr.io -u YOUR_GH_USER --password-stdin

docker build -t ghcr.io/YOUR_GH_USER/lucabridge-backend:latest ./backend
docker build -t ghcr.io/YOUR_GH_USER/lucabridge-frontend:latest ./frontend
docker push ghcr.io/YOUR_GH_USER/lucabridge-backend:latest
docker push ghcr.io/YOUR_GH_USER/lucabridge-frontend:latest
```

- [ ] Both images pushed.
- [ ] If the GHCR packages are **private**, create a pull secret so k3s can pull:
  ```bash
  kubectl create namespace lucabridge
  kubectl -n lucabridge create secret docker-registry ghcr-creds \
    --docker-server=ghcr.io \
    --docker-username=YOUR_GH_USER \
    --docker-password=$GHCR_PAT
  ```
  Then add to `30-backend.yaml` / `40-frontend.yaml` pod spec:
  ```yaml
      spec:
        imagePullSecrets:
          - name: ghcr-creds
  ```
  (Skip this if you make the packages public — simpler.)

---

## 3. Fill in the manifests

Edit these placeholders in `k8s/` before applying:

- [ ] `10-postgres.yaml` → `POSTGRES_PASSWORD`
- [ ] `20-minio.yaml` → `MINIO_ROOT_PASSWORD`
- [ ] `30-backend.yaml` → image path, `JWT_SECRET`, `APP_ADMIN_PASSWORD_HASH`, `CORS_ORIGINS`, `STORAGE_PUBLIC_BASE_URL`
- [ ] `40-frontend.yaml` → image path, `SITE_ORIGIN`
- [ ] `50-ingress.yaml` → `host:` (your domain, 3 places)

> Prefer not to commit real secrets. Either keep a local uncommitted copy, or create the
> secrets imperatively with `kubectl create secret generic ...` and delete the Secret
> blocks from the YAML.

---

## 4. Deploy — apply in order

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/10-postgres.yaml
kubectl apply -f k8s/20-minio.yaml
kubectl apply -f k8s/30-backend.yaml
kubectl apply -f k8s/40-frontend.yaml
kubectl apply -f k8s/50-ingress.yaml
```

(Or just `kubectl apply -f k8s/` — apply is order-independent; pods retry until deps are up.)

Watch it come alive:

- [ ] `kubectl -n lucabridge get pods -w` → all `Running` / `Ready`
- [ ] `kubectl -n lucabridge get pvc` → `postgres` + `minio-data` `Bound`
- [ ] `kubectl -n lucabridge logs deploy/backend` → Spring boots, Flyway migrations run
- [ ] Browse `http://YOUR_DOMAIN` → SSR site loads; `http://YOUR_DOMAIN/api/...` → API responds

---

## 5. SSL (automatic HTTPS)

Two options — pick one:

**A. cert-manager + Let's Encrypt (all in-cluster, the "real" way)**
```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/latest/download/cert-manager.yaml
```
- [ ] Create a `ClusterIssuer` (Let's Encrypt) — see cert-manager docs.
- [ ] Uncomment the `tls:` block in `50-ingress.yaml`, add the issuer annotation, re-apply.
- [ ] Cert issues automatically and **auto-renews** — same convenience as cPanel AutoSSL.

**B. Cloudflare in front (simplest)**
- [ ] Proxy the domain through Cloudflare (orange cloud), set SSL mode "Full".
- [ ] TLS terminates at Cloudflare; ingress stays plain HTTP. Zero cert management.

---

## 6. Day-2 operations (the marketable muscle memory)

```bash
kubectl -n lucabridge get pods                     # health at a glance
kubectl -n lucabridge scale deploy/frontend --replicas=3   # scale SSR
kubectl -n lucabridge rollout restart deploy/backend       # redeploy new image
kubectl -n lucabridge rollout undo deploy/backend          # rollback
kubectl -n lucabridge logs -f deploy/backend               # tail logs
kubectl -n lucabridge exec -it postgres-0 -- psql -U lucabridge   # DB shell
```

- [ ] Postgres backup (cron): `kubectl -n lucabridge exec postgres-0 -- pg_dump -U lucabridge lucabridge | gzip > backup.sql.gz`
- [ ] Add `metrics-server` to enable `kubectl top` + HPA autoscaling.
- [ ] Monitoring: your existing Prometheus/Grafana can scrape the backend's
      `/actuator/prometheus` — deploy them as pods or point an external Grafana at the
      ingress.

---

## 7. Multi-node practice cluster (k3d) — same VPS, HK$0

k3s on the VPS = **1 real node**. To practice the multi-node-only concepts (scheduling,
affinity, taints, drain), run a **virtual** multi-node cluster with k3d — each "node" is
a Docker container on the same box. Keep this separate from your production k3s.

```bash
# install docker + k3d
curl -fsSL https://get.docker.com | sh
curl -s https://raw.githubusercontent.com/k3d-io/k3d/main/install.sh | bash

# 1 server + 3 agents = a 4-node cluster
k3d cluster create practice --servers 1 --agents 3

kubectl config use-context k3d-practice
kubectl get nodes          # → 4 nodes, all Ready
```

Things you can now actually practice (impossible on a single node):

- [ ] **Scheduling**: deploy something with `replicas: 6`, watch pods spread across nodes.
- [ ] **Node affinity / anti-affinity**: pin or spread pods by node label.
- [ ] **Taints & tolerations**: taint a node, prove pods avoid it unless they tolerate.
- [ ] **Cordon / drain**: `kubectl cordon k3d-practice-agent-0` then `drain` — watch
      pods reschedule. This is the "rolling node maintenance" story interviewers ask about.
- [ ] **DaemonSet**: deploy one → confirm exactly one pod per node.
- [ ] **Load the LucaBridge images** into the cluster without a registry:
      `k3d image import ghcr.io/YOUR_GH_USER/lucabridge-backend:latest -c practice`

Tear down anytime: `k3d cluster delete practice` (your production k3s is untouched).

---

## CV framing

After this you can honestly write: *"Deployed a multi-service app (Spring Boot, Postgres
StatefulSet, SSR frontend, object storage) to a self-managed Kubernetes cluster (k3s);
authored Deployments, StatefulSets, Services, Ingress, ConfigMaps/Secrets, probes and
resource limits; practiced multi-node scheduling, affinity, taints, and node
drain/rollout operations."* — which maps directly onto the Docker + Kubernetes + CI/CD
requirements dominating HK DevOps/Cloud postings.

---

## Quick reference — file map

| File | What it creates |
|---|---|
| `k8s/00-namespace.yaml` | `lucabridge` namespace |
| `k8s/10-postgres.yaml` | Postgres Secret + headless Service + StatefulSet + PVC |
| `k8s/20-minio.yaml` | MinIO Secret + PVC + Service + Deployment |
| `k8s/30-backend.yaml` | Backend ConfigMap + Secret + Service + Deployment |
| `k8s/40-frontend.yaml` | Frontend ConfigMap + Service + Deployment (2 replicas) |
| `k8s/50-ingress.yaml` | Traefik Ingress: `/api` → backend, `/` → SSR frontend |
