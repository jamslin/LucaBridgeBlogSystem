-- Reference data — NOT seed content.
--
-- These rows are part of the schema contract: the application is broken without them,
-- they are closed lists that change only when code changes, and a fresh clone must boot
-- without anyone handing over a dump file. Actual content (blog posts, events, jobs,
-- home blocks, uploaded media) never appears in a migration — it arrives by dump/restore.
--
-- Every statement is idempotent so re-running against a partially populated database is
-- safe.

-- ---------------------------------------------------------------------------
-- company — the single row must exist before anything can update it
-- ---------------------------------------------------------------------------

INSERT INTO company (id, charity_reg_no, founded_year, phone, email)
VALUES (1, '91/17604', 2021, '+852 2608 9577', 'enquiry@lucabridge.org.hk')
ON CONFLICT (id) DO NOTHING;

INSERT INTO company_text (company_id, tc_name, en_name, sc_name, tc_tagline, en_tagline, sc_tagline)
VALUES (1,
        '樂橋', 'LucaBridge', '乐桥',
        '用愛築橋，連結希望', 'Bridging Hearts, Building Hope', '用爱筑桥，连结希望')
ON CONFLICT (company_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- service — the five programme areas. Drives the home chip row, the home service
-- cards, the services page, and blog tagging.
-- ---------------------------------------------------------------------------

INSERT INTO service (code, sort_order) VALUES
    ('environment', 10),
    ('poverty',     20),
    ('elderly',     30),
    ('campus',      40),
    ('volunteer',   50)
ON CONFLICT (code) DO NOTHING;

INSERT INTO service_text (service_id, tc_name, en_name, sc_name)
SELECT s.id, v.tc, v.en, v.sc
FROM (VALUES
    ('environment', '環保保育', 'Environment',      '环保保育'),
    ('poverty',     '扶貧支援', 'Poverty relief',   '扶贫支援'),
    ('elderly',     '長者關懷', 'Elderly care',     '长者关怀'),
    ('campus',      '校園計劃', 'Campus programme', '校园计划'),
    ('volunteer',   '義工發展', 'Volunteering',     '义工发展')
) AS v(code, tc, en, sc)
JOIN service s ON s.code = v.code
ON CONFLICT (service_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- referral_group — the 所屬團體 / 推薦團體 options on the event registration form.
--
-- NOTE: this list came from the client's existing Google Form, which belongs to a
-- partner organisation. Confirm with the client that these are the groups they want
-- before the form goes live.
-- ---------------------------------------------------------------------------

INSERT INTO referral_group (code, sort_order) VALUES
    ('hkiyf',        10),
    ('hkya',         20),
    ('cw_district',  30),
    ('east_district',40),
    ('south_yda',    50),
    ('wanchai',      60),
    ('hawkers',      70),
    ('ossa',         80),
    ('music_sports', 90),
    ('other',       999)
ON CONFLICT (code) DO NOTHING;

INSERT INTO referral_group_text (referral_group_id, tc_name)
SELECT g.id, v.tc
FROM (VALUES
    ('hkiyf',        '香港島青年聯會'),
    ('hkya',         '香港青年會'),
    ('cw_district',  '香港中西區青年聯會'),
    ('east_district','香港東區青年聯會'),
    ('south_yda',    '南區青年發展聯會'),
    ('wanchai',      '灣仔青年聯會'),
    ('hawkers',      '港九新界販商社團聯合會青年聯盟'),
    ('ossa',         '香港傑出學生協進會'),
    ('music_sports', '香港音樂體育文化聯盟'),
    ('other',        '其他社交媒體或朋友')
) AS v(code, tc)
JOIN referral_group g ON g.code = v.code
ON CONFLICT (referral_group_id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- NOT here on purpose:
--
--   app_user — the bootstrap admin is created by AdminUserInitializer from an env var.
--              A credential in a SQL file is exactly how the previous admin password
--              got clobbered by a reseed.
--   home_block, blog, event, job, media — content. Dump/restore only.
-- ---------------------------------------------------------------------------
