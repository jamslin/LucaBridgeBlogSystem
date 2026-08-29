# LucaBridge Azure 雲端成本檢討

**日期：** 2026 年 8 月 20 日
**環境：** Azure AKS（rg-lucabridge，East Asia）
**資助額：** US$2,000（NGO grant，按年計）

---

## 一、摘要

以目前用量推算，US$2,000 資助會喺**大約第 10 個月**用完，唔夠一年。

問題唔係「用得多」，而係**系統自動開多咗兩部虛擬機**，而我哋一直冇為此設任何提示或上限。

已確認原因，修正方案清晰，唔需要改架構、唔影響網站運作。修正後每月成本由約 US$190 降到約 **US$83**，一年約 US$1,000，即係資助夠用超過兩年。

---

## 二、現況數字

| 項目 | 金額 |
|---|---|
| 資助總額 | US$2,000.00 |
| 已使用（截至 8 月 20 日） | US$100.30 |
| 餘額 | US$1,899.70 |
| 8 月預測總額 | US$189.66 |
| **按此速度年化** | **約 US$2,276** |

超出資助額約 14%。

---

## 三、錢用咗喺邊

8 月至今 US$98.07（AKS 資源群組）分佈如下：

| 項目 | 金額 | 佔比 |
|---|---|---|
| 虛擬機（約 3 部） | $74.81 | 76% |
| 虛擬機系統磁碟（4 隻） | $18.67 | 19% |
| 公共 IP（2 個） | $3.20 | 3% |
| 資料儲存 PVC（4 隻） | $1.28 | 1% |
| 負載平衡器 | $0.09 | <1% |

**結論：95% 嘅開支嚟自虛擬機同佢哋嘅磁碟。** 其餘項目全部係散銀，唔值得處理。

---

## 四、問題根源

系統原本設計係跑 **1 部**虛擬機，但實際跑緊 **3 部**。

計算依據：虛擬機 14 日收咗 $74.81，即每日 $5.34。一部機每日約 $2.00，所以平均開緊接近 3 部。磁碟帳單亦印證：兩隻由 8 月 6 日開始，另外兩隻約 8 月 14 日先出現。

**點解會自動加機：**

8 月 7 日加咗 UAT 測試環境之後，測試環境同正式環境嘅資源需求加埋超出咗一部機嘅容量。Kubernetes 見到有程式排唔到位，就按設定自動加機，一路加到上限 3 部。

呢個係設定問題，唔係故障。系統只係照住我哋俾佢嘅指示做。真正嘅疏忽係：**我哋冇設預算提示**，所以冇人知道佢加咗機。

---

## 五、建議

按次序執行，唔可以跳步。

**1. 收細每個程式嘅資源預留量**
測試環境嘅程式預留咗過多資源。調低之後，正式同測試環境可以共用一部機。

**2. 正式環境前端由 2 個副本減到 1 個**
喺呢個規模嘅機器上，兩個副本冇實際效益。

**3. 之後先鎖死自動加機上限做 2 部**
必須做完第 1、2 步先鎖，否則程式會卡住起唔到。

**4. 清走冇再用嘅磁碟**
可能有兩隻係舊節點遺留低，未刪除但仍然收費。

**5. 即刻設立預算提示（US$100/月，80% 同 100% 發電郵）**
呢一步最重要。技術修正解決今次問題，預算提示防止下次。

---

## 六、修正後預期

| 項目 | 每月 |
|---|---|
| 虛擬機（1 部） | ~$60 |
| 系統磁碟 | ~$15 |
| 公共 IP | ~$3 |
| 容器登錄（ACR） | ~$5 |
| **合計** | **~$83** |

年化約 **US$1,000**，用咗資助一半，仲有空間加 domain、TLS 憑證同日後流量增長。

**風險：** 低。以上全部係設定調整，唔涉及改程式碼或架構，可隨時回復。網站服務不會中斷。

---
---

# 技術附錄

## 診斷數據

**成本分析（Cost Management → Resources，範圍 `mc_rg-lucabridge_aks-lucabridge_eastasia`，Aug 2026）**

```
aks-nodepool1-1337845...  VMSS      $74.81
aks-nodepool1-133784a...  Disk       $6.78   ← 8/6 起
aks-nodepool1-133784a...  Disk       $6.77   ← 8/6 起
aks-nodepool1-133784a...  Disk       $2.56   ← 約 8/14 起
aks-nodepool1-133784a...  Disk       $2.56   ← 約 8/14 起
48aad879-...              Public IP  $1.60
kubernetes-ad1eaf94...    Public IP  $1.60
pvc-2800c351 / 5a61d9f7 / 6f3db2a0 / dc7faabc  Disk  $1.28 合計
kubernetes                LB         $0.09
```

