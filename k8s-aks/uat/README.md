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

## Deploy

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

# 3. apply the stack
kubectl apply -f uat.yaml
kubectl -n lucabridge-uat get pods -w
```

## Notes / known follow-ups

- **Images won't load** — MinIO isn't publicly exposed (same as prod). Text content only.
- **NetworkPolicy not applied** — the cluster has no network-policy engine; enabling it
  (`az aks update --network-policy ...`) restarts prod nodes, so it's deferred. Isolation
  still holds via separate namespace + separate secrets.
- **Cost:** ~$0–1.5/mo (fits on the existing node; +~$60/mo only if the autoscaler adds a node).
  Park it when idle: `kubectl -n lucabridge-uat scale deploy --all --replicas=0`.
