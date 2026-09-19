#!/system/bin/sh
MODDIR=${0%/*}
LOG="$MODDIR/last-mount-check.log"

{
  echo '=== post-mount verification ==='
  date 2>/dev/null
  echo

  echo '[active metamodule]'
  readlink -f /data/adb/metamodule 2>/dev/null || echo '(not detected)'

  echo
  echo '[hybrid mount config]'
  if [ -f /data/adb/hybrid-mount/config.toml ]; then
    cat /data/adb/hybrid-mount/config.toml
  else
    echo '(not present)'
  fi

  echo
  echo '[module fonts]'
  ls -l "$MODDIR/system/fonts/NotoColorEmoji.ttf"         "$MODDIR/system/fonts/NotoColorEmojiFlags.ttf" 2>/dev/null
  sha256sum "$MODDIR/system/fonts/NotoColorEmoji.ttf"             "$MODDIR/system/fonts/NotoColorEmojiFlags.ttf" 2>/dev/null

  echo
  echo '[root-visible /system fonts]'
  ls -l /system/fonts/NotoColorEmoji.ttf         /system/fonts/NotoColorEmojiFlags.ttf 2>/dev/null
  sha256sum /system/fonts/NotoColorEmoji.ttf             /system/fonts/NotoColorEmojiFlags.ttf 2>/dev/null

  echo
  echo '[mountinfo]'
  grep -E ' /system/fonts |NotoColorEmoji|KSU' /proc/self/mountinfo 2>/dev/null | head -120
} > "$LOG" 2>&1
