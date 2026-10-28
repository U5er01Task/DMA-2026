param([int]$From=0,[int]$To=79)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$out = Join-Path $root 'evidence';New-Item -ItemType Directory -Force $out|Out-Null
for($task=$From; $task -le $To; $task++) {
 adb logcat -c
 adb shell am force-stop com.university.app
 adb shell am start -n com.university.app/.MainActivity --ei task $task
 Start-Sleep -Seconds 5
 adb shell screencap -p /sdcard/a80_current.png
 adb pull /sdcard/a80_current.png (Join-Path $out ('task_{0:D2}.png' -f ($task+1))) | Out-Null
 adb logcat -d -v time | Select-String 'A80_' | Out-File -Encoding utf8 (Join-Path $out ('task_{0:D2}.log' -f ($task+1)))
}
