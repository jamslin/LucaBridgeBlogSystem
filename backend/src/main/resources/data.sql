-- Combined dev seed. Loaded by spring.sql.init AFTER Hibernate builds the
-- schema from the @Entity classes (see application-dev.yml). Inserts only —
-- no DDL. Sourced from the former Flyway V1 (categories) + V2 (author) + V4.

-- Category reference data (from former V1):
-- Seed categories (繁中 required, EN/简中 optional per "Resolved decisions")
INSERT INTO category (key, sort_order) VALUES
    ('poverty-relief', 1),
    ('environment', 2),
    ('campus', 3),
    ('volunteering', 4);

INSERT INTO category_translation (category_id, lang, name) VALUES
    ((SELECT id FROM category WHERE key = 'poverty-relief'), 'zh-Hant', '扶貧'),
    ((SELECT id FROM category WHERE key = 'poverty-relief'), 'en', 'Poverty Relief'),
    ((SELECT id FROM category WHERE key = 'poverty-relief'), 'zh-Hans', '扶贫'),
    ((SELECT id FROM category WHERE key = 'environment'), 'zh-Hant', '環保'),
    ((SELECT id FROM category WHERE key = 'environment'), 'en', 'Environment'),
    ((SELECT id FROM category WHERE key = 'environment'), 'zh-Hans', '环保'),
    ((SELECT id FROM category WHERE key = 'campus'), 'zh-Hant', '校園'),
    ((SELECT id FROM category WHERE key = 'campus'), 'en', 'Campus'),
    ((SELECT id FROM category WHERE key = 'campus'), 'zh-Hans', '校园'),
    ((SELECT id FROM category WHERE key = 'volunteering'), 'zh-Hant', '義工'),
    ((SELECT id FROM category WHERE key = 'volunteering'), 'en', 'Volunteering'),
    ((SELECT id FROM category WHERE key = 'volunteering'), 'zh-Hans', '义工');

-- Shared editorial author (from former V2):
INSERT INTO author (name, avatar_url, bio) VALUES
    ('LucaBridge 編輯部', NULL, 'LucaBridge editorial team.');

-- Imported Wix content + pages/events/settings (from former V4):
-- V4 — Real LucaBridge content imported from the live Wix site (lucabridge.com).
-- DEV-ONLY seed (classpath:db/seed) — never runs in prod, same pattern as V2 demo.
-- Language: 繁中 (zh-Hant) only, matching the live site. EN / 简中 fall back to 繁中.
--
-- Image URLs point at MinIO (populated by sync-wix-images.sh). Dev host below;
-- prod swaps the host via the media base config.
--   MinIO dev base: http://localhost:9000/blog-media/wix/
--
-- NOTE (data quality): the live "About" page contradicts itself — the intro says the
-- charity was founded 2021 (成立於2021年) while the "歷史背景" block says 2010 (建立於2010年).
-- Preserved verbatim below; confirm the correct year with the client before go-live.

-- ═════════════════════════════════════════════════════════════
-- Site settings (footer / masthead / org facts)
-- ═════════════════════════════════════════════════════════════
INSERT INTO site_setting (key, value) VALUES
    ('org_name_zh',   '樂橋'),
    ('org_name_en',   'LucaBridge'),
    ('slogan',        '一起攜手，創造更美好的未來！'),
    ('tagline',       '我們致力於推動支援香港弱勢社群的行動'),
    ('founded_year',  '2021'),
    ('tax_exempt_no', '91/17604'),
    ('address',       '元朗合益中心'),
    ('phone',         '+852 2660 9577'),
    ('whatsapp',      '+852 61850571'),
    ('email',         'enquiry@lokkiu.org.hk'),
    ('facebook_url',  'https://www.facebook.com/profile.php?id=61554823205966'),
    ('instagram_url', 'https://www.instagram.com/luca_bridge159/');

-- ═════════════════════════════════════════════════════════════
-- Static pages
-- ═════════════════════════════════════════════════════════════
INSERT INTO page (slug, page_type, status, hero_image_url, sort_order) VALUES
    ('about',          'STANDARD', 'PUBLISHED', NULL, 1),
    ('poverty-relief', 'STANDARD', 'PUBLISHED', 'http://localhost:9000/blog-media/wix/02a841_9edea948.jpg', 2),
    ('campus',         'STANDARD', 'PUBLISHED', 'http://localhost:9000/blog-media/wix/02a841_9a6ab234.jpg', 3),
    ('donate',         'DONATE',   'PUBLISHED', NULL, 4),
    ('privacy',        'LEGAL',    'PUBLISHED', NULL, 90),
    ('terms',          'LEGAL',    'PUBLISHED', NULL, 91);