VMSS $74.81 ÷ 14 日 = $5.34/日。Standard_B2s_v2 約 $0.083/hr ≈ $2.00/日。
→ 平均節點數 ≈ 2.7，即 autoscaler 長期貼住 max-count 3。

四隻 nodepool OS disk 對應曾經存在過嘅節點；其中兩隻計費時間較短，符合 8 月中擴容嘅時間點。

## 容量分析

`Standard_B2s_v2` = 2 vCPU / 8 GiB。

- Allocatable ≈ 1.9 vCPU
- kube-system + app-routing nginx ingress ≈ 0.6–0.7 vCPU
- **實際可派俾應用 ≈ 1.2 vCPU**

`lucabridge-uat` namespace 嘅 ResourceQuota 設咗 `requests.cpu: 1`。正式環境（postgres、minio、backend、frontend ×2）本身已經接近食盡一部機嘅可用額度，UAT 嘅 pod 無位可排 → cluster autoscaler 觸發擴容。

ResourceQuota 本意係防止 UAT 搶走 prod 資源，但因為冇同時收細個別 pod 嘅 `requests`，反而變成強制擴容嘅推手。

## 驗證指令

```bash
kubectl get nodes
kubectl describe nodes | grep -A8 "Allocated resources"
kubectl get pods -A -o custom-columns=\
NS:.metadata.namespace,NAME:.metadata.name,CPU:.spec.containers[*].resources.requests.cpu
kubectl get pods -A --field-selector status.phase=Pending
```

## 執行步驟

**Step 1 — 收細 resource requests**

UAT（`lucabridge-uat`）全部 pod：

```yaml
resources:
  requests:
    cpu: 50m
    memory: 256Mi
  limits:
    memory: 512Mi
```

Prod（`lucabridge`）：

| 元件 | requests.cpu | requests.memory |
|---|---|---|
| postgres | 100m | 512Mi |
| minio | 100m | 256Mi |
| backend | 200m | 768Mi |
| frontend | 100m | 256Mi |

合計 prod ≈ 0.5 vCPU、UAT ≈ 0.25 vCPU，一部機容納有餘。

**Step 2 — 前端縮到單副本**

```bash
kubectl scale deployment/frontend --replicas=1 -n lucabridge
```

**Step 3 — 鎖死 autoscaler（確認上兩步生效後先做）**

```bash
az aks nodepool update -g rg-lucabridge --cluster-name aks-lucabridge \
  -n nodepool1 --update-cluster-autoscaler --min-count 1 --max-count 2
```

保留 max 2 而唔係 1，係留一個真係有需要時嘅緩衝。

**Step 4 — 清理孤兒磁碟**

```bash
az disk list -g mc_rg-lucabridge_aks-lucabridge_eastasia \
  --query "[?diskState=='Unattached'].{name:name,gb:diskSizeGb,sku:sku.name}" -o table
```

確認之後先刪。

**Step 5 — 設立 Budget**

Portal → Cost Management + Billing → Budgets → Add
範圍：subscription `f8a95881-6fb5-4679-abdf-923e506239cc`
金額：US$100 / 月，警示 80% + 100%，收件人：管理員電郵

## 進階選項（暫不建議即做）

**UAT 定時關機。** UAT 用 `SPRING_PROFILES_ACTIVE=dev`（create-drop + data.sql），每次啟動自動重建資料，關機零損失。可加 CronJob 喺平日 20:00 HKT 縮到 0、09:00 復原。做完 Step 1 之後未必需要，但係一個安全嘅額外緩衝。

**節點系統磁碟改用 Standard SSD。** 現時每部機掛住 128 GB Premium SSD（約 $19.7/月）。改用 30 GB Standard SSD 約 $2.4/月。但 OS disk 類型建立後不可改，要開新 nodepool、cordon 舊嘅再刪，工序多而慳得約 $17/月。可以留待日後有需要時做。

---

## 附註：架構取捨

原定部署目標係單機 Docker Compose（租用伺服器）。改用 AKS 係為咗實踐 k8s 同 CI/CD，屬有意識嘅選擇。

成本上，AKS 大約係單機方案嘅 3 至 4 倍。修正後每月 $83 仍然喺資助範圍內，所以維持現狀合理。但如果日後學習目標已達成、而成本再有壓力，將正式環境搬返單機 Docker Compose、只喺練習時開 AKS，係一個可行嘅退路。
