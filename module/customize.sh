#!/system/bin/sh

EXPECTED_MAIN='72a635cb3d2f3524c51620cdde406b217204e8a6a06c6a096ff8ed4b5fd6e27b'
EXPECTED_FLAGS='29779e6015811e747dfeafa418f4d3bd0824c5e045b5e489b359f7b69d0bb52c'

ui_print '- OnePlus 13 Noto Emoji Mount Fix v2.4.0'
ui_print '- Noto Color Emoji 2.051'
ui_print '- system/fonts は metamodule にマウントさせます'
ui_print '- 手動 mount --bind は使用しません'
ui_print ''

MAIN="$MODPATH/system/fonts/NotoColorEmoji.ttf"
FLAGS="$MODPATH/system/fonts/NotoColorEmojiFlags.ttf"

[ -s "$MAIN" ] || abort '! NotoColorEmoji.ttf is missing from the module ZIP.'
[ -s "$FLAGS" ] || abort '! NotoColorEmojiFlags.ttf is missing from the module ZIP.'

MAIN_SHA=$(sha256sum "$MAIN" 2>/dev/null | awk '{print $1}')
FLAGS_SHA=$(sha256sum "$FLAGS" 2>/dev/null | awk '{print $1}')

[ "$MAIN_SHA" = "$EXPECTED_MAIN" ] || abort "! NotoColorEmoji.ttf hash mismatch: $MAIN_SHA"
[ "$FLAGS_SHA" = "$EXPECTED_FLAGS" ] || abort "! NotoColorEmojiFlags.ttf hash mismatch: $FLAGS_SHA"

# Old updater/runtime bind files are intentionally not used.
rm -f "$MODPATH/post-fs-data.sh" "$MODPATH/service.sh"       "$MODPATH/update-core.sh" "$MODPATH/emoji-state.env"       "$MODPATH/reboot-required" "$MODPATH/last-bind.log" 2>/dev/null

if [ "$KSU" = "true" ]; then
  if [ -L /data/adb/metamodule ] || [ -d /data/adb/metamodule ]; then
    META=$(readlink -f /data/adb/metamodule 2>/dev/null)
    ui_print "- Active metamodule: ${META:-/data/adb/metamodule}"
  else
    ui_print '!'
    ui_print '! Active KernelSU metamodule was not detected.'
    ui_print '! Install/enable a compatible metamodule before rebooting.'
    ui_print '!'
  fi
fi

set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/action.sh" 0 0 0755
set_perm "$MODPATH/post-mount.sh" 0 0 0755
set_perm "$MAIN" 0 0 0644
set_perm "$FLAGS" 0 0 0644

ui_print ''
ui_print "- main : $MAIN_SHA"
ui_print "- flags: $FLAGS_SHA"
ui_print ''
ui_print '! KernelSU Next の Umount modules が有効な app では'
ui_print '! systemless font が app namespace から外れる場合があります。'
ui_print '- Reboot is required.'