-- ── About (關於樂橋) ──────────────────────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'about'), 'zh-Hant',
     '關於樂橋', '一起攜手，創造更美好的未來！',
     E'## 關於樂橋\n\n樂橋是一個非牟利慈善團體，一直視幫助弱勢社群、保護環境為己任，成立於2021年，屬公共性質的慈善機構，受《稅務條例》第88條監管，慈善團體免稅編號（91/17604）。\n\n## 使命與願景\n\n我們的使命是通過教育、醫療和社區支持，改善弱勢群體的生活品質。我們致力於建造一個人人都能公平享有資源的世界。\n\n## 歷史背景\n\n我們的慈善團體建立於2010年，起初由一小群志同道合的朋友發起，目的是提供基礎教育給偏鄉地區的孩子。十多年來，我們的行動已經擴展到更多領域，並幫助了數以萬計的人。');

-- ── Poverty relief work (扶貧工作) ───────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'poverty-relief'), 'zh-Hant',
     '香港扶貧計劃 — 攜手扶貧，共創希望', NULL,
     E'在香港這座繁華的大都市中，有許多人仍然面臨生活困境。根據最新統計，香港約有20%的人口生活在貧窮線以下，他們每日為基本生活需求而奮鬥，缺乏足夠的住房、食物和教育資源。\n\n我們深信，每個人都應該有機會過上有尊嚴的生活。為此，我們致力於推動一系列扶貧計畫，幫助弱勢群體重新燃起希望：\n\n- **基礎生活支援**：提供糧食包、生活用品和緊急補助，減輕經濟壓力。\n- **教育資助**：為貧困家庭的孩子提供學費補助與學習資源，讓知識改變未來。\n- **就業支持**：舉辦職業培訓和就業輔導，幫助低收入人士獲得穩定收入。\n- **社區重建**：改善基層社區的基礎設施，創造更安全、舒適的居住環境。\n\n我們的努力已經讓成千上萬的家庭重新站穩腳步，讓孩子重返校園，讓長者感受到社會的關愛。這些改變，離不開您的支持。\n\n加入我們，一起改變香港的未來！您的一點支持，可以為一個家庭帶來希望。讓我們攜手同行，共同打造一個更公平、更有愛的社會。\n\n## 基礎生活支援：提供即時援助，減輕經濟壓力\n\n我們深知，解決日常生活的基本需求是扶貧工作的起點。許多基層家庭因收入不足，甚至無法負擔三餐或基本生活用品。我們的基礎生活支援計畫專注於：\n\n- **糧食包分發**：定期向有需要的家庭提供糧食包，內含米、麵條、罐頭食品等日常必需品，確保每個人都能吃飽。\n- **生活用品補助**：分發衛生用品、衣物、毛毯等，幫助低收入家庭改善生活質量，特別是在冬季或緊急情況下。\n- **緊急現金補助**：針對特殊情況（如意外、疾病或家庭變故），提供短期經濟援助，幫助受助者渡過難關，避免陷入更深的困境。\n\n這些即時援助，為許多家庭帶來了基本的保障，也讓他們感受到社會的溫暖與支持。\n\n## 教育資助：讓知識改變未來\n\n教育是擺脫貧困的關鍵，但對許多貧困家庭而言，昂貴的學費和學習資源成為沉重的負擔。我們致力於確保每個孩子都有接受教育的機會，為此推出了以下措施：\n\n- **學費補助計畫**：資助來自低收入家庭的學生，幫助他們完成中小學甚至高等教育的學業，不讓經濟困難成為學習的阻礙。\n- **學習資源提供**：捐贈書籍、學習工具和電子設備，確保孩子們擁有足夠的資源來學習和成長。\n- **課後輔導與支援**：為學習困難的孩子提供免費的課後輔導與心理支持，幫助他們建立自信，追求更高的成就。\n\n我們相信，知識的力量可以改變孩子的一生，而每一份支持，都是對未來的投資。\n\n## 就業支持：助力低收入人士實現經濟獨立\n\n在香港的基層社群中，許多人因缺乏技能或相關經驗，難以找到穩定的工作，導致長期經濟困難。我們的就業支持計畫致力於幫助這些人群提升能力，找到適合的職業機會：\n\n- **職業技能培訓**：開設免費的職業培訓課程，如烹飪、護理、清潔、基礎電腦操作等，幫助參與者掌握實用技能。\n- **就業輔導**：提供一對一的職業規劃建議，協助參與者更新履歷、準備面試，並連結潛在的雇主資源。\n- **創業支持**：為有創業意向的人士提供資金、指導與資源，幫助他們實現經濟自立。\n- **企業合作**：我們與本地企業建立合作，創造更多適合低技能勞工的工作機會。\n\n我們的目標是幫助受助者不僅擺脫短期貧困，更有能力長期維持經濟獨立。\n\n## 社區重建資助\n\n貧困地區的基礎設施常年不足，居住環境惡劣，直接影響居民的生活質量與健康。我們的社區重建計畫專注於改善這些問題，為基層社區注入新活力：\n\n- **基礎設施升級**：修繕老舊房屋，改善供水、照明和衛生設施，讓居民擁有更安全舒適的居住空間。\n- **公共空間建設**：興建或翻新社區中心、公園和兒童遊樂場，為居民提供更多的活動空間與資源。\n- **健康與衛生推廣**：在社區內開展清潔運動與衛生教育活動，提升居民的健康意識，預防疾病傳播。\n- **社區活動**：舉辦文化節日、社區聚會等活動，促進鄰里關係，增強社區凝聚力。\n\n透過這些努力，我們希望不僅改善物理環境，更讓居民感受到歸屬感與尊重。');

