# Команды проверки Android80

Требования: подключённое Android-устройство с разрешённой USB-отладкой, установленные приложения `app` и (для проверки взаимодействия между приложениями) `client`.

```bash
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r client/build/outputs/apk/debug/client-debug.apk
adb shell am start -n com.university.app/.MainActivity

# Задания в интерфейсе нумеруются как в методичке (1.1 … 6.5, затем 1…50).
# ADB-параметр task — индекс от 0 до 79.
adb shell am start -n com.university.app/.MainActivity --ei task 0
adb shell am start -n com.university.app/.MainActivity --ei task 79

# Логи и память
adb logcat -c
adb logcat -v time | grep 'A80_'
adb shell dumpsys meminfo com.university.app
adb shell dumpsys deviceidle

# Демонстрация sticky service: сначала запустить пункт T2.2 (индекс 6).
adb shell am kill com.university.app

# Тест внешнего управляющего сообщения (пункт 34 / индекс 63)
adb shell am broadcast -a com.university.app.PLAY -n com.university.app/.PlaybackControlReceiver
adb shell am broadcast -a com.university.app.PAUSE -n com.university.app/.PlaybackControlReceiver
adb shell am broadcast -a com.university.app.STOP -n com.university.app/.PlaybackControlReceiver
adb shell am broadcast -a com.university.app.MUTE -n com.university.app/.PlaybackControlReceiver
adb shell am broadcast -a com.university.app.UNMUTE -n com.university.app/.PlaybackControlReceiver

# Проверка отказа запуска неэкспортируемого сервиса (пункт 46)
adb shell am startservice -n com.university.app/.DemoService

# Симуляция батареи — на поддерживаемых устройствах
adb shell dumpsys battery set level 5
adb shell dumpsys battery reset

# Скриншот и снимок Logcat одного задания
adb shell screencap -p /sdcard/android80.png
adb pull /sdcard/android80.png ./android80.png
adb logcat -d -v time > ./android80.log
```

Автоматическая серия: `bash scripts/capture_evidence.sh 0 79`. Для заданий, требующих смены языка, реальной камеры, контактов, SMS, датчиков, WebSocket-сервера, часов ожидания или системного ANR, одной автоматической серии недостаточно; снимок делается вручную после выполнения действия. Скриншоты в исходном архиве не приложены, потому что проект в данной среде не запускался на Android.
