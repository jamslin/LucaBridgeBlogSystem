CREATE TABLE IF NOT EXISTS homepage_banner (
    id BIGSERIAL PRIMARY KEY,
    image_url VARCHAR(500) NOT NULL,
    link_url VARCHAR(500),
    sort_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    title_zh_hant VARCHAR(250) NOT NULL,
    subtitle_zh_hant VARCHAR(500),
    button_label_zh_hant VARCHAR(100),
    title_en VARCHAR(250),
    subtitle_en VARCHAR(500),
    button_label_en VARCHAR(100),
    title_zh_hans VARCHAR(250),
    subtitle_zh_hans VARCHAR(500),
    button_label_zh_hans VARCHAR(100),
    CONSTRAINT homepage_banner_schedule_valid CHECK (ends_at IS NULL OR starts_at IS NULL OR ends_at >= starts_at),
    CONSTRAINT homepage_banner_sort_nonnegative CHECK (sort_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_homepage_banner_display ON homepage_banner (active, sort_order);
