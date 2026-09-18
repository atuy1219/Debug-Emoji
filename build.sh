#!/usr/bin/env bash
set -euo pipefail

SDK_ROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [[ -z "$SDK_ROOT" ]]; then
  echo "ANDROID_HOME or ANDROID_SDK_ROOT is required" >&2
  exit 1
fi

API=36
BT=36.0.0
ANDROID_JAR="$SDK_ROOT/platforms/android-$API/android.jar"
TOOLS="$SDK_ROOT/build-tools/$BT"
OUT="${1:-build}"

rm -rf "$OUT"
mkdir -p "$OUT/classes" "$OUT/dex"

javac -source 8 -target 8 -encoding UTF-8   -cp "$ANDROID_JAR"   -d "$OUT/classes"   src/com/atuy1219/debugemoji/MainActivity.java

jar cf "$OUT/classes.jar" -C "$OUT/classes" .

"$TOOLS/d8" --min-api 26 --lib "$ANDROID_JAR"   --output "$OUT/dex" "$OUT/classes.jar"

"$TOOLS/aapt2" link   --manifest AndroidManifest.xml   -I "$ANDROID_JAR"   -o "$OUT/unsigned.apk"

cp "$OUT/unsigned.apk" "$OUT/with-dex.apk"
(
  cd "$OUT/dex"
  zip -q -j "../with-dex.apk" classes.dex
)

"$TOOLS/zipalign" -f 4 "$OUT/with-dex.apk" "$OUT/aligned.apk"

if [[ ! -f "$OUT/debug.keystore" ]]; then
  keytool -genkeypair -noprompt     -keystore "$OUT/debug.keystore"     -storepass android -keypass android     -alias androiddebugkey     -dname "CN=Android Debug,O=Debug Emoji,C=JP"     -keyalg RSA -keysize 2048 -validity 10000
fi

"$TOOLS/apksigner" sign   --ks "$OUT/debug.keystore"   --ks-key-alias androiddebugkey   --ks-pass pass:android   --key-pass pass:android   --out "$OUT/Debug-Emoji.apk"   "$OUT/aligned.apk"

"$TOOLS/apksigner" verify --verbose "$OUT/Debug-Emoji.apk"
sha256sum "$OUT/Debug-Emoji.apk"
