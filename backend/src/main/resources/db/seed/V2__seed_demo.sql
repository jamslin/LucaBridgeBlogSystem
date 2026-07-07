-- Demo seed data for the 'dev' profile only (see application-dev.yml flyway locations).
-- 4 demo posts, one per mission category, each with 繁中 required + EN/简中 optional.

INSERT INTO author (name, avatar_url, bio) VALUES
    ('LucaBridge 編輯部', NULL, 'LucaBridge editorial team.');

INSERT INTO post (slug, category_id, author_id, status, cover_image_url, reading_minutes, published_at) VALUES
    ('coastal-cleanup-2026-spring',
        (SELECT id FROM category WHERE key = 'environment'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'https://picsum.photos/seed/lucabridge-coastal/1200/700', 4, now() - interval '2 days'),
    ('winter-food-drive-2026',
        (SELECT id FROM category WHERE key = 'poverty-relief'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'https://picsum.photos/seed/lucabridge-fooddrive/1200/700', 3, now() - interval '9 days'),
    ('campus-recycling-workshop',
        (SELECT id FROM category WHERE key = 'campus'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'https://picsum.photos/seed/lucabridge-campus/1200/700', 5, now() - interval '20 days'),
    ('volunteer-orientation-2026',
        (SELECT id FROM category WHERE key = 'volunteering'),
        (SELECT id FROM author LIMIT 1), 'PUBLISHED',
        'https://picsum.photos/seed/lucabridge-volunteer/1200/700', 2, now() - interval '30 days');

INSERT INTO post_translation (post_id, lang, title, subtitle, excerpt, body_markdown) VALUES
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), 'zh-Hant',
        '春季海岸清潔行動', '超過一百位義工參與', '樂橋與社區義工於本季展開海岸清潔，共收集逾三百公斤海洋垃圾。',
        E'## 行動回顧\n本季海岸清潔行動吸引超過一百位義工參與，共收集逾三百公斤海洋垃圾。\n\n> 每一次清潔，都是對海洋的一次承諾。\n\n感謝所有參與的義工與合作夥伴。'),
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), 'en',
        'Spring Coastal Clean-Up', 'Over 100 volunteers joined in', 'LucaBridge and community volunteers removed over 300kg of marine debris this season.',
        E'## Recap\nThis season''s coastal clean-up brought together more than 100 volunteers, collecting over 300kg of marine debris.\n\n> Every clean-up is a promise kept to the ocean.\n\nThanks to everyone who took part.'),
    ((SELECT id FROM post WHERE slug = 'winter-food-drive-2026'), 'zh-Hant',
        '冬季糧食援助計劃', '為兩百個基層家庭送暖', '樂橋聯同本地商戶為兩百個基層家庭提供冬季糧食包。',
        E'## 計劃內容\n本次糧食援助計劃為兩百個基層家庭提供糧食包，並由義工親自派送。\n\n感謝各方善心人士的捐助。'),
    ((SELECT id FROM post WHERE slug = 'campus-recycling-workshop'), 'zh-Hant',
        '校園回收工作坊', '中學生齊學環保實踐', '樂橋走進校園，與中學生一同學習回收分類與升級再造。',
        E'## 工作坊回顧\n本次工作坊邀請中學生參與回收分類與升級再造實作，反應熱烈。'),
    ((SELECT id FROM post WHERE slug = 'volunteer-orientation-2026'), 'zh-Hant',
        '新義工迎新日', '認識樂橋的使命與工作', '樂橋為新義工舉辦迎新日，介紹機構使命及各項服務工作。',
        E'## 活動內容\n迎新日內容包括機構使命介紹、崗位配對及安全須知。歡迎有志之士加入樂橋大家庭。');

INSERT INTO media (post_id, url, width, height, caption, sort_order) VALUES
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), 'https://picsum.photos/seed/lucabridge-coastal-1/900/600', 900, 600, '義工分組清潔海岸線', 1),
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), 'https://picsum.photos/seed/lucabridge-coastal-2/900/600', 900, 600, '收集所得的海洋垃圾', 2),
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), 'https://picsum.photos/seed/lucabridge-coastal-3/900/600', 900, 600, '活動大合照', 3);

INSERT INTO press_link (post_id, label, url, sort_order) VALUES
    ((SELECT id FROM post WHERE slug = 'coastal-cleanup-2026-spring'), '明報報導', 'https://example.com/press/mingpao-coastal-cleanup', 1);
