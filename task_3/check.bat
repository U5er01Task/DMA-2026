@echo off
rem Проверка всех манифестов утилитой aapt2 (та же проверка, что делает Gradle при сборке)
setlocal enabledelayedexpansion

if "%ANDROID_HOME%"=="" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "AAPT2=%ANDROID_HOME%\build-tools\35.0.0\aapt2.exe"
set "ANDROID_JAR=%ANDROID_HOME%\platforms\android-35\android.jar"

if not exist build mkdir build

echo === aapt2 compile: res ===
"%AAPT2%" compile --dir res -o build\res.zip
if errorlevel 1 (
    echo Resource compilation FAILED
    exit /b 1
)
echo res compiled OK
echo.

echo === aapt2 link: manifests ===
set /a OK=0
set /a FAIL=0
for /r manifests %%f in (*.xml) do (
    set "NAME=%%f"
    set "NAME=!NAME:%CD%\manifests\=!"
    "%AAPT2%" link -I "%ANDROID_JAR%" --manifest "%%f" -o build\check.apk build\res.zip ^
        --min-sdk-version 24 --target-sdk-version 35 > build\last.log 2>&1
    if !errorlevel! equ 0 (
        set /a OK+=1
        echo [ OK ] !NAME!
    ) else (
        set /a FAIL+=1
        echo [FAIL] !NAME!
        type build\last.log
    )
)
echo.
echo Checked: !OK! OK, !FAIL! FAIL
if !FAIL! gtr 0 exit /b 1
