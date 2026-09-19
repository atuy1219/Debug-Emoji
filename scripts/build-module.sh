#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MODULE_SRC="$ROOT/module"
BUILD_DIR="$ROOT/build"
STAGE="$BUILD_DIR/stage"

NOTO_TAG="${NOTO_TAG:-v2.051}"
MAIN_SHA='72a635cb3d2f3524c51620cdde406b217204e8a6a06c6a096ff8ed4b5fd6e27b'
FLAGS_SHA='29779e6015811e747dfeafa418f4d3bd0824c5e045b5e489b359f7b69d0bb52c'

MAIN_URL="https://raw.githubusercontent.com/googlefonts/noto-emoji/${NOTO_TAG}/fonts/NotoColorEmoji.ttf"
FLAGS_URL="https://raw.githubusercontent.com/googlefonts/noto-emoji/${NOTO_TAG}/fonts/NotoColorEmoji-flagsonly.ttf"

VERSION="$(sed -n 's/^version=//p' "$MODULE_SRC/module.prop" | head -n1)"
[ -n "$VERSION" ] || { echo 'version not found in module.prop' >&2; exit 1; }

OUT="$BUILD_DIR/OnePlus13_NotoEmoji_MountFix_v${VERSION}.zip"

rm -rf "$BUILD_DIR"
mkdir -p "$STAGE"
cp -a "$MODULE_SRC/." "$STAGE/"
mkdir -p "$STAGE/system/fonts"
rm -f "$STAGE/system/fonts/.gitkeep"

echo "Downloading Noto Emoji ${NOTO_TAG}..."
curl -fL --retry 3 --retry-delay 2 -o "$STAGE/system/fonts/NotoColorEmoji.ttf" "$MAIN_URL"
curl -fL --retry 3 --retry-delay 2 -o "$STAGE/system/fonts/NotoColorEmojiFlags.ttf" "$FLAGS_URL"

printf '%s  %s\n' "$MAIN_SHA" "$STAGE/system/fonts/NotoColorEmoji.ttf" | sha256sum -c -
printf '%s  %s\n' "$FLAGS_SHA" "$STAGE/system/fonts/NotoColorEmojiFlags.ttf" | sha256sum -c -

chmod 0755 "$STAGE/customize.sh" "$STAGE/action.sh" "$STAGE/post-mount.sh"
chmod 0644 "$STAGE/module.prop"            "$STAGE/system/fonts/NotoColorEmoji.ttf"            "$STAGE/system/fonts/NotoColorEmojiFlags.ttf"

(
  cd "$STAGE"
  zip -qr9 "$OUT" .
)

echo
echo "Built: $OUT"
sha256sum "$OUT"