-- ── Campus activities (校園活動) ─────────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'campus'), 'zh-Hant',
     '為什麼選擇校園活動？', NULL,
     E'校園活動不僅能讓學生在安全的學習環境中了解社會議題，更能培養領導力、團隊合作能力與同理心。我們希望透過這些活動，讓每位學生明白：「每個小行動，都能為社會帶來積極的改變。」\n\n我們的校園活動包括：\n\n- **慈善義賣**\n- **沙灘清潔**\n- **中醫義診**\n- **慈善路跑**\n\n## 支持校園活動\n\n### 加入我們，讓愛心從校園延伸到社會！\n\n無論是參與活動、協助籌辦，還是提供支持，您的投入將幫助更多學生成為改變的推動者。讓我們攜手，透過校園活動，為社會注入更多希望與愛心！');

-- ── Donate (立即捐款) ────────────────────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'donate'), 'zh-Hant',
     '攜手共創美好未來', NULL,
     E'歡迎來到我們的捐款主頁，您的每一份捐款都將為環保、兒童教育、基層家庭和長者關懷帶來深遠影響。無論金額大小，您的支持都能為社區注入新的希望與力量。一起行動，讓愛傳遞，為未來播下希望的種子！\n\n捐款方向：\n\n- **環保 — 守護地球**：一起守護地球，讓未來更綠色！\n- **兒童及學生 — 夢想起航**：每一份捐款，助力孩子夢想成真！\n- **基層家庭及老人 — 共建美好陪伴**：關愛基層，攜手共創美好生活；關懷長者，讓愛陪伴他們每一天！\n\n> 注意：網上捐款一經扣賬便不能取消、退款及更改，敬請於過賬前確認捐款金額及資料。謝謝！\n\n捐款金額（示例）：HK$500 · HK$1,000 · HK$2,000\n\n捐款≥100港幣將自動發送收據，供稅務扣除用途。');

