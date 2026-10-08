$ErrorActionPreference = 'Stop'
if (-not $env:NOVATECH_DB_URL -or -not $env:NOVATECH_DB_URL.StartsWith('jdbc:mysql://127.0.0.1:23307/novatech?')) {
    throw 'Configura la BD local aislada en puerto 23307. Este script no prueba Aiven.'
}
if (-not $env:JAVA_HOME) { throw 'Configura JAVA_HOME con la ruta del JDK.' }
$proyecto = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../../..'))
$salida = Join-Path $proyecto 'target/qa-etapa5'
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$driver = Get-ChildItem (Join-Path $proyecto 'target/novatech-1.0-SNAPSHOT/WEB-INF/lib/mysql-connector-j-*.jar') | Select-Object -First 1
if (-not $driver) { throw 'Primero compila el proyecto con Maven package.' }
$cp = (Join-Path $proyecto 'target/classes') + ';' + $driver.FullName + ';' + $salida
$sources = @(
    (Join-Path $proyecto 'docs/cumplimiento/pruebas/IntegrationEtapa4.java'),
    (Join-Path $proyecto 'docs/cumplimiento/pruebas/IntegrationExcel.java'),
    (Join-Path $PSScriptRoot 'IntegrationEtapa5.java'),
    (Join-Path $PSScriptRoot 'FixtureEtapa5.java')
)
& (Join-Path $env:JAVA_HOME 'bin/javac.exe') -encoding UTF-8 -cp $cp -d $salida @sources
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron compilar las pruebas.' }
foreach ($suite in @('IntegrationEtapa4','IntegrationExcel','IntegrationEtapa5')) {
    & (Join-Path $env:JAVA_HOME 'bin/java.exe') -cp $cp $suite
    if ($LASTEXITCODE -ne 0) { throw "Falló $suite. Revisa la salida anterior." }
}
