#!/system/bin/sh
MODDIR=${0%/*}

EXPECTED_MAIN='72a635cb3d2f3524c51620cdde406b217204e8a6a06c6a096ff8ed4b5fd6e27b'
EXPECTED_FLAGS='29779e6015811e747dfeafa418f4d3bd0824c5e045b5e489b359f7b69d0bb52c'

echo '=== OnePlus 13 Noto Emoji Mount Fix v2.4.0 ==='
echo

echo '[active metamodule]'
if [ -L /data/adb/metamodule ] || [ -d /data/adb/metamodule ]; then
  readlink -f /data/adb/metamodule 2>/dev/null || echo /data/adb/metamodule
  [ -f /data/adb/metamodule/module.prop ] &&     grep -E '^(id|name|version|versionCode|metamodule)=' /data/adb/metamodule/module.prop 2>/dev/null
else
  echo 'NOT DETECTED'
fi

echo
echo '[Hybrid Mount config]'
cat /data/adb/hybrid-mount/config.toml 2>/dev/null || echo '(not present)'

echo
echo '[module copy]'
ls -lh "$MODDIR/system/fonts/NotoColorEmoji.ttf"        "$MODDIR/system/fonts/NotoColorEmojiFlags.ttf" 2>/dev/null
sha256sum "$MODDIR/system/fonts/NotoColorEmoji.ttf"           "$MODDIR/system/fonts/NotoColorEmojiFlags.ttf" 2>/dev/null

echo
echo '[root-visible /system copy]'
ls -lh /system/fonts/NotoColorEmoji.ttf        /system/fonts/NotoColorEmojiFlags.ttf 2>/dev/null
sha256sum /system/fonts/NotoColorEmoji.ttf           /system/fonts/NotoColorEmojiFlags.ttf 2>/dev/null

echo
echo '[expected]'
echo "main : $EXPECTED_MAIN"
echo "flags: $EXPECTED_FLAGS"

echo
echo '[mountinfo]'
grep -E ' /system/fonts |NotoColorEmoji|KSU' /proc/self/mountinfo 2>/dev/null | head -120

echo
echo '[post-mount log]'
cat "$MODDIR/last-mount-check.log" 2>/dev/null || echo '(no post-mount log yet)'

echo
echo '[display samples]'
echo 'JP=🇯🇵 US=🇺🇸 CN=🇨🇳 HK=🇭🇰 TW=🇹🇼'
echo 'U17=🫪 🫯 🫈 🛘 🫍 🪊 🪎'
echo
echo 'NOTE:'
echo 'KernelSU Next の Umount modules が ON の app では、'
echo 'module mount が app namespace から外され stock font が見える場合があります.'
