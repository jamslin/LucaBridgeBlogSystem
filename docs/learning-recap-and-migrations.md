# 樂橋 LucaBridge — 學習筆記：我哋做咗乜、點解、同業界點做

> 一份由頭到尾嘅回顧 + Flyway/Hibernate 深入 + 潮流對照。寫畀你日後翻睇。

---

## 0. 一句總結

我哋由一個 Wix 網站，重建成一個「自建後端 + SSR 前端 + 容器化部署」嘅三語慈善網站。技術上偏向**企業級、穩陣、可學習**，而唔係「最快出街」。呢個取向唔算最潮，但係扎實；潮流嗰邊會用更多「託管服務（CMS / BaaS）」嚟慳返自建後端嘅工。以下逐層拆解。

---

## 1. 由頭到尾：我哋做過乜，點解咁做

### 1.1 起點
原本個網站係 Wix（`My Site 1`），內容係繁中為主。目標：重建成一個正式、可自己控制、三語（繁中／EN／简中）、有捐款路徑嘅慈善網站。順帶當成一個**學習 project**（練 Spring Boot、CI/CD、監控）。

### 1.2 技術棧點揀，點解

| 選擇 | 點解 |
|---|---|
| **React Router v7（SSR）** | 唔用純 SPA 或者 SSG，係因為要 **SEO** 同 **WhatsApp/FB 分享預覽**（呢啲爬蟲唔行 JS），而且內容要「即時由 DB 出」。SSR 一次滿足晒。 |
| **Spring Boot 3（Java 21）** | 業界主流後端，穩定、生態成熟；亦係你想練嘅嘢。對呢個規模嚟講偏重型 - 係故意嘅。 |
| **PostgreSQL 16** | 目前最穩、最通用嘅關聯式資料庫，幾乎係預設選擇。 |
| **MinIO** | S3 相容嘅物件儲存，放相片；日後可以無痛轉去 AWS S3。 |
| **Docker Compose（單機）** | 單一部機跑晒所有 container，簡單、平、易維護；避開 k8s 呢類 overkill。 |
| **nginx** | 反向代理 + SSR 微快取（micro-cache）。 |
| **Jenkins CD + GitHub Actions** | Actions 做 PR gate，Jenkins 做部署 - 亦係練 CI/CD。 |
| **Prometheus + Grafana** | 監控，練 observability。 |

### 1.3 資料模型：translation-per-row
每個內容（post / category / page / event）嘅每種語言，係**一行 translation**（例如 `post_translation` 一種語言一行），而唔係一張表擺三個語言欄。好處：加語言唔使改 schema、缺翻譯可以 fallback 去繁中。

### 1.4 設計系統
定咗方向 **1B Bridge Green**（葉綠 `#3F6B4D`），用 **design tokens**（`tokens.js` + `global.css` 嘅 CSS 變數）做單一真相來源 - 改一個色，全站跟。跟住做 component polish（三種按鈕、sage 藥丸 filter、分段語言切換、赭金 pull-quote）。

### 1.5 內容匯入 + 擴充模型
由 Wix 抓返所有內容，發現個站唔淨係 blog：仲有靜態頁（關於／捐款／私隱／條款）、活動、真 blog post。所以我哋**擴充咗資料模型**，加 `page` / `event` / `site_setting` 三種表，先至裝得落啲真內容。

### 1.6 一路撞到嘅 bug（同點解）
- **`/api/categories` 500（LazyInitializationException）**：service 喺 transaction 外面先至讀 lazy 嘅 `translations`。加 `@Transactional(readOnly=true)` 解決。
- **首頁出 nginx 預設頁**：你部機 port 80 畀另一個程式（loopback 上）霸咗，蓋過咗我哋 nginx。改用 `:3000` 直連前端繞過。
- **前端／nginx 冇起身**：`up --build backend` 淨係起 backend + 依賴，冇起 frontend/nginx。要 `up -d`（唔指名 service）。
- **seed 失敗 `created_at` NULL**：轉咗 Hibernate 之後，entity 嘅 Java 預設值唔會變成 DB 預設值，raw SQL seed 就撞 NOT NULL。加 `columnDefinition` 補返 DB 預設值。

