#!/usr/bin/env bash
# Всё за один запуск: сборка, установка, разрешения, скриншоты 80 заданий, отчёт.
# Запуск из корня проекта:  bash scripts/run_all.sh
# Только часть заданий:     bash scripts/run_all.sh 0 9
# Без пересборки:           SKIP_BUILD=1 bash scripts/run_all.sh
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
FROM="${1:-0}"
TO="${2:-79}"
PKG=com.university.app
OUT="$ROOT/evidence"

# 1. adb
if ! command -v adb >/dev/null 2>&1; then
  SDK="$(sed -n 's/^sdk.dir=//p' local.properties 2>/dev/null)"
  for d in "$SDK" "$HOME/Android/Sdk"; do
    [ -x "$d/platform-tools/adb" ] && export PATH="$PATH:$d/platform-tools" && break
  done
fi
command -v adb >/dev/null 2>&1 || { echo "adb не найден. Установите: sudo apt install adb"; exit 1; }
adb start-server >/dev/null 2>&1
STATE="$(adb get-state 2>&1)"
if [ "$STATE" != "device" ]; then
  echo "Телефон не готов: $STATE"
  echo "Проверьте кабель, включите «Отладку по USB» и подтвердите запрос на телефоне."
  adb devices
  exit 1
fi
echo "Телефон: $(adb shell getprop ro.product.model | tr -d '\r')"

# 2. экран включён и не гаснет
adb shell svc power stayon usb >/dev/null 2>&1
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard >/dev/null 2>&1

# 3. сборка и установка
if [ -z "${SKIP_BUILD:-}" ]; then
  java_ok() { java -version 2>&1 | grep -Eq 'version "(1[7-9]|2[0-9])'; }
  if ! java_ok; then
    for j in /opt/android-studio*/jbr "$HOME"/android-studio*/jbr "$HOME"/.local/share/JetBrains/Toolbox/apps/*/ch-*/*/jbr /snap/android-studio/current/android-studio/jbr; do
      if [ -x "$j/bin/java" ]; then export JAVA_HOME="$j"; export PATH="$j/bin:$PATH"; break; fi
    done
  fi
  java_ok || { echo "Нужна Java 17+. Установите: sudo apt install openjdk-17-jdk"; exit 1; }
  chmod +x gradlew
  echo "Сборка и установка (первый раз может занять несколько минут)..."
  ./gradlew :app:installDebug :client:installDebug || { echo "Сборка не удалась"; exit 1; }
fi

# 4. разрешения (SEND_SMS не выдаём: задание иначе отправит настоящее SMS)
for p in POST_NOTIFICATIONS ACCESS_FINE_LOCATION ACTIVITY_RECOGNITION READ_CONTACTS READ_CALL_LOG READ_MEDIA_IMAGES; do
  adb shell pm grant "$PKG" "android.permission.$p" >/dev/null 2>&1 || echo "  не выдано: $p"
done
adb shell appops set "$PKG" SCHEDULE_EXACT_ALARM allow >/dev/null 2>&1
adb shell appops set "$PKG" USE_FULL_SCREEN_INTENT allow >/dev/null 2>&1
adb shell dumpsys deviceidle whitelist +"$PKG" >/dev/null 2>&1

# 5. скриншоты
# TASKS="29 30 31" bash scripts/run_all.sh  - только эти задания (номера с нуля)
mkdir -p "$OUT"
if [ -n "${TASKS:-}" ]; then LIST="$TASKS"; else LIST="$(seq "$FROM" "$TO")"; fi
for n in $LIST; do
  k=$(printf '%02d' $((n + 1)))
  wait_s=5
  [ "$n" = 16 ] && wait_s=18   # ANR появляется через ~10 секунд
  [ "$n" = 68 ] && wait_s=14   # будильник срабатывает через 10 секунд
  printf 'Задание %s из 80 ... ' "$k"
  for attempt in 1 2 3; do
    # закрываем окна предыдущего задания (выбор приложения, камера, файлы)
    adb shell input keyevent KEYCODE_HOME
    adb shell am force-stop "$PKG"
    sleep 1
    adb logcat -c
    adb shell am start -n "$PKG/.MainActivity" --ei task "$n" >/dev/null
    sleep "$wait_s"
    adb logcat -d -v time | grep 'A80_' > "$OUT/task_$k.log" || true
    grep -q "A80_$((n + 1)) .*START" "$OUT/task_$k.log" && break
    printf 'повтор... '
  done
  adb exec-out screencap -p > "$OUT/task_$k.png"
  adb shell cmd statusbar expand-notifications
  sleep 1
  adb exec-out screencap -p > "$OUT/task_${k}_shade.png"
  adb shell cmd statusbar collapse
  adb logcat -d -v time | grep 'A80_' > "$OUT/task_$k.log" || true
  echo "готово"
done
adb shell am force-stop "$PKG"
adb shell input keyevent KEYCODE_HOME

# 6. отчёт и папка
python3 scripts/build_report.py "$FROM" "$TO"
echo "Готово: $OUT"
command -v xdg-open >/dev/null 2>&1 && xdg-open "$OUT" >/dev/null 2>&1 &
exit 0
