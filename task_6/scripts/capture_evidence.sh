#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/evidence"
mkdir -p "$OUT"
START="${1:-0}"
END="${2:-79}"
for n in $(seq "$START" "$END"); do
  printf -- '--- Task %02d ---\n' "$n"
  adb logcat -c
  adb shell am force-stop com.university.app
  adb shell am start -n com.university.app/.MainActivity --ei task "$n"
  sleep 5
  adb shell screencap -p /sdcard/a80_current.png
  adb pull /sdcard/a80_current.png "$OUT/task_$(printf '%02d' $((n+1))).png" >/dev/null
  adb logcat -d -v time | grep 'A80_' > "$OUT/task_$(printf '%02d' $((n+1))).log" || true
  sleep 1
done
echo "Evidence: $OUT"
# Для заданий с запросом разрешений, системными событиями, будильником, камерой и датчиками
# часть скриншотов придётся снять вручную.
