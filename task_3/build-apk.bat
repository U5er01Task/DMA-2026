@echo off
rem Сборка APK из итогового манифеста (практика 50) и вывод того, что видит PackageManager
if "%ANDROID_HOME%"=="" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "AAPT2=%ANDROID_HOME%\build-tools\35.0.0\aapt2.exe"
set "ANDROID_JAR=%ANDROID_HOME%\platforms\android-35\android.jar"

if not exist build mkdir build
"%AAPT2%" compile --dir res -o build\res.zip || exit /b 1
"%AAPT2%" link -I "%ANDROID_JAR%" --manifest manifests\block5\p50_production_fintech.xml ^
    -o build\fintech.apk build\res.zip ^
    --min-sdk-version 24 --target-sdk-version 35 --version-code 1 --version-name 1.0 || exit /b 1

rem UTF-8 в консоли, чтобы русское название из values-ru выводилось нормально
chcp 65001 > nul
echo === build\fintech.apk ===
"%AAPT2%" dump badging build\fintech.apk