### 1.7 DB 策略大轉向：Flyway → Hibernate（因為仲喺 prototype）
你嘅 schema 仲會經常改，寫一大堆 migration script 太煩。所以 dev 熄咗 Flyway，改用 **Hibernate `create-drop`**（每次開機由 entity 重起 schema）+ 一個 `data.sql` seed。Prod 保留 Flyway。呢個係今次筆記嘅重點，第 2 節詳講。

---

## 2. Flyway vs Hibernate：由零講起

### 2.1 三個詞
- **Schema（結構）**：空表、欄位、約束、index。
- **Migration（遷移）**：有版本、有次序、只行一次嘅改動 script（改結構 = DDL）。
- **Seed（種子）**：其實都係一個 migration/script，只不過佢係「入資料」（DML），唔係改結構。

### 2.2 Flyway 係咩、點運作
Flyway 係一個「資料庫版本控制」工具。你寫一堆有編號嘅 SQL（`V1__`, `V2__`…），Flyway：
1. 睇個 DB 嘅記帳表 `flyway_schema_history`，知道邊啲行過。
2. 按編號次序，將未行嘅逐個 apply，每個只行一次。
3. 記低每個嘅 **checksum**（內容指紋）。

**好處**：明確、可審計、可重現、跨環境一致、支援 review。
**代價**：schema 每次改都要手寫一個新 script；prototype 期會累積好多。

### 2.3 Hibernate `ddl-auto` 係咩、五個 mode
Hibernate（JPA 實作）可以**由你啲 `@Entity` class 反過嚟生成 schema**。`spring.jpa.hibernate.ddl-auto` 有五個值：

| 值 | 行為 | 用喺邊 |
|---|---|---|
| `none` | 咩都唔做 | 由 Flyway/Liquibase 管 schema 時 |
| `validate` | 只檢查 entity 同 DB schema 夾唔夾，唔改 | **Prod 最佳**（配 Flyway） |
| `update` | 增量改（加表/欄），唔 drop、唔改型 | 開發方便，但會 drift，唔建議 prod |
| `create` | 開機時 drop + 重建 | 測試 |
| `create-drop` | 開機建、關機 drop | **本地 prototype**（我哋而家用緊） |

**好處**：零 migration script，schema 永遠跟 entity。
**代價**：`update` 唔識 drop/rename/改型；生成嘅 DDL 唔一定有你要嘅 index、DB 預設值、`ON DELETE CASCADE`（正正係我哋撞到 `created_at` 個 bug 嘅原因）。

### 2.4 核心矛盾：邊個係 schema 嘅「真相來源」？
成件事嘅本質係一條問題：**schema 由邊度定義？**
- **Migration 派（Flyway/Liquibase）**：SQL script 係真相，entity 要跟住寫，prod 用 `validate` 確保夾。
- **ORM 派（Hibernate 生成 / Django / Rails / Prisma）**：程式碼（entity/model）係真相，schema 由佢生成。

兩派都有大量真實團隊用。**唔可以兩個同時做真相** - 否則就 drift。我哋而家 dev 用 ORM 派（快），prod 保留 migration 派（穩）。

### 2.5 我哋撞到嗰個 bug 嘅本質
`created_at NOT NULL` 但 seed 冇畀值。因為：
- Flyway schema 寫住 `DEFAULT now()`（**DB 層**預設）→ raw insert 唔畀值都得。
- Hibernate 由 entity 生成時，`@Column(nullable=false)` + Java field `= Instant.now()` 只係 **Java 層**預設，**唔會**變成 DB 層 `DEFAULT` → raw SQL seed 繞過 Java，就 NULL。

