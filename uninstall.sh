#!/system/bin/sh

MODDIR=${1:-${0%/*}}
pm uninstall --user 0 com.iosbar.navhook >/dev/null 2>&1 || true
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
for legacy in \
  "$MODDIR/runtime" \
  "$MODDIR/system" \
  "$MODDIR/overlay-src" \
  "$MODDIR/scripts" \
  "$MODDIR/post-fs-data.sh" \
  "$MODDIR/post-mount.sh" \
  "$MODDIR/late-load.sh" \
  "$MODDIR/service.sh" \
  "$MODDIR/build_overlays.ps1" \
  "$MODDIR/disable"; do
  rm -rf "$legacy" 2>/dev/null || true
done
exit 0
