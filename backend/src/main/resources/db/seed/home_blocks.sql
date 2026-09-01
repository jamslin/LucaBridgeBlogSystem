-- Starter home-page content, transcribed from the design comps.
--
-- NOT a Flyway migration, and deliberately so. Migrations in this project carry
-- schema and closed-list reference data only; home blocks are editorial content,
-- which the V2 header names explicitly as belonging to dump/restore rather than
-- to the repo. The rule exists because a repo-driven seed here once overwrote a
-- live row in prod (see application-dev.yml). So this is opt-in: nothing runs it
-- for you, in any environment.
--
-- Run it once against an empty home_block table to get the home page the comps
-- show, then edit everything in the CMS afterwards.
--
-- Copy the file in and run it there. This works the same on macOS, Linux and
-- Windows, and — unlike piping — cannot re-encode the file on the way in, which
-- matters because every string here is Chinese:
--
--   docker compose cp backend/src/main/resources/db/seed/home_blocks.sql postgres:/tmp/seed.sql
--   docker compose exec postgres psql -U lucabridge -d lucabridge -f /tmp/seed.sql
--
-- Avoid `psql ... < file` in PowerShell: `<` is a reserved operator there, and
-- `Get-Content | psql` re-encodes the text (PowerShell 5.1 pipes to native
-- commands as ASCII by default), which turns the Chinese into question marks.
--
-- Safe to run twice: every insert is guarded on the slot already being empty, so
-- it will not duplicate blocks or touch anything you have edited.
--
-- What it does NOT set:
--   * HERO image — upload one in the CMS (Home page → the HERO block → Upload).
--     Without it the hero falls back to a red gradient, which is a legitimate
--     look, not a broken state.
--   * FEATURED — needs a real published blog post to pin, so it is left out.

BEGIN;

-- ---------------------------------------------------------------------------
-- HERO — the headline over the hero image. The newline in tc_title/en_title is
-- meaningful: the home page breaks the headline exactly where the editor does.
-- ---------------------------------------------------------------------------
WITH new_block AS (
    INSERT INTO home_block (slot, link_url, sort_order, is_active)
    SELECT 'HERO', '/volunteer', 0, true
    WHERE NOT EXISTS (SELECT 1 FROM home_block WHERE slot = 'HERO')
    RETURNING id
)
INSERT INTO home_block_text (home_block_id, tc_eyebrow, en_eyebrow, sc_eyebrow,
                             tc_title, en_title, sc_title,
                             tc_subtitle, en_subtitle, sc_subtitle,
                             tc_button_label, en_button_label, sc_button_label)
SELECT id,
       '與社區同行', 'Walking with the community', '与社区同行',
       E'用愛築橋\n連結希望',
       E'Bridging hearts,\nbuilding hope',
       E'用爱筑桥\n连结希望',
       '樂橋是香港的非牟利慈善團體，透過保育、關懷及社區服務，連結愛心與希望。',
       'LucaBridge is a Hong Kong charity connecting care and hope through conservation, community support and volunteering.',
       '乐桥是香港的非牟利慈善团体，透过保育、关怀及社区服务，连结爱心与希望。',
       '成為義工', 'Become a volunteer', '成为义工'
FROM new_block;

-- ---------------------------------------------------------------------------
-- STAT — the proof tiles inside the red band. Title is the number alone; the
-- subtitle's first line is the label and its second line the caption.
-- ---------------------------------------------------------------------------
WITH new_blocks AS (
    INSERT INTO home_block (slot, sort_order, is_active)
    SELECT 'STAT', v.sort_order, true
    FROM (VALUES (0), (1), (2)) AS v(sort_order)
    WHERE NOT EXISTS (SELECT 1 FROM home_block WHERE slot = 'STAT')
    RETURNING id, sort_order
)
INSERT INTO home_block_text (home_block_id, tc_title, en_title, sc_title,
                             tc_subtitle, en_subtitle, sc_subtitle)
