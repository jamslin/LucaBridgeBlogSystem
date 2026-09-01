-- Fills in trilingual text that V2 left blank on rows it already seeds.
--
-- Scope is deliberately narrow. V2 established that a migration may carry closed-list
-- reference data — the five services, the single company row — but not content. This
-- adds no rows and creates no new concepts: it only completes columns that were left
-- NULL on those same reference rows, using the approved wording from the design comps.
--
-- Every statement is `WHERE <column> IS NULL`, so it can only ever fill a blank. If an
-- editor has already written something, this leaves it alone. That matters because
-- migrations run in every environment including prod, and the last repo-driven seed in
-- this project overwrote a live row (see application-dev.yml).
--
-- Home-page blocks are NOT here. Those are editorial content and belong to the CMS —
-- see db/seed/home_blocks.sql for an opt-in starter set.

-- ---------------------------------------------------------------------------
-- service descriptions — shown on the services page and the home chip row
-- ---------------------------------------------------------------------------

UPDATE service_text st
SET tc_description = COALESCE(st.tc_description, v.tc),
    en_description = COALESCE(st.en_description, v.en),
    sc_description = COALESCE(st.sc_description, v.sc)
FROM (VALUES
    ('environment',
     '海岸清潔、回收與生態導賞，把環保由概念變成一個上午的實際行動。',
     'Shoreline clean-ups, recycling drives and eco walks — turning conservation from an idea into one morning''s concrete work.',
     '海岸清洁、回收与生态导赏，把环保由概念变成一个上午的实际行动。'),
    ('poverty',
     '糧食包、日用品及全新校服，直接送到元朗區的基層家庭手上。',
     'Food packs, daily necessities and new school uniforms delivered straight to low-income families in Yuen Long.',
     '粮食包、日用品及全新校服，直接送到元朗区的基层家庭手上。'),
    ('elderly',
     '獨居長者定期家訪：同一組義工跟進同一批長者，記得每一戶的名字。',
     'Regular home visits to elderly people living alone — the same volunteers with the same households, so we remember every name.',
     '独居长者定期家访：同一组义工跟进同一批长者，记得每一户的名字。'),
    ('campus',
     '與中學合辦服務學習，由學生自己策劃、自己執行。',
     'Service learning run with secondary schools, planned and delivered by the students themselves.',
     '与中学合办服务学习，由学生自己策划、自己执行。'),
    ('volunteer',
     '基礎培訓、工具與保險一應俱全，無經驗也可以由第一次做起。',
     'Training, equipment and insurance provided — no experience needed to start with your first shift.',
     '基础培训、工具与保险一应俱全，无经验也可以由第一次做起。')
) AS v(code, tc, en, sc)
JOIN service s ON s.code = v.code
WHERE st.service_id = s.id
  AND (st.tc_description IS NULL OR st.en_description IS NULL OR st.sc_description IS NULL);

-- ---------------------------------------------------------------------------
-- company — address, office hours and the about paragraph. The row and its
-- name/tagline already exist from V2; these columns were left blank.
-- ---------------------------------------------------------------------------

UPDATE company_text
SET tc_about = COALESCE(tc_about,
        '樂橋成立於 2021 年，是受《稅務條例》第 88 條監管的香港非牟利慈善團體。我們透過環保保育、扶貧支援、長者關懷、校園計劃及義工發展五個服務範疇，連結有需要人士、義工與社區伙伴。'),
    en_about = COALESCE(en_about,
        'Founded in 2021, LucaBridge is a Hong Kong charity registered under Section 88 of the Inland Revenue Ordinance. We work across five service areas — conservation, poverty relief, elderly care, campus programmes and volunteer development — connecting people in need with volunteers and community partners.'),
    sc_about = COALESCE(sc_about,
        '乐桥成立于 2021 年，是受《税务条例》第 88 条监管的香港非牟利慈善团体。我们透过环保保育、扶贫支援、长者关怀、校园计划及义工发展五个服务范畴，连结有需要人士、义工与社区伙伴。'),
    tc_address      = COALESCE(tc_address, '元朗'),
    en_address      = COALESCE(en_address, 'Yuen Long, Hong Kong'),
    sc_address      = COALESCE(sc_address, '元朗'),
    tc_office_hours = COALESCE(tc_office_hours, '星期一至五 10:00–18:00'),
    en_office_hours = COALESCE(en_office_hours, 'Mon–Fri 10:00–18:00'),
    sc_office_hours = COALESCE(sc_office_hours, '星期一至五 10:00–18:00')
WHERE company_id = 1;
