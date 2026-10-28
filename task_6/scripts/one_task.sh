#!/usr/bin/env bash
# Одно задание с ручными действиями.
#   bash scripts/one_task.sh НОМЕР [секунд]
# НОМЕР как в report.md (1-80). За указанное время (по умолчанию 25 с) выполните
# действие на телефоне, скрипт покажет лог и сохранит evidence/task_NN.png, _shade.png и .log
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"; cd "$ROOT"
N="${1:?укажите номер задания 1-80}"; SEC="${2:-25}"; PKG=com.university.app
command -v adb >/dev/null 2>&1 || export PATH="$PATH:$(sed -n 's/^sdk.dir=//p' local.properties)/platform-tools:$HOME/Android/Sdk/platform-tools"
K=$(printf '%02d' "$N"); OUT="$ROOT/evidence"; mkdir -p "$OUT"
adb shell input keyevent KEYCODE_WAKEUP
adb shell input keyevent KEYCODE_HOME
adb shell am force-stop "$PKG"; sleep 1; adb logcat -c
adb shell am start -n "$PKG/.MainActivity" --ei task $((N - 1)) >/dev/null
echo "Задание $N запущено. $SEC секунд на действия на телефоне..."
timeout "$SEC" adb logcat -v time | grep --line-buffered 'A80_' | tee "$OUT/task_$K.log"
adb exec-out screencap -p > "$OUT/task_$K.png"
adb shell cmd statusbar expand-notifications; sleep 1
adb exec-out screencap -p > "$OUT/task_${K}_shade.png"
adb shell cmd statusbar collapse
echo "Сохранено: $OUT/task_$K.*"
