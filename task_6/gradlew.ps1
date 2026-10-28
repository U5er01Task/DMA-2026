$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
if (Get-Command gradle -ErrorAction SilentlyContinue) { & gradle -p $root @args; exit $LASTEXITCODE }
$dir = Join-Path $root '.bootstrap-gradle'
$zip = Join-Path $dir 'gradle-8.10.2-bin.zip'
$exe = Join-Path $dir 'gradle-8.10.2/bin/gradle.bat'
if (-not (Test-Path $exe)) {
 New-Item -ItemType Directory -Force $dir | Out-Null
 Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-8.10.2-bin.zip' -OutFile $zip
 $hash = (Get-FileHash -Algorithm SHA256 $zip).Hash.ToLower()
 if ($hash -ne '31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26') { throw 'Gradle checksum mismatch' }
 Expand-Archive -Path $zip -DestinationPath $dir -Force
}
& $exe -p $root @args; exit $LASTEXITCODE