**業界點睇**：呢個係「用 raw SQL seed + ORM 生成 schema」嘅經典陷阱。三種正路解法：
1. **加 DB 預設**：`@Column(columnDefinition="timestamptz not null default now()")`（我哋用呢個）。
2. **用 Hibernate 生命週期**：`@CreationTimestamp`／`@UpdateTimestamp`（但只對「經 entity 儲存」有效，對 raw seed 冇用）。
3. **seed 時明確畀值**：INSERT 時自己寫 `now()`。

### 2.6 業界點樣兩者並用（最常見嘅「混合」做法）
1. **本地/prototype**：`ddl-auto: update` 或 `create-drop`，快速試 schema，唔寫 migration。
2. **Schema 穩定後**：叫 Hibernate 出一份 DDL，凍結成 **baseline `V1`**（`flyway baseline`）。
3. **之後所有改動**：只用 Flyway 新增 migration（forward-only）。
4. **所有環境**：`ddl-auto: validate` - Hibernate 只驗證唔改，schema 由 Flyway 話事。
5. **CI/CD**：部署時自動跑 migration；prod 用 `validate` 兜底。

---

## 3. 我嘅做法夠唔夠潮？

**老實講：扎實，但偏企業重型；唔算追潮流，亦唔算落後。** 逐項睇：

| 範疇 | 你嘅選擇 | 潮流位置 |
|---|---|---|
| 前端 | React Router v7 SSR | ✅ 主流當代（同 Next.js / Remix 同一世代） |
| 後端 | Spring Boot（Java） | 🟡 業界標準、Asia/enterprise 好流行，但「潮」嗰邊會偏 Node/Go/Bun |
| DB | PostgreSQL | ✅ 幾乎係當代預設 |
| 儲存 | MinIO / S3 | ✅ 標準 |
| 部署 | Docker Compose 單機 | 🟡 務實，細 project 岩；潮流會講 k8s / serverless / PaaS（對你嚟講 overkill） |
| Migration | Flyway | ✅ Java 界標準（另一大係 Liquibase） |
| 監控 | Prometheus + Grafana | ✅ 標準 |

**判斷**：你冇追 hype（好事），揀嘅全部係「10 年後都仲喺度」嘅穩陣技術。對一個**學習 project** 嚟講，呢個組合啱到唔得 - 你會學到真正嘅後端/DevOps 功。唯一「唔潮」嘅位，係對一個細型內容網站嚟講，自建 Java 後端係重型；但你係**明知而為**（為學習），所以冇問題。

---

## 4. 同樣嘅 project／系統，人哋會點做？

### 4.1 如果純粹想搞掂呢個 charity blog（唔為學習）
大部分人／freelancer 會**唔自建後端**，改用：
- **Headless CMS**（Strapi / Sanity / Payload / Contentful）+ Next.js：客戶自己改內容，免寫 admin。
- **BaaS**（Supabase / Firebase）+ Next.js：Supabase = Postgres + 自帶 auth + 自帶 migration 工具，免自建 Java。
- 甚至**留喺 Wix / Squarespace**：如果冇 dev 需求。
重點：內容網站嘅「內容」，業界傾向交畀 CMS，唔會焗入 code。

### 4.2 如果係同你類似嘅「自建 Spring Boot + Postgres 內容平台」
專業團隊嘅標準做法：
- Schema：**Flyway/Liquibase migration** 做真相，`ddl-auto: validate` 喺**所有**環境（prod 永遠唔用 create/update）。
- Seed vs 內容：**參考資料**（categories 呢類）用 migration seed；**可編輯內容**（文章/頁面）用 **admin/CMS 喺 runtime 入**，唔會用 migration。
- 部署：CI 自動跑 migration；blue-green / rolling 部署；migration 要「向後兼容」先可以零停機。
- 本地：可以用 Testcontainers 起臨時 Postgres 跑 migration 測試。

**你同「標準」嘅唯一分別**：你 prototype 期暫時用 Hibernate 生成 schema 慳工 - 呢個喺早期係合理嘅務實選擇，只要記得**穩定後切返去 Flyway `validate`**。

