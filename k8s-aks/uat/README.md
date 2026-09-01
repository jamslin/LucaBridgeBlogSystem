# UAT environment (AKS)

A second, isolated copy of LucaBridge on the **same** AKS cluster, in its own namespace
`lucabridge-uat`. Used to view the **demo/seed content** while prod stays untouched.

- **URL:** http://uat.20.24.249.212.nip.io
- **Isolation:** own Postgres + MinIO + secrets. Prod (`lucabridge` namespace) can't be
  reached or affected from here. Guardrails: `ResourceQuota` (1 CPU / 2Gi requests) +
  `LimitRange` so UAT can't starve prod.
- **Routing:** `nip.io` maps `uat.20.24.249.212.nip.io` → the shared ingress IP
  `20.24.249.212`; nginx splits by Host header. Prod stays host-less on the bare IP.
  Swap in a real domain later by changing the `host:` in `uat.yaml` (3 configs) + an A record.
- **Seeding:** backend runs `SPRING_PROFILES_ACTIVE=dev` → Hibernate `create-drop` + `data.sql`,
  so UAT **self-seeds on boot and RESETS on every restart**. That's intended for a demo box.
  Want it to persist? Add a dedicated `uat` Spring profile (Flyway-managed) instead of `dev`.

## Automated deployment

Pushes to `main` are deployed by `.github/workflows/deploy-uat.yml`. The workflow builds
immutable backend and frontend images tagged with the Git commit SHA, pushes them to ACR,
applies `uat.yaml`, waits for both deployments, and checks the UAT home page.

The workflow uses Azure OIDC. Configure the GitHub `uat` environment with
`AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, and `AZURE_SUBSCRIPTION_ID` before enabling it.

## Manual deployment

Secrets are created imperatively (kept out of git). From Cloud Shell or a machine with
`kubectl` pointed at the cluster:

```bash
# 1. namespace (or let uat.yaml create it — it includes the Namespace object)
kubectl create namespace lucabridge-uat

# 2. secrets (generate fresh values)
UPG=$(openssl rand -hex 16); UMIN=$(openssl rand -hex 16); UJWT=$(openssl rand -hex 48)
kubectl -n lucabridge-uat create secret generic postgres-secret --from-literal=POSTGRES_PASSWORD="$UPG"
kubectl -n lucabridge-uat create secret generic minio-secret \
  --from-literal=MINIO_ROOT_USER=minioadmin --from-literal=MINIO_ROOT_PASSWORD="$UMIN"
kubectl -n lucabridge-uat create secret generic backend-secret \
  --from-literal=JWT_SECRET="$UJWT" --from-literal=APP_ADMIN_PASSWORD_HASH=""

# 3. create the UAT-specific seed and apply the stack
sed 's#http://localhost:9000#http://uat.20.24.249.212.nip.io#g' \
  ../../backend/src/main/resources/data.sql > /tmp/uat-data.sql
kubectl -n lucabridge-uat create configmap uat-seed \
  --from-file=data.sql=/tmp/uat-data.sql --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -f uat.yaml
kubectl -n lucabridge-uat get pods -w
```

## Notes / known follow-ups

- **Media:** `/blog-media` is routed to UAT MinIO and the demo bucket must allow anonymous
  downloads. For the `lucabridge` namespace this is now automatic — the backend Deployment
  has an `ensure-media-bucket` initContainer that creates the bucket and sets the download
  policy on every rollout. This UAT manifest is standalone and does NOT have it, so here you
  still run `mc anonymous set download m/blog-media` after rebuilding MinIO storage.
  Download each source image to a file before `mc cp`; avoid `mc pipe`, which can exceed
  UAT's memory limit. Do not use public-read buckets for private uploads.
- **NetworkPolicy not applied** — the cluster has no network-policy engine; enabling it
  (`az aks update --network-policy ...`) restarts prod nodes, so it's deferred. Isolation
  still holds via separate namespace + separate secrets.
- **Cost:** ~$0–1.5/mo (fits on the existing node; +~$60/mo only if the autoscaler adds a node).
  Park it when idle: `kubectl -n lucabridge-uat scale deploy --all --replicas=0`.