-- ── Privacy Policy (隱私政策) ────────────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'privacy'), 'zh-Hant',
     '樂橋隱私政策', '生效日期：2025年7月31日',
     E'## 1. 引言\n\n樂橋（香港註冊非牟利慈善團體，免稅編號 91/17604）承諾保護您的個人資料，並遵守香港《個人資料（私隱）條例》（第486章）及其他適用法律。本隱私政策說明我們如何收集、使用、儲存及保護您的個人資料。\n\n## 2. 收集的資料\n\n我們可能收集以下個人資料：\n\n- 姓名、電郵地址、電話號碼（例如，捐款、會員註冊或活動報名時提供）。\n- 付款資訊（僅限捐款，通過安全支付平台處理）。\n- 網站使用資料（例如，IP地址、瀏覽器類型，通過Cookies或其他追蹤技術收集）。\n- 其他自願提供的資料（例如，志願者申請中的技能或興趣）。\n\n## 3. 資料用途\n\n您的個人資料將用於：\n\n- 處理捐款、會員註冊及活動報名。\n- 發送確認電郵、收據及樂橋最新消息（經您同意）。\n- 分析網站使用情況以改善服務。\n- 遵守法律義務或回應政府要求。\n\n## 4. 資料分享\n\n我們不會出售或租賃您的個人資料，但可能與以下第三方分享：\n\n- 支付處理商（例如，Wix Payments）以完成捐款交易。\n- 服務提供商（例如，電郵服務）以支持我們的運營。\n- 法律要求下的相關機構。\n\n## 5. 資料儲存與安全\n\n- 您的資料儲存在安全的伺服器上，採用行業標準加密技術。\n- 我們採取合理措施保護資料，但無法保證絕對安全。\n- 資料將保留至完成其用途或法律要求為止，之後將安全刪除。\n\n## 6. 您的權利\n\n根據香港《個人資料（私隱）條例》，您有權：\n\n- 查閱及更正您的個人資料。\n- 要求停止使用您的資料作直接營銷用途。\n- 聯繫我們以行使權利：enquiry@lokkiu.org.hk。\n\n## 7. Cookies 與追蹤技術\n\n- 我們使用Cookies改善網站體驗（例如，記住語言偏好）。\n- 您可通過瀏覽器設置禁用Cookies，但可能影響網站功能。\n\n## 8. 第三方連結\n\n我們的網站可能包含第三方連結（例如，KOL合作夥伴）。我們不對其隱私政策負責，請審閱相關政策。\n\n## 9. 政策更新\n\n我們可能不時更新本隱私政策，修訂版本將發佈於網站，注明生效日期。\n\n## 10. 聯繫我們\n\n如有任何疑問，請聯繫：\n\n- 電郵：enquiry@lokkiu.org.hk\n\n樂橋，2021年成立，香港註冊非牟利慈善團體（免稅編號 91/17604）');

-- ── Terms & Conditions (使用條款) ────────────────────────────
INSERT INTO page_translation (page_id, lang, title, subtitle, body_markdown) VALUES
    ((SELECT id FROM page WHERE slug = 'terms'), 'zh-Hant',
     '樂橋使用條款與條件', '生效日期：2025年7月31日',
     E'## 1. 引言\n\n歡迎使用樂橋網站。本使用條款與條件（「條款」）規範您使用本網站及相關服務（包括捐款、會員註冊、活動報名）。使用本網站即表示您同意遵守本條款。\n\n## 2. 使用資格\n\n- 您必須年滿指定歲數或在監護人同意下使用本網站。\n- 您同意提供真實、準確及完整的資料（例如，捐款或註冊時）。\n\n## 3. 網站使用\n\n- 您可使用本網站瀏覽資訊、捐款、註冊會員或參加活動。\n- 禁止行為包括但不限於：上傳惡意軟件、侵犯版權、或從事非法活動。\n- 樂橋保留終止違反條款用戶訪問權利的權利。\n\n## 4. 捐款\n\n- 捐款通過Wix Donations或其他支付平台處理，接受Visa、MasterCard、銀聯及手動支付方式（FPS、支付寶、PayMe、BOC Pay、銀行轉帳）。\n- 捐款為自願且不可退款，除非法律另有規定。\n- 捐款≥100港幣將自動發送收據，供稅務扣除用途。\n\n## 5. 會員與活動\n\n- 會員註冊需提供個人資料並遵守會員規則。\n- 活動報名須遵守活動特定條款（例如，取消政策）。\n- 樂橋保留取消或更改活動的權利，詳情將提前通知。\n\n## 6. 知識產權\n\n- 本網站內容（文字、圖片、標誌）屬樂橋或其許可方所有，受版權法保護。\n- 未經許可，您不得複製、分發或修改網站內容。\n\n## 7. 責任限制\n\n- 本網站按「現狀」提供，樂橋不保證其無錯誤或不間斷。\n- 樂橋對因使用網站導致的任何損失（包括資料丟失）不承擔責任，除非法律另有規定。\n\n## 8. 第三方連結\n\n- 網站可能包含第三方連結（例如，KOL合作夥伴），其內容及服務不受樂橋控制。\n- 您使用第三方服務需自行承擔風險。\n\n## 9. 條款更新\n\n- 樂橋可隨時更新本條款，修訂版本將發佈於網站，注明生效日期。\n- 繼續使用網站即表示您同意新條款。\n\n## 10. 適用法律\n\n- 本條款受香港特別行政區法律管轄，任何爭議將提交香港法院處理。\n\n## 11. 聯繫我們\n\n如有任何疑問，請聯繫：\n\n- 電郵：enquiry@lokkiu.org.hk\n\n樂橋，2021年成立，香港註冊非牟利慈善團體（免稅編號 91/17604）');