---

## 5. 由 top 學 Flyway & Hibernate：路線圖 + 黃金法則

### 5.1 心法（一句記住）
> **Migration 描述資料庫嘅「形狀」，同 code 一齊行；ORM 幫你由 code 對映到資料。Schema 只可以有一個真相來源。**

### 5.2 黃金法則
1. **Forward-only**：一個 migration apply 過就當凍結，永遠唔改／唔刪；要改就加新 migration。
2. **唔好手改 prod DB**：任何改動都要有 migration 記錄，否則 drift + 無法重現。
3. **一個 schema owner**：Flyway 派 → 全環境 `validate`；ORM 派 → 唔好又寫 Flyway。唔好兩頭做。
4. **Seed ≠ 內容**：參考資料入 migration；可編輯內容交 runtime（app/admin）。
5. **Prod 永遠 `validate`**，永遠唔好 `create`/`update`。
6. **每個 migration 細、單一目的、可 review**。

### 5.3 頂尖團隊點處理「我哋嗰個」問題（schema 真相來源 + dev快/prod穩）
- 早期用 ORM 生成或 `update` 快速迭代 → 穩定後 `flyway baseline` 凍結 → 之後純 Flyway。
- DB 層面嘅嘢（預設值、index、約束）**明確寫喺 migration**，唔靠 ORM 生成，因為 ORM 生成嘅未必齊（正如我哋撞到）。
- 用 CI 跑「由零 migrate 一次」+ 「由上一版 migrate 上嚟」兩條路，確保新舊環境都 work。

### 5.4 學習資源／關鍵字（自己再深挖）
- Flyway 官方文檔：`baseline`, `repair`, `validate`, `outOfOrder`, callbacks。
- Liquibase（Flyway 嘅主要對手，用 XML/YAML changelog，功能更多）。
- Hibernate：`hbm2ddl`（即 ddl-auto 背後）、`@CreationTimestamp`、schema validation。
- 概念：**Evolutionary Database Design**（Martin Fowler 嗰篇經典）、**expand-and-contract / parallel change**（零停機改 schema 嘅手法）。
- 對照其他生態：Django migrations、Rails Active Record migrations、Prisma Migrate、TypeORM、Alembic（Python）- 睇吓「ORM 派」點處理同一問題。

---

*筆記完。想我就任何一節（例如 expand-and-contract 零停機遷移、或者 headless CMS 對比）再深入，話我知。*

---

# 附錄（2026-07-10 補充）

## 6. Headless CMS 詳解

傳統 CMS（如 WordPress）= **身 + 頭** 綁埋：身 = 內容 DB + 後台編輯；頭 = 呈現嘅前端主題。

**Headless CMS = 保留個身，斬走個頭**：淨係管理內容 + 透過 API（REST/GraphQL）吐內容，冇自帶前端。個「頭」由你自己接（Next.js 網站／手機 app／任何客戶端）。

- 「Headless」= 冇咗個「呈現嘅頭」。
- 好處：一份內容餵多個前端；用你鍾意嘅現代前端框架。
- 例子：Strapi、Sanity、Contentful、Payload。
- 對照：你自建緊嘅 Spring Boot 後端 + 未來 admin UI，本質上就係自己整一個 headless CMS。

## 7. 用 Flyway，dev 期點解唔會累積一堆垃圾 migration

**關鍵誤解**：「唔可以改／刪 migration」呢條規，**只喺個 migration 已經 share / release 之後先生效**。本地一個人試緊、未 push、未上過 shared 環境，可以隨便改／刪／重寫。

避免堆積嘅標準手法：
- **Release 前 squash**：prototype 期啲亂 migration，出 prod 前摺埋成一個乾淨 baseline `V1`。
- **改返最後一個未 release 嘅 migration**：淨係本地 DB 就 drop 咗再跑，唔使加「改返轉頭」script。
- **prototype 期唔逐個寫 migration**：本地用 `ddl-auto: update`／`create-drop`，穩定咗先寫正式 migration（＝我哋嘅做法）。

