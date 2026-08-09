# LucaBridge on Azure AKS - Setup Runbook

Provision an AKS cluster + Azure Container Registry, then deploy the existing `k8s/`
manifests to it. Written for the NGO-grant subscription ("Azure subscription 1",
tenant 樂橋有限公司). Region: **East Asia (eastasia)**.

Target config: AKS Free control-plane tier, 1x `Standard_B2ms` node with cluster
autoscaler 1->3, managed nginx ingress (app routing add-on), ACR attached for image pulls.

There are two paths to create the infra - **A (CLI, fast)** or **B (portal GUI)**. Do one.
Then everyone does **Part C** (deploy) on their Mac.

---

## Part A - Create infra with az CLI (recommended, ~1 command block)

Run in the portal Cloud Shell (the `>_` icon, Bash) or on your Mac after `az login`.

```bash
# --- variables ---
RG=rg-lucabridge
LOC=eastasia
AKS=aks-lucabridge
ACR=lucabridgeacr            # must be globally unique + lowercase alnum; add digits if taken

# 1. Resource group
az group create -n $RG -l $LOC

# 2. Container registry (Basic ~US$5/mo)
az acr create -n $ACR -g $RG --sku Basic

# 3. AKS cluster - Free tier, B2ms, autoscale 1-3, nginx ingress add-on, ACR attached
az aks create \
  -g $RG -n $AKS -l $LOC \
  --tier free \
  --node-vm-size Standard_B2ms \
  --node-count 1 \
  --enable-cluster-autoscaler --min-count 1 --max-count 3 \
  --enable-app-routing \
  --attach-acr $ACR \
  --generate-ssh-keys \
  --network-plugin azure

# 4. Pull kubeconfig into your local kubectl
az aks get-credentials -g $RG -n $AKS
kubectl get nodes        # node should show Ready
```

If `az acr create` fails on name-taken, change `ACR` to something unique (e.g. `lucabridgeacr01`) and rerun from step 2.

Skip to **Part C**.

---

## Part B - Create infra in the portal GUI (the "建立 Kubernetes 叢集" wizard)

### Tab 基本 (Basics)
- 訂用帳戶 (Subscription): **Azure subscription 1**
- 資源群組 (Resource group): click **新建** -> name `rg-lucabridge` -> 確定
- 叢集預設設定 (Cluster preset): **開發/測試 (Dev/Test)** - gives Free tier + single node
- Kubernetes 叢集名稱 (Cluster name): `aks-lucabridge`
- 區域 (Region): **東亞 (East Asia)**
- 可用性區域 (Availability zones): **無 (None)**
- AKS 定價層 (Pricing tier): **免費 (Free)**
- Kubernetes version: leave default

### Tab 節點集區 (Node pools)
- Click the default pool **agentpool** to edit it:
  - 節點大小 (Node size): change to **Standard_B2ms**
  - 縮放方法 (Scale method): **自動縮放 (Autoscale)**
  - 節點計數下限 (Min): **1**   |   節點計數上限 (Max): **3**
  - 更新 (Update)

### Tab 網路 (Networking)
- 網路設定: leave default (Azure CNI)
- Tick **啟用應用程式路由 (Enable application routing)** - this installs the managed nginx ingress controller

### Tab 整合 (Integrations)
- 容器登錄 (Container registry): click **新建** -> name `lucabridgeacr` (unique) -> SKU **Basic**
- 容器監視 / Azure 監視器: **optional**. You already run Prometheus/Grafana, so you can
  leave managed monitoring **off** to save credit, or turn it on for the learning.

### Finish
- **檢閱 + 建立 (Review + create)** -> wait for validation pass -> **建立 (Create)**.
  Provisioning takes ~5-10 min.
- After it finishes: on your Mac run `az aks get-credentials -g rg-lucabridge -n aks-lucabridge`

---

## Part C - Deploy LucaBridge to the cluster (on your Mac)

