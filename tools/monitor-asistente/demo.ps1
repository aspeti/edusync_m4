# Demo local del monitor. Dia 1 termina en 0. Dia 2 termina en 1.
$ErrorActionPreference = "Continue"
$backend = Join-Path $PSScriptRoot "..\..\backend"
$cli = "com.edusync.shared.ai.application.monitoring.MonitorAsistenteCli"
$plugin = "org.codehaus.mojo:exec-maven-plugin:3.5.0:java"

function Invoke-Monitor([string]$argsLine) {
    Push-Location $backend
    try {
        mvn -q -DskipTests compile $plugin "-Dexec.mainClass=$cli" "-Dexec.args=$argsLine"
        return $LASTEXITCODE
    } finally {
        Pop-Location
    }
}

Write-Host "=== DIA 1 (sano) ==="
$code1 = Invoke-Monitor "alertas ../tools/monitor-asistente/datos/trafico_dia1.jsonl"
Write-Host "codigo: $code1"

Write-Host "=== DIA 2 (degradado) ==="
$code2 = Invoke-Monitor "alertas ../tools/monitor-asistente/datos/trafico_dia2.jsonl"
Write-Host "codigo: $code2"
Invoke-Monitor "casos-nuevos ../tools/monitor-asistente/datos/trafico_dia2.jsonl" | Out-Null

if ($code1 -ne 0 -or $code2 -ne 1) {
    Write-Host "La demo no salio como se esperaba (dia1=$code1, dia2=$code2)."
    exit 1
}
Write-Host "Demo ok: dia1=0 dia2=1. Los candidatos no entraron solos al dataset."
exit 0
