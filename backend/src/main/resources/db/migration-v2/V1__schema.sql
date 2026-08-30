-- LucaBridge schema v2 — greenfield rebuild.
--
-- Conventions
--   snake_case, singular table names.
--   Translatable text lives in a 1:1 <table>_text side table (PK = FK, ON DELETE CASCADE)
--   with flattened locale-prefixed columns. tc_* is the base language and is NOT NULL;
--   en_* / sc_* are nullable and fall back to tc_* in one shared resolver in the app.
--   Booleans are is_*, timestamps are *_at and always timestamptz.
--   Enumerated values are varchar + CHECK, never Postgres enums (far easier to alter).
--
-- Publish contract — identical columns and meaning on blog, event, job:
--   status        DRAFT never public, whatever the window says
--   publish_at    null = no lower bound
--   unpublish_at  null = no upper bound
--   published_at  first publish; display date and sort key
--   Visibility is decided at READ TIME in the public query. There is deliberately no
--   scheduler: a missed or double-run job silently corrupts visibility, and the cluster
--   has no CPU headroom for another pod.

-- ---------------------------------------------------------------------------
-- shared helpers
-- ---------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------------------------------------------------------------------------
-- users and access
-- ---------------------------------------------------------------------------

CREATE TABLE app_user (
    id            bigserial PRIMARY KEY,
    username      varchar(80)  NOT NULL UNIQUE,
    email         varchar(320),
    password_hash varchar(100) NOT NULL,
    display_name  varchar(120),
    -- disable rather than delete, so audit_log.actor_id always resolves
    is_active     boolean      NOT NULL DEFAULT true,
    last_login_at timestamptz,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    updated_at    timestamptz  NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_app_user_updated BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE user_role (
    user_id bigint      NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role    varchar(20) NOT NULL CHECK (role IN ('ADMIN','EDITOR')),
    PRIMARY KEY (user_id, role)
);

-- ---------------------------------------------------------------------------
-- media — one library for every image on the site
-- ---------------------------------------------------------------------------

CREATE TABLE media (
    id          bigserial PRIMARY KEY,
    s3_key      varchar(500)  NOT NULL UNIQUE,   -- object key in MinIO/S3
    url         varchar(1000) NOT NULL,          -- public URL
    file_name   varchar(300),
    mime_type   varchar(100),
    byte_size   bigint,
    width       integer,                          -- layout without fetching the file
    height      integer,
    checksum    varchar(64),                      -- dedupe re-uploads of the same bytes
    uploaded_by bigint REFERENCES app_user(id) ON DELETE SET NULL,
    created_at  timestamptz   NOT NULL DEFAULT now()
);

-- Alt text is nullable on purpose: a decorative image legitimately has empty alt.
-- Requiring it is enforced at the point of use, not in the catalogue.
CREATE TABLE media_text (
    media_id   bigint PRIMARY KEY REFERENCES media(id) ON DELETE CASCADE,
    tc_alt     varchar(300),
    en_alt     varchar(300),
    sc_alt     varchar(300),
    tc_caption varchar(500),
    en_caption varchar(500),
    sc_caption varchar(500)
);

-- ---------------------------------------------------------------------------
-- company — single row, replaces the old key/value settings table
-- ---------------------------------------------------------------------------

CREATE TABLE company (
    id             smallint PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    charity_reg_no varchar(40),
    founded_year   integer,
    phone          varchar(40),
    email          varchar(320),
    logo_media_id  bigint REFERENCES media(id) ON DELETE SET NULL,
    instagram_url  varchar(500),
    facebook_url   varchar(500),
    youtube_url    varchar(500),
    updated_by     bigint REFERENCES app_user(id) ON DELETE SET NULL,
    updated_at     timestamptz NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_company_updated BEFORE UPDATE ON company
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE company_text (
    company_id      smallint PRIMARY KEY REFERENCES company(id) ON DELETE CASCADE,
    tc_name         varchar(200) NOT NULL,
    en_name         varchar(200),
    sc_name         varchar(200),
    tc_tagline      varchar(300),
    en_tagline      varchar(300),
    sc_tagline      varchar(300),
    tc_about        text,
    en_about        text,
    sc_about        text,
    tc_address      varchar(500),
    en_address      varchar(500),
    sc_address      varchar(500),
    tc_office_hours varchar(300),
    en_office_hours varchar(300),
    sc_office_hours varchar(300)
);

-- ---------------------------------------------------------------------------
-- service — one taxonomy: home chips, home cards, services page, blog tagging
-- ---------------------------------------------------------------------------

CREATE TABLE service (
    id             bigserial PRIMARY KEY,
    code           varchar(50) NOT NULL UNIQUE,   -- stable identifier used in code and URLs
    icon_media_id  bigint REFERENCES media(id) ON DELETE SET NULL,
    sort_order     integer     NOT NULL DEFAULT 0,
    is_active      boolean     NOT NULL DEFAULT true
);

CREATE TABLE service_text (
    service_id     bigint PRIMARY KEY REFERENCES service(id) ON DELETE CASCADE,
    tc_name        varchar(120) NOT NULL,
    en_name        varchar(120),
    sc_name        varchar(120),
    tc_description varchar(500),
    en_description varchar(500),
    sc_description varchar(500)
);

-- ---------------------------------------------------------------------------
-- blog
-- ---------------------------------------------------------------------------

CREATE TABLE blog (
    id             bigserial PRIMARY KEY,
    slug           varchar(200) NOT NULL UNIQUE,
    service_id     bigint REFERENCES service(id) ON DELETE SET NULL,
    cover_media_id bigint REFERENCES media(id) ON DELETE SET NULL,
    author_id      bigint REFERENCES app_user(id) ON DELETE SET NULL,
    read_minutes   integer,
    gallery_layout varchar(20)  NOT NULL DEFAULT 'NONE'
                   CHECK (gallery_layout IN ('NONE','CAROUSEL','GRID','MASONRY')),
    status         varchar(20)  NOT NULL DEFAULT 'DRAFT'
                   CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    publish_at     timestamptz,
    unpublish_at   timestamptz,
    published_at   timestamptz,
    created_by     bigint REFERENCES app_user(id) ON DELETE SET NULL,
    updated_by     bigint REFERENCES app_user(id) ON DELETE SET NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    deleted_at     timestamptz,
    CONSTRAINT blog_window_order CHECK (unpublish_at IS NULL OR publish_at IS NULL
                                        OR unpublish_at > publish_at)
);
CREATE TRIGGER trg_blog_updated BEFORE UPDATE ON blog
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE blog_text (
    blog_id    bigint PRIMARY KEY REFERENCES blog(id) ON DELETE CASCADE,
    tc_title   varchar(300) NOT NULL,
    en_title   varchar(300),
    sc_title   varchar(300),
    tc_summary varchar(600),
    en_summary varchar(600),
    sc_summary varchar(600),
    tc_body    text,
    en_body    text,
    sc_body    text
);

CREATE TABLE blog_gallery (
    blog_id    bigint  NOT NULL REFERENCES blog(id) ON DELETE CASCADE,
    media_id   bigint  NOT NULL REFERENCES media(id) ON DELETE RESTRICT,
    sort_order integer NOT NULL DEFAULT 0,
    PRIMARY KEY (blog_id, media_id)
);

-- ---------------------------------------------------------------------------
-- event
-- ---------------------------------------------------------------------------

CREATE TABLE event (
    id                     bigserial PRIMARY KEY,
    slug                   varchar(200) NOT NULL UNIQUE,
    cover_media_id         bigint REFERENCES media(id) ON DELETE SET NULL,
    gallery_layout         varchar(20)  NOT NULL DEFAULT 'NONE'
                           CHECK (gallery_layout IN ('NONE','CAROUSEL','GRID','MASONRY')),
    -- when the event HAPPENS. Never confuse with the publish window below.
    starts_at              timestamptz,
    ends_at                timestamptz,
    venue_map_url          varchar(1000),
    -- registration
    is_registerable        boolean      NOT NULL DEFAULT false,
    registration_opens_at  timestamptz,
    registration_closes_at timestamptz,
    capacity               integer,     -- null = unlimited; over capacity -> WAITLIST
    -- publish contract
    status                 varchar(20)  NOT NULL DEFAULT 'DRAFT'
                           CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    publish_at             timestamptz,
    unpublish_at           timestamptz,
    published_at           timestamptz,
    created_by             bigint REFERENCES app_user(id) ON DELETE SET NULL,
    updated_by             bigint REFERENCES app_user(id) ON DELETE SET NULL,
    created_at             timestamptz  NOT NULL DEFAULT now(),
    updated_at             timestamptz  NOT NULL DEFAULT now(),
    deleted_at             timestamptz,
    CONSTRAINT event_window_order CHECK (unpublish_at IS NULL OR publish_at IS NULL
                                         OR unpublish_at > publish_at),
    CONSTRAINT event_time_order CHECK (ends_at IS NULL OR starts_at IS NULL
                                       OR ends_at >= starts_at),
    CONSTRAINT event_reg_order CHECK (registration_closes_at IS NULL
                                      OR registration_opens_at IS NULL
                                      OR registration_closes_at > registration_opens_at)
);
CREATE TRIGGER trg_event_updated BEFORE UPDATE ON event
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE event_text (
    event_id   bigint PRIMARY KEY REFERENCES event(id) ON DELETE CASCADE,
    tc_title   varchar(300) NOT NULL,
    en_title   varchar(300),
    sc_title   varchar(300),
    tc_summary varchar(600),
    en_summary varchar(600),
    sc_summary varchar(600),
    tc_body    text,
    en_body    text,
    sc_body    text,
    tc_venue   varchar(300),
    en_venue   varchar(300),
    sc_venue   varchar(300)
);

CREATE TABLE event_gallery (
    event_id   bigint  NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    media_id   bigint  NOT NULL REFERENCES media(id) ON DELETE RESTRICT,
    sort_order integer NOT NULL DEFAULT 0,
    PRIMARY KEY (event_id, media_id)
);

-- ---------------------------------------------------------------------------
-- event registration
-- ---------------------------------------------------------------------------

CREATE TABLE referral_group (
    id         bigserial PRIMARY KEY,
    code       varchar(50) NOT NULL UNIQUE,
    sort_order integer     NOT NULL DEFAULT 0,
    is_active  boolean     NOT NULL DEFAULT true
);

CREATE TABLE referral_group_text (
    referral_group_id bigint PRIMARY KEY REFERENCES referral_group(id) ON DELETE CASCADE,
    tc_name           varchar(200) NOT NULL,
    en_name           varchar(200),
    sc_name           varchar(200)
);

-- Fixed columns, not a form builder: the fields are the same across events, and fixed
-- columns are queryable, exportable and validatable in a way EAV never is.
-- PERSONAL DATA under the PDPO. ADMIN-only read. Retention policy required.
CREATE TABLE event_registration (
    id                    bigserial PRIMARY KEY,
    -- RESTRICT, not CASCADE: deleting an event must never silently destroy the
    -- registrations people submitted to it.
    event_id              bigint      NOT NULL REFERENCES event(id) ON DELETE RESTRICT,
    reference_code        varchar(20) NOT NULL UNIQUE,   -- shown to the registrant, used at check-in
    referral_group_id     bigint REFERENCES referral_group(id) ON DELETE SET NULL,
    referral_group_other  varchar(200),                  -- free text when 其他 is chosen
    full_name             varchar(200) NOT NULL,
    gender                varchar(1) CHECK (gender IN ('M','F')),
    birth_year            integer CHECK (birth_year BETWEEN 1900 AND 2100),  -- year only, never a full DOB
    email                 varchar(320) NOT NULL,
    phone                 varchar(40)  NOT NULL,
    postal_address        varchar(500),
    is_whatsapp_confirmed boolean      NOT NULL DEFAULT false,
    status                varchar(20)  NOT NULL DEFAULT 'PENDING'
                          CHECK (status IN ('PENDING','CONFIRMED','WAITLIST','CANCELLED','ATTENDED')),
    locale                varchar(2)   NOT NULL DEFAULT 'tc' CHECK (locale IN ('tc','en','sc')),
    -- Consent TEXT lives in the repo i18n files; only the version is stored here, so we
    -- can still prove which wording each person agreed to after the text changes.
    terms_accepted_at     timestamptz  NOT NULL,
    terms_version         varchar(20)  NOT NULL,
    privacy_consent_at    timestamptz  NOT NULL,
    privacy_version       varchar(20)  NOT NULL,
    -- separate, OPTIONAL opt-in. Never bundled into the mandatory consent above.
    is_friends_opt_in     boolean      NOT NULL DEFAULT false,
    extra_answers         jsonb,       -- escape hatch for a one-off question, no migration
    admin_note            text,
    submitted_at          timestamptz  NOT NULL DEFAULT now(),
    updated_by            bigint REFERENCES app_user(id) ON DELETE SET NULL,
    updated_at            timestamptz  NOT NULL DEFAULT now()
);
CREATE TRIGGER trg_event_registration_updated BEFORE UPDATE ON event_registration
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- one registration per email per event
CREATE UNIQUE INDEX uq_event_registration_email
    ON event_registration (event_id, lower(email));

-- ---------------------------------------------------------------------------
-- job
-- ---------------------------------------------------------------------------

CREATE TABLE job (
    id              bigserial PRIMARY KEY,
    slug            varchar(200) NOT NULL UNIQUE,
    employment_type varchar(30),
    department      varchar(120),
    posted_at       timestamptz,
    -- application deadline, ALSO enforced in the public query so expired roles drop off
    closes_at       timestamptz,
    apply_email     varchar(320),
    apply_url       varchar(1000),
    status          varchar(20)  NOT NULL DEFAULT 'DRAFT'
                    CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    publish_at      timestamptz,
    unpublish_at    timestamptz,
    published_at    timestamptz,
    created_by      bigint REFERENCES app_user(id) ON DELETE SET NULL,
    updated_by      bigint REFERENCES app_user(id) ON DELETE SET NULL,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),
    deleted_at      timestamptz,
    CONSTRAINT job_window_order CHECK (unpublish_at IS NULL OR publish_at IS NULL
                                       OR unpublish_at > publish_at)
);
CREATE TRIGGER trg_job_updated BEFORE UPDATE ON job
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE job_text (
    job_id      bigint PRIMARY KEY REFERENCES job(id) ON DELETE CASCADE,
    tc_title    varchar(300) NOT NULL,
    en_title    varchar(300),
    sc_title    varchar(300),
    tc_body     text,
    en_body     text,
    sc_body     text,
    tc_location varchar(300),
    en_location varchar(300),
    sc_location varchar(300)
);

-- ---------------------------------------------------------------------------
-- home page blocks
-- ---------------------------------------------------------------------------

-- The table supplies CONTENT only. React owns layout. If anyone ever asks for a
-- column_width or background_colour column here, that is the moment this becomes an
-- accidental page builder — say no.
CREATE TABLE home_block (
    id           bigserial PRIMARY KEY,
    slot         varchar(20) NOT NULL
                 CHECK (slot IN ('HERO','STAT','FEATURED','SUPPORT','VOLUNTEER','QUICK_LINK')),
    media_id     bigint REFERENCES media(id) ON DELETE SET NULL,
    blog_id      bigint REFERENCES blog(id) ON DELETE SET NULL,  -- FEATURED pins a story
    link_url     varchar(1000),
    sort_order   integer     NOT NULL DEFAULT 0,
    is_active    boolean     NOT NULL DEFAULT true,
    publish_at   timestamptz,
    unpublish_at timestamptz,
    updated_by   bigint REFERENCES app_user(id) ON DELETE SET NULL,
    created_at   timestamptz NOT NULL DEFAULT now(),
    updated_at   timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT home_block_window_order CHECK (unpublish_at IS NULL OR publish_at IS NULL
                                              OR unpublish_at > publish_at)
);
CREATE TRIGGER trg_home_block_updated BEFORE UPDATE ON home_block
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE home_block_text (
    home_block_id   bigint PRIMARY KEY REFERENCES home_block(id) ON DELETE CASCADE,
    tc_title        varchar(300) NOT NULL,
    en_title        varchar(300),
    sc_title        varchar(300),
    tc_subtitle     varchar(600),
    en_subtitle     varchar(600),
    sc_subtitle     varchar(600),
    tc_button_label varchar(100),
    en_button_label varchar(100),
    sc_button_label varchar(100)
);

-- ---------------------------------------------------------------------------
-- supporting
-- ---------------------------------------------------------------------------

CREATE TABLE audit_log (
    id          bigserial PRIMARY KEY,
    actor_id    bigint REFERENCES app_user(id) ON DELETE SET NULL,
    action      varchar(50) NOT NULL,
    entity_type varchar(50) NOT NULL,
    entity_id   bigint,
    changes     jsonb,
    at          timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE url_redirect (
    id           bigserial PRIMARY KEY,
    from_path    varchar(500) NOT NULL UNIQUE,
    to_path      varchar(500) NOT NULL,
    is_permanent boolean      NOT NULL DEFAULT true,
    created_at   timestamptz  NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------------
-- indexes
-- ---------------------------------------------------------------------------

-- visibility predicates: the public queries filter on these four together
CREATE INDEX idx_blog_visibility  ON blog  (status, publish_at, unpublish_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_event_visibility ON event (status, publish_at, unpublish_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_visibility   ON job   (status, publish_at, unpublish_at, closes_at) WHERE deleted_at IS NULL;

CREATE INDEX idx_blog_published   ON blog  (published_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_event_starts     ON event (starts_at)         WHERE deleted_at IS NULL;
CREATE INDEX idx_blog_service     ON blog  (service_id);

-- foreign keys used in joins and in the image reference count
CREATE INDEX idx_blog_cover       ON blog  (cover_media_id);
CREATE INDEX idx_event_cover      ON event (cover_media_id);
CREATE INDEX idx_blog_gallery_med ON blog_gallery  (media_id);
CREATE INDEX idx_event_gallery_med ON event_gallery (media_id);
CREATE INDEX idx_home_block_media ON home_block (media_id);
CREATE INDEX idx_service_icon     ON service (icon_media_id);

CREATE INDEX idx_home_block_slot  ON home_block (slot, sort_order) WHERE is_active;
CREATE INDEX idx_reg_event_status ON event_registration (event_id, status);
CREATE INDEX idx_audit_entity     ON audit_log (entity_type, entity_id, at DESC);