> The C2 edits below are ALREADY DONE in the new `k8s-aks/` folder (k3s originals kept in `k8s/`).
> So you only need C1 (build+push images), C3 (set real secrets), then C4 with `-f k8s-aks/`.

Your `k8s/` manifests were written for **k3s + Traefik + local-path storage**. Four edits
make them AKS-native. (`<ACR>` = your registry login server, e.g. `lucabridgeacr.azurecr.io`.)

### C1. Build + push images to ACR
```bash
ACR_NAME=lucabridgeacr
az acr login -n $ACR_NAME
LOGIN=$(az acr show -n $ACR_NAME --query loginServer -o tsv)

docker build -t $LOGIN/lucabridge-backend:latest ./backend
docker build -t $LOGIN/lucabridge-frontend:latest ./frontend
docker push $LOGIN/lucabridge-backend:latest
docker push $LOGIN/lucabridge-frontend:latest
```
> On Apple Silicon add `--platform linux/amd64` to each `docker build` so images run on the AKS x86 nodes.

### C2. Four manifest edits
1. **Image paths** - `k8s/30-backend.yaml` + `k8s/40-frontend.yaml`:
   change `ghcr.io/YOUR_GH_USER/lucabridge-*:latest` -> `<ACR>/lucabridge-*:latest`.
   (No `imagePullSecrets` needed - `--attach-acr` grants pull via managed identity.)
2. **Storage class** - `k8s/10-postgres.yaml` + `k8s/20-minio.yaml`:
   change `storageClassName: local-path` -> `storageClassName: managed-csi`.
3. **Ingress class** - `k8s/50-ingress.yaml`: add `ingressClassName: webapprouting.kubernetes.azure.com`
   under `spec:` (the app routing add-on's class). Remove any Traefik-specific annotations.
4. **Ingress host** - `k8s/50-ingress.yaml`: set `host:` to your real domain (3 places), or
   leave it and reach the site by the ingress public IP for now.

### C3. Set the real secrets (don't commit them)
Fill `POSTGRES_PASSWORD`, `MINIO_ROOT_PASSWORD`, `JWT_SECRET`, `APP_ADMIN_PASSWORD_HASH`,
`CORS_ORIGINS`, `STORAGE_PUBLIC_BASE_URL`, `SITE_ORIGIN` in the Secret/ConfigMap blocks
(same placeholders the k3s checklist lists in docs/k3s-deployment-checklist.md section 3).

### C4. Apply + watch
```bash
kubectl apply -f k8s-aks/
kubectl -n lucabridge get pods -w          # wait for all Running/Ready
kubectl -n lucabridge get pvc              # postgres + minio-data -> Bound
kubectl get ingress -n lucabridge          # note the ADDRESS (public IP)
```
Point your domain's A record at that ingress IP. For HTTPS, either cert-manager +
Let's Encrypt, or put Cloudflare in front (SSL mode Full) - same options as the k3s doc.

### Day-2 (same muscle memory as k3s)
```bash
kubectl -n lucabridge scale deploy/frontend --replicas=3
kubectl -n lucabridge rollout restart deploy/backend
kubectl -n lucabridge rollout undo deploy/backend
kubectl -n lucabridge logs -f deploy/backend
```

---

## Cost snapshot (against the ~US$2,000/yr NGO grant)
| Item | Approx |
|---|---|
| AKS control plane (Free tier) | US$0 |
| 1x Standard_B2ms node (idle) | ~US$60/mo |
| Extra nodes only when autoscaler adds them | ~US$60/mo each, transient |
| ACR Basic | ~US$5/mo |
| Load balancer + public IP | ~US$3/mo |
| Managed disks (PVCs) | ~US$5/mo |
| **Idle total** | **~US$75/mo** -> well inside the grant |

Stop billing when not learning: `az aks stop -g rg-lucabridge -n aks-lucabridge`
(restart with `az aks start ...`). Stopped clusters bill only disks.
