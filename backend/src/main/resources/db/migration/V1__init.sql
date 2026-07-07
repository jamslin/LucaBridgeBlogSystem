-- LucaBridge blog schema — translation-per-row model
-- See Notion: 03 · Architecture, Data Model & API

CREATE TABLE author (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    avatar_url  VARCHAR(500),
    bio         TEXT
);

CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    key         VARCHAR(60) NOT NULL UNIQUE,   -- e.g. 'poverty-relief', 'environment', 'campus', 'volunteering'
    sort_order  INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE category_translation (
    id          BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    lang        VARCHAR(10) NOT NULL,          -- 'zh-Hant' | 'en' | 'zh-Hans'
    name        VARCHAR(120) NOT NULL,
    CONSTRAINT uq_category_translation UNIQUE (category_id, lang)
);

CREATE TABLE post (
    id               BIGSERIAL PRIMARY KEY,
    slug             VARCHAR(200) NOT NULL UNIQUE,
    category_id      BIGINT NOT NULL REFERENCES category(id),
    author_id        BIGINT REFERENCES author(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'DRAFT',   -- DRAFT | PUBLISHED
    cover_image_url  VARCHAR(500),
    reading_minutes  INTEGER,
    published_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_post_status_published_at ON post (status, published_at DESC);
CREATE INDEX idx_post_category ON post (category_id);

CREATE TABLE post_translation (
    id              BIGSERIAL PRIMARY KEY,
    post_id         BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    lang            VARCHAR(10) NOT NULL,       -- 'zh-Hant' | 'en' | 'zh-Hans'
    title           VARCHAR(300) NOT NULL,
    subtitle        VARCHAR(400),
    excerpt         VARCHAR(600),
    body_markdown   TEXT NOT NULL,
    CONSTRAINT uq_post_translation UNIQUE (post_id, lang)
);

CREATE TABLE media (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    width       INTEGER,
    height      INTEGER,
    caption     VARCHAR(300),
    sort_order  INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_media_post ON media (post_id, sort_order);

CREATE TABLE press_link (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES post(id) ON DELETE CASCADE,
    label       VARCHAR(200) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_press_link_post ON press_link (post_id, sort_order);

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