SELECT b.id, v.tc_title, v.en_title, v.sc_title, v.tc_sub, v.en_sub, v.sc_sub
FROM new_blocks b
JOIN (VALUES
    (0, '895', '895', '895',
        E'位支持者\n已與我們同行',
        E'supporters\nwalking with us',
        E'位支持者\n已与我们同行'),
    (1, '5', '5', '5',
        E'個服務範疇\n由環保到長者關懷',
        E'service areas\nfrom conservation to elderly care',
        E'个服务范畴\n由环保到长者关怀'),
    (2, '2021', '2021', '2021',
        E'年成立\n稅務編號 91/17604',
        E'founded\nCharity no. 91/17604',
        E'年成立\n税务编号 91/17604')
) AS v(sort_order, tc_title, en_title, sc_title, tc_sub, en_sub, sc_sub)
  ON v.sort_order = b.sort_order;

-- ---------------------------------------------------------------------------
-- SUPPORT — the one ask on the page. Without this block the band still renders
-- from the frontend's fallback copy; adding it makes the wording editable.
-- ---------------------------------------------------------------------------
WITH new_block AS (
    INSERT INTO home_block (slot, link_url, sort_order, is_active)
    SELECT 'SUPPORT', '/volunteer', 0, true
    WHERE NOT EXISTS (SELECT 1 FROM home_block WHERE slot = 'SUPPORT')
    RETURNING id
)
INSERT INTO home_block_text (home_block_id, tc_eyebrow, en_eyebrow, sc_eyebrow,
                             tc_title, en_title, sc_title,
                             tc_subtitle, en_subtitle, sc_subtitle,
                             tc_button_label, en_button_label, sc_button_label,
                             tc_note, en_note, sc_note)
SELECT id,
       '招募義工 · VOLUNTEER', 'Volunteer', '招募义工 · VOLUNTEER',
       E'登記一次，\n全年都能上場。',
       E'Register once,\njoin us all year.',
       E'登记一次，\n全年都能上场。',
       '填一次義工表格，之後每次活動只需回覆 WhatsApp 就能報名。無需經驗，我們提供培訓、工具與保險。',
       'Fill in the volunteer form once, then sign up for any event with a single WhatsApp reply. No experience needed — we provide training, equipment and insurance.',
       '填一次义工表格，之后每次活动只需回复 WhatsApp 就能报名。无需经验，我们提供培训、工具与保险。',
       '成為義工', 'Become a volunteer', '成为义工',
       -- Fine print beside the button. The field count is a claim about the
       -- registration form, so it lives here where staff can correct it.
       E'約 5 分鐘 · 11 個欄位\n18 歲以下需家長簽署同意書',
       E'About 5 minutes · 11 fields\nUnder 18s need a signed parental consent form',
       E'约 5 分钟 · 11 个栏位\n18 岁以下需家长签署同意书'
FROM new_block;

-- ---------------------------------------------------------------------------
-- QUICK_LINK — the three routes under the news grid.
-- ---------------------------------------------------------------------------
WITH new_blocks AS (
    INSERT INTO home_block (slot, link_url, sort_order, is_active)
    SELECT 'QUICK_LINK', v.link_url, v.sort_order, true
    FROM (VALUES ('/donate', 0), ('/careers', 1), ('/contact', 2)) AS v(link_url, sort_order)
    WHERE NOT EXISTS (SELECT 1 FROM home_block WHERE slot = 'QUICK_LINK')
    RETURNING id, sort_order
)
INSERT INTO home_block_text (home_block_id, tc_title, en_title, sc_title,
                             tc_subtitle, en_subtitle, sc_subtitle)
SELECT b.id, v.tc_title, v.en_title, v.sc_title, v.tc_sub, v.en_sub, v.sc_sub
FROM new_blocks b
JOIN (VALUES
    (0, '物資捐贈', 'Donate goods', '物资捐赠',
        '米糧、日用品及全新校服', 'Rice, daily necessities and new school uniforms', '米粮、日用品及全新校服'),
    (1, '職位空缺', 'Job openings', '职位空缺',
        '查看現時開放的崗位', 'See the roles open right now', '查看现时开放的岗位'),
    (2, '聯絡我們', 'Contact us', '联络我们',
        '元朗 · +852 2608 9577', 'Yuen Long · +852 2608 9577', '元朗 · +852 2608 9577')
) AS v(sort_order, tc_title, en_title, sc_title, tc_sub, en_sub, sc_sub)
  ON v.sort_order = b.sort_order;

COMMIT;
