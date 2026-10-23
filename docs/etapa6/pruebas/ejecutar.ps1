$ErrorActionPreference = 'Stop'
if (-not $env:NOVATECH_DB_URL -or -not $env:NOVATECH_DB_URL.StartsWith('jdbc:mysql://127.0.0.1:23307/novatech?')) { throw 'Solo QA local en puerto 23307. Nunca Aiven.' }
if (-not $env:JAVA_HOME) { throw 'Configura JAVA_HOME.' }
$proyecto = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$salida = Join-Path $proyecto 'target/qa-etapa6'
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$driver = Get-ChildItem (Join-Path $proyecto 'target/novatech-1.0-SNAPSHOT/WEB-INF/lib/mysql-connector-j-*.jar') | Select-Object -First 1
if (-not $driver) { throw 'Compila con Maven package primero.' }
$cp = (Join-Path $proyecto 'target/classes') + ';' + $driver.FullName + ';' + $salida
& (Join-Path $env:JAVA_HOME 'bin/javac.exe') -encoding UTF-8 -cp $cp -d $salida (Join-Path $proyecto 'docs/cumplimiento/pruebas/IntegrationEtapa4.java') (Join-Path $PSScriptRoot 'IntegrationEtapa6.java')
if ($LASTEXITCODE -ne 0) { throw 'No se compilaron las pruebas.' }
& (Join-Path $env:JAVA_HOME 'bin/java.exe') -cp $cp IntegrationEtapa6
if ($LASTEXITCODE -ne 0) { throw 'Falló IntegrationEtapa6.' }
