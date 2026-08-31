-- event was missing service_id — the whole reason "category" was renamed to "service" was one
-- taxonomy shared across blog and event, and the design puts a service chip on every event card.
--
-- SET NULL, not RESTRICT: losing a category tag when a service is removed is cosmetic, unlike
-- the media FKs where losing the reference would mean a broken image. Matches blog.service_id.

ALTER TABLE event ADD COLUMN service_id bigint REFERENCES service(id) ON DELETE SET NULL;

CREATE INDEX idx_event_service ON event (service_id);