-- ═════════════════════════════════════════════════════════════
-- Events (未來活動)
-- ═════════════════════════════════════════════════════════════
INSERT INTO event (slug, status, starts_at, ends_at, location_text, cover_image_url) VALUES
    ('charity-meal-2025-08-16', 'PUBLISHED',
     TIMESTAMPTZ '2025-08-16 11:30:00+08', TIMESTAMPTZ '2025-08-16 13:30:00+08',
     '地點待定', 'http://localhost:9000/blog-media/wix/02a841_b705ec69.jpg');

INSERT INTO event_translation (event_id, lang, title, summary, body_markdown) VALUES
    ((SELECT id FROM event WHERE slug = 'charity-meal-2025-08-16'), 'zh-Hant',
     '慈善派飯活動', '八月十六日',
     E'## 派飯活動\n\n派飯活動旨在提供食物給有需要的人，促進社會互助和關懷。以下是一些關於派飯活動的要點：\n\n### 活動目的\n\n- 幫助有需要的家庭或個人\n- 促進社區凝聚力\n- 提高對食物浪費問題的認識\n\n**時間**：2025年8月16日 11:30 – 13:30\n**地點**：地點待定');

-- ═════════════════════════════════════════════════════════════
-- Real blog posts (from homepage 回憶記錄) — replaces the placeholder
-- English "welfare distribution" post, which was Wix AI filler.
-- These fill the previously-empty 'environment' category with real content.
-- ═════════════════════════════════════════════════════════════
INSERT INTO post (slug, category_id, author_id, status, cover_image_url, reading_minutes, published_at) VALUES
    ('coastal-cleanup-tsing-lung-tau-2025',
        (SELECT id FROM category WHERE key = 'environment'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'http://localhost:9000/blog-media/wix/02a841_02d65a97.jpg', 1, TIMESTAMPTZ '2025-04-02 10:00:00+08'),
    ('charity-blessing-bags-2025',
        (SELECT id FROM category WHERE key = 'poverty-relief'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'http://localhost:9000/blog-media/wix/02a841_ce489b52.jpg', 1, TIMESTAMPTZ '2025-05-31 10:00:00+08');

INSERT INTO post_translation (post_id, lang, title, subtitle, excerpt, body_markdown) VALUES
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-tsing-lung-tau-2025'), 'zh-Hant',
        '青龍頭碼頭海岸清潔行動', '2025年4月2日', '義工於青龍頭碼頭海岸展開清潔行動，共清理32公斤垃圾。',
        E'## 活動回顧\n\n**日期**：2025年4月2日\n\n**活動內容**：清潔行動\n\n**地點**：青龍頭碼頭海岸\n\n本次行動共清理 **32kg** 垃圾，感謝所有參與的義工。'),
    ((SELECT id FROM post WHERE slug = 'charity-blessing-bags-2025'), 'zh-Hant',
        '慈善福袋派發', '2025年5月31日', '樂橋義工派發慈善福袋，為基層家庭送上關懷。',
        E'## 活動留影\n\n**日期**：2025年5月31日\n\n**活動內容**：慈善福袋派發\n\n感謝各方善心人士的支持，讓關懷送到有需要的家庭手中。');

-- Media galleries for the two real posts (青龍頭 cleanup + 福袋 distribution)
INSERT INTO media (post_id, url, width, height, caption, sort_order) VALUES
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-tsing-lung-tau-2025'), 'http://localhost:9000/blog-media/wix/02a841_02d65a97.jpg', 480, 480, '青龍頭碼頭海岸清潔', 1),
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-tsing-lung-tau-2025'), 'http://localhost:9000/blog-media/wix/02a841_ddc14c59.jpg', 480, 480, '清潔行動', 2),
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-tsing-lung-tau-2025'), 'http://localhost:9000/blog-media/wix/02a841_ea19fbf6.jpg', 480, 480, '收集所得垃圾', 3),
    ((SELECT id FROM post WHERE slug = 'charity-blessing-bags-2025'), 'http://localhost:9000/blog-media/wix/02a841_ce489b52.jpg', 480, 480, '慈善福袋派發', 1),
    ((SELECT id FROM post WHERE slug = 'charity-blessing-bags-2025'), 'http://localhost:9000/blog-media/wix/02a841_730abf01.jpg', 480, 480, '福袋派發現場', 2),
    ((SELECT id FROM post WHERE slug = 'charity-blessing-bags-2025'), 'http://localhost:9000/blog-media/wix/02a841_a0712079.jpg', 480, 480, '義工合照', 3);