結論：跟手嘅人睇到嘅係**修剪過、有目的**嘅歷史，唔係 dev 期原始亂噏。

## 8. Migration → ORM 生成 schema 嘅經典陷阱

1. **Java 預設值 ≠ DB 預設值**（我哋撞嗰個 `created_at NULL`）。
2. **唔生成自訂 index** → 大資料查詢變慢。
3. **`ON DELETE CASCADE` 冇咗** → raw SQL 刪除行為唔同。
4. **型別對映意外**：`varchar(255)` 預設、enum ordinal vs string、`timestamp` vs `timestamptz`、`BigDecimal` precision。
5. **`update` 永遠唔 drop／唔改型** → 舊欄靜靜留低，schema drift。
6. **命名策略 camelCase ↔ snake_case** → 引號 identifier / 搵唔到欄。
7. **prod 用 `create`/`update`** → 開機可能 drop 重建表 = 資料災難。

## 9. 出名案例／來源

- **Vlad Mihalcea**（Hibernate 專家）：金句 = prod 永遠唔好 `ddl-auto=update`，用 Flyway/Liquibase + `validate`。
- **`gh-ost`**（GitHub 開源，MySQL 線上改 schema）：因大規模改表會鎖表停服務，先要專門工具。
- **`pt-online-schema-change`**（Percona）：同類老牌工具。
- **GitLab database migration style guide**（公開文檔）：零停機遷移實戰。
- **Django / Rails / Prisma Migrate**：ORM 派點做 migration，值得對照。
- **Martin Fowler：Evolutionary Database Design**：範疇經典文。

## 10. Expand-and-Contract：零停機改 schema（進階）

### 問題
真實 prod 做 rolling / blue-green 部署時，**舊 code 同新 code 會有一段時間同時行**，對住**同一個 DB**。如果你一次過改 schema（例如 rename 或 drop 欄），仲喺度行緊嘅舊 code 就即刻爆。你冇辦法「改 schema」同「換 code」原子性咁一齊做。

### 解法：將一個「破壞性改動」拆成幾個「向後兼容」嘅細步，跨幾次部署完成。三個階段：

1. **Expand（擴張）**：只**加**新嘢（新欄／新表），唔郁舊嘢。新舊並存。舊 code 唔知有新欄，照行。部署。
2. **Transition（過渡）**：backfill 舊資料入新欄；code 改成**同時寫新舊**（dual-write）、**讀新**。部署。
3. **Contract（收縮）**：確定冇 code 再讀寫舊嘢之後，先喺**之後一個** migration drop 舊嘢。部署。

### 例子：`full_name` 改名做 `display_name`
1. **Expand**：加 `display_name`（nullable）。部署 - 舊 code 仍用 `full_name`，不受影響。
2. **Transition**：`UPDATE ... SET display_name = full_name`；code 寫兩欄、讀 `display_name`。部署。
3. **Contract**：有信心後，drop `full_name`。部署。

每一步，當時行緊嘅新舊 code 都 work，永遠冇一刻「行緊嘅 code 要一個唔存在嘅欄」。

### 黃金規則
- 每個 migration 要**同「當時已部署嘅 code」向後兼容**。
- **加**（欄／表／index）係安全；**drop / rename / 收窄型別 / 加 NOT NULL 冇 default** 係危險 - 要拆步。
- 加 `NOT NULL`：先加 nullable → backfill → 再加約束，分階段。
- 大表加 index：Postgres 用 `CREATE INDEX CONCURRENTLY` 免鎖表。
- GitLab 甚至將 migration 分「regular」同「post-deployment」兩類去強制呢個紀律。

### 對你個 project 而言
而家 `create-drop` 每次開機重起 DB，所以你**暫時唔會撞呢個問題**。但一旦切返 Flyway + 有真資料嘅 prod，**expand-and-contract 就係你日後改 schema 而唔停機、唔失資料嘅方法**。
