#!/system/bin/sh

SKIPUNZIP=0

ui_print() { echo "$@" >&2; }

MODDIR=${MODPATH:-${0%/*}}
HOOK_APK="$MODDIR/runtime/iosbar-navhook.apk"

# 0.3.x installed overlay APKs as data packages in addition to mounting their
# files. Removing only the old module directory leaves those package records
# alive until the next boot, which can make SystemUI resolve stale resource
# types and send LSPosed into safe mode. Uninstall only data-backed copies;
# never disable or remove a platform overlay from a read-only partition.
for legacy_package in \
  com.android.internal.systemui.navbar.gestural \
  com.iosbar.oplus.width \
  com.iosbar.systemui.dimen; do
  legacy_path=$(pm path "$legacy_package" 2>/dev/null | sed -n 's/^package://p' | head -n 1)
  case "$legacy_path" in
    /data/*|/mnt/expand/*)
      pm uninstall --user 0 "$legacy_package" >/dev/null 2>&1 || true
      ;;
  esac
done

# 0.3.x left RRO and mount payloads behind when a module was upgraded in place.
# Remove only this module's legacy paths; the actual overlay packages are never touched.
for legacy in \
  "$MODDIR/system" \
  "$MODDIR/overlay-src" \
  "$MODDIR/scripts" \
  "$MODDIR/post-fs-data.sh" \
  "$MODDIR/post-mount.sh" \
  "$MODDIR/late-load.sh" \
  "$MODDIR/service.sh" \
  "$MODDIR/build_overlays.ps1"; do
  rm -rf "$legacy" 2>/dev/null || true
done
rm -f "$MODDIR/disable" 2>/dev/null || true

if [ ! -s "$HOOK_APK" ]; then
  abort "The SystemUI hook APK is missing."
fi

ui_print "iOS-style gesture bar (SystemUI hook-only)"
ui_print "Target scope: com.android.systemui"
ui_print "No framework or navigation-mode overlay is replaced."

if [ -s "$HOOK_APK" ]; then
  hook_size=$(wc -c < "$HOOK_APK" 2>/dev/null | tr -d '[:space:]')
  case "$hook_size" in
    ''|*[!0-9]*) ui_print "! Hook APK size unavailable; RRO-only mode" ;;
    *)
      if cat "$HOOK_APK" | pm install -r -d -S "$hook_size" >/dev/null 2>&1; then
        ui_print "API 102 SystemUI geometry + transient-scrim hook installed"
      else
        abort "API 102 hook install failed. The module was not enabled."
      fi
      ;;
  esac
else
  abort "API 102 hook not bundled."
fi

set_perm_recursive "$MODDIR" 0 0 0755 0644
[ ! -f "$MODDIR/uninstall.sh" ] || set_perm "$MODDIR/uninstall.sh" 0 0 0755
mkdir -p "$MODDIR/runtime" 2>/dev/null
chmod 0700 "$MODDIR/runtime" 2>/dev/null
exit 0
