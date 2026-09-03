#!/usr/bin/env bash
# Put a backup back. This REPLACES the current database contents and re-uploads the
# saved image files, so run it on a stack you are happy to overwrite.
#
#   ./restore-data.sh            <- backups/latest/
#   ./restore-data.sh before-v6  <- backups/before-v6/
set -euo pipefail
cd "$(dirname "$0")"

NAME="${1:-latest}"
BUCKET="${STORAGE_BUCKET:-blog-media}"

[ -f "backups/$NAME/db.dump" ] || {
  echo "[X] backups/$NAME/db.dump not found. Nothing to restore." >&2
  exit 1
}

echo "Restoring backups/$NAME — this OVERWRITES the current database and media."
read -r -p "Type RESTORE to continue: " confirm
[ "$confirm" = "RESTORE" ] || { echo "Cancelled. Nothing changed."; exit 1; }

if ! docker compose ps --status running --services | grep -qx postgres; then
  echo "[X] Postgres is not running. Start the stack first, then run this again." >&2
  exit 1
fi

echo "[1/2] Database..."
# --clean --if-exists drops each object before recreating it, so restoring over a
# populated database works. Flyway's own history table rides along in the dump,
# which is what keeps the schema version consistent with the data. pg_restore exits
# non-zero on the harmless "does not exist" notices --clean produces on a fresh
# database, so its status is reported rather than aborting the media half.
docker compose exec -T postgres pg_restore -U lucabridge -d lucabridge \
  --clean --if-exists --no-owner "/backups/$NAME/db.dump" \
  || echo "[!] pg_restore reported errors — often just --clean notices. Check the site."

echo "[2/2] Media files..."
docker compose --profile tools run --rm mc \
  "mc mirror --overwrite /backups/$NAME/media local/$BUCKET"

echo
echo "Done. Restart the backend so it picks the data up cleanly:"
echo "  docker compose restart backend"
