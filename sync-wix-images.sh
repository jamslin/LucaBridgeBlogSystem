#!/usr/bin/env bash
# sync-wix-images.sh — download the original LucaBridge images off Wix and push
# them into the running MinIO container. Needs ONLY Docker (no host `mc`).
# Run with the stack up:  ./sync-wix-images.sh
#
# It launches a throwaway Alpine container that shares MinIO's network, grabs the
# mc client + curl inside that container, and streams each image straight into
# the blog-media/wix/ bucket. Idempotent.
set -euo pipefail

MINIO_CONTAINER="${MINIO_CONTAINER:-lucabridge-minio}"
MINIO_USER="${MINIO_USER:-minioadmin}"
MINIO_PASSWORD="${MINIO_PASSWORD:-minioadmin}"

# shortname  <space>  full Wix media id
MANIFEST='
02a841_9edea948 02a841_9edea9488aa04bb8980d556348b8e6a9~mv2.jpg
02a841_9a6ab234 02a841_9a6ab23499454861b4a632712050a7a8~mv2.jpg
02a841_02d65a97 02a841_02d65a97ebb84ddeb6a33828b6ee1bad~mv2.jpg
02a841_ddc14c59 02a841_ddc14c596f4147339ee713efcae24a3a~mv2.jpg
02a841_ea19fbf6 02a841_ea19fbf68f9847b8ab6c5ecf58a695a8~mv2.jpg
02a841_ce489b52 02a841_ce489b52ec26452abdfd2aafbc5ddf4e~mv2.jpg
02a841_730abf01 02a841_730abf01079947569ad1d1f024e7b027~mv2.jpg
02a841_a0712079 02a841_a071207950244038ad379142aa0c38dc~mv2.jpg
02a841_b705ec69 02a841_b705ec69148a41cdbdd996edea4e017e~mv2.jpg
02a841_fa7bda0e 02a841_fa7bda0e231b424cb09d722450810159~mv2.jpg
02a841_dd10da57 02a841_dd10da5720214812a33d729093fdd2f3~mv2.jpg
02a841_9696824c 02a841_9696824c83d54f8b8f62fe89a669391e~mv2.jpg
8cb765_bb2e693c 8cb765_bb2e693cc9b34676b7fd790855865548~mv2.jpg
8cb765_22194d28 8cb765_22194d28108c4138bbf64b1650f5432f~mv2.jpg
8cb765_ec4f9760 8cb765_ec4f9760a94a425bb30152f51997ca24~mv2.jpg
8cb765_957a2a12 8cb765_957a2a1285e449c7849460f909a45a78~mv2.jpg
8cb765_746c3f6d 8cb765_746c3f6dcbd54f3995723a48184e2611~mv2.jpg
8cb765_317b4ecb 8cb765_317b4ecbddc64f6499c275cc2cbe2413~mv2.jpg
2a1a02_84cfbe35 2a1a02_84cfbe3521da4e34a5a002237fdbb811~mv2.jpg
'

echo "→ Syncing images into container '$MINIO_CONTAINER' via a throwaway Alpine helper…"
docker run --rm \
  --network "container:${MINIO_CONTAINER}" \
  -e MANIFEST="$MANIFEST" \
  -e MC_USER="$MINIO_USER" \
  -e MC_PASS="$MINIO_PASSWORD" \
  alpine sh -c '
    set -e
    apk add --no-cache curl >/dev/null
    wget -qO /usr/local/bin/mc https://dl.min.io/client/mc/release/linux-amd64/mc
    chmod +x /usr/local/bin/mc
    mc alias set m http://localhost:9000 "$MC_USER" "$MC_PASS" >/dev/null
    mc mb -p m/blog-media >/dev/null 2>&1 || true
    mc anonymous set download m/blog-media >/dev/null 2>&1 || true
    n=0
    printf "%s\n" "$MANIFEST" | while read short id; do
      [ -z "$short" ] && continue
      curl -fsSL "https://static.wixstatic.com/media/$id" | mc pipe "m/blog-media/wix/$short.jpg"
      n=$((n+1)); echo "  ✓ $short.jpg"
    done
    echo "done."
  '
echo "✓ Images available at http://localhost:9000/blog-media/wix/  (bucket is public-read)"
