-- V3 — Extend the blog model to match the live LucaBridge site.
-- Adds: static pages (About, Donate, legal), an events calendar, and site settings.
-- PROD-SAFE schema migration — goes in classpath:db/migration (runs everywhere).
-- Content for these tables is seeded dev-only in V4 (db/seed); prod content is
-- entered via the admin UI (Phase 4).

-- ─────────────────────────────────────────────────────────────
-- Static pages (translation-per-row, mirrors post/post_translation)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE page (
    id              BIGSERIAL PRIMARY KEY,
    slug            VARCHAR(120) NOT NULL UNIQUE,      -- 'about','poverty-relief','campus','donate','privacy','terms'
    page_type       VARCHAR(30)  NOT NULL DEFAULT 'STANDARD', -- STANDARD | LEGAL | DONATE
    status          VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED', -- DRAFT | PUBLISHED
    hero_image_url  VARCHAR(500),
    sort_order      INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE page_translation (
    id            BIGSERIAL PRIMARY KEY,
    page_id       BIGINT NOT NULL REFERENCES page(id) ON DELETE CASCADE,
    lang          VARCHAR(10)  NOT NULL,               -- 'zh-Hant' | 'en' | 'zh-Hans'
    title         VARCHAR(300) NOT NULL,
    subtitle      VARCHAR(400),
    body_markdown TEXT         NOT NULL,
    CONSTRAINT uq_page_translation UNIQUE (page_id, lang)
);

-- ─────────────────────────────────────────────────────────────
-- Events calendar (未來活動 / event-details)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE event (
    id               BIGSERIAL PRIMARY KEY,
    slug             VARCHAR(160) NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED', -- DRAFT | PUBLISHED
    starts_at        TIMESTAMPTZ,
    ends_at          TIMESTAMPTZ,
    location_text    VARCHAR(200),
    cover_image_url  VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_event_status_starts_at ON event (status, starts_at);

CREATE TABLE event_translation (
    id            BIGSERIAL PRIMARY KEY,
    event_id      BIGINT NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    lang          VARCHAR(10)  NOT NULL,
    title         VARCHAR(300) NOT NULL,
    summary       VARCHAR(600),
    body_markdown TEXT         NOT NULL,
    CONSTRAINT uq_event_translation UNIQUE (event_id, lang)
);

-- ─────────────────────────────────────────────────────────────
-- Site settings — footer/masthead contact + org facts (site-wide data,
-- not page prose). Simple key/value so the footer stops being hardcoded.
-- ─────────────────────────────────────────────────────────────
CREATE TABLE site_setting (
    key    VARCHAR(80) PRIMARY KEY,
    value  TEXT NOT NULL
);
