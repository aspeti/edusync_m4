# Retest de AI-SEC-007 y AI-SEC-008 (red team del asistente de producto).
#
# Paso 1 (siempre): prueba que la mitigacion es la que corta el ataque.
#   a) Reemplaza temporalmente EjecutarConsultaAgenteService.java por la version del
#      commit base (sin PoliticaAlcanceAgente) y corre el gate determinista.
#      Se espera ROJO en RT-EXFIL-002, RT-AUTH-003 y RT-TENANT-001.
#   b) Restaura el archivo (siempre, aunque algo falle) y vuelve a correr el gate.
#      Se espera VERDE.
# Paso 2 (opcional, -Live): repite los ataques contra el backend local con Ollama.
#   Requiere el backend RECOMPILADO y REINICIADO, y EDUSYNC_REDTEAM_JWT con un JWT
#   de un usuario PROFESOR sintetico.
#
# Uso (desde tools\red-team-agent):
#   .\retest_ai_sec.ps1
#   .\retest_ai_sec.ps1 -Live
#   .\retest_ai_sec.ps1 -BaseCommit 77e74f9
#
# Nunca ataca otra cosa que localhost. No escribe secretos en el repo.

param(
    [string]$BaseCommit = "77e74f9",
    [switch]$Live
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
$repo = (Resolve-Path "..\..").Path
$backend = Join-Path $repo "backend"
$rel = "backend/src/main/java/com/edusync/shared/ai/application/service/EjecutarConsultaAgenteService.java"
$servicio = Join-Path $repo ($rel -replace "/", "\")
$marca = Get-Date -Format "yyyy-MM-dd_HHmmss"
$salida = Join-Path $PSScriptRoot "evidencia\retest"
New-Item -ItemType Directory -Force -Path $salida | Out-Null
$respaldo = Join-Path $env:TEMP "EjecutarConsultaAgenteService.$marca.java"
$ids = @("RT-EXFIL-002", "RT-AUTH-003", "RT-TENANT-001")

function Correr-Gate([string]$etiqueta) {
    $log = Join-Path $salida "$etiqueta`_$marca.log"
    Push-Location $backend
    # Maven escribe avisos en stderr; con ErrorActionPreference=Stop, PowerShell 5
    # los convierte en error y corta el script. cmd /c une stderr a stdout antes.
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        cmd /c "mvn -Dtest=RedTeamAssistantCatalogWebMvcTest test 2>&1" | Out-File -FilePath $log -Encoding utf8
        $codigo = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevEap
        Pop-Location
    }
    $md = Join-Path $backend "target\redteam\ultimo.md"
    if (Test-Path $md) {
        Copy-Item $md (Join-Path $salida "$etiqueta`_$marca.md") -Force
    }
    return $codigo
}

# --- Paso 1a: sin mitigacion ------------------------------------------------
$hashOriginal = (Get-FileHash $servicio -Algorithm SHA256).Hash
Copy-Item $servicio $respaldo -Force
Write-Host "Respaldo del servicio: $respaldo"
try {
    Push-Location $repo
    # cmd /c escribe los bytes tal cual (PowerShell 5 con '>' escribiria UTF-16)
    cmd /c "git show ${BaseCommit}:$rel > `"$servicio`""
    Pop-Location
    if ((Get-FileHash $servicio -Algorithm SHA256).Hash -eq $hashOriginal) {
        throw "La version $BaseCommit es igual a la actual: no hay mitigacion que quitar."
    }
    Write-Host "== Paso 1a: gate SIN PoliticaAlcanceAgente ($BaseCommit). Se espera ROJO."
    $codigoSin = Correr-Gate "sin_mitigacion"
} finally {
    Copy-Item $respaldo $servicio -Force
    # Copy-Item conserva la fecha vieja del archivo: Maven veria el .class (mas nuevo,
    # compilado SIN mitigacion) como vigente y no recompilaria. Se fuerza la fecha.
    (Get-Item $servicio).LastWriteTime = Get-Date
    if ((Get-FileHash $servicio -Algorithm SHA256).Hash -ne $hashOriginal) {
        Write-Error "No se pudo restaurar $servicio. Copia manual desde $respaldo"
        exit 3
    }
    Write-Host "Servicio restaurado (hash verificado)."
}

$logSin = Get-Content (Join-Path $salida "sin_mitigacion_$marca.log") -Raw
$rojos = $ids | Where-Object { $logSin -match [regex]::Escape($_) }
Write-Host ("  exit={0}  ataques que ganaron sin mitigacion: {1}" -f $codigoSin, ($rojos -join ", "))

# --- Paso 1b: con mitigacion ------------------------------------------------
Write-Host "== Paso 1b: gate CON PoliticaAlcanceAgente. Se espera VERDE."
$codigoCon = Correr-Gate "con_mitigacion"
$logCon = Get-Content (Join-Path $salida "con_mitigacion_$marca.log") -Raw
if ($logCon -notmatch "Compiling \d+ source files") {
    Write-Warning "Maven no recompilo el servicio restaurado; el resultado del paso 1b no es valido."
    $codigoCon = 99
}
Write-Host ("  exit={0}" -f $codigoCon)

$ok = ($codigoSin -ne 0) -and ($rojos.Count -eq $ids.Count) -and ($codigoCon -eq 0)

# --- Paso 2: retest en vivo (opcional) ---------------------------------------
if ($Live) {
    if (-not $env:EDUSYNC_REDTEAM_JWT) {
        Write-Error "Falta EDUSYNC_REDTEAM_JWT (JWT de un PROFESOR sintetico)."
        exit 2
    }
    Write-Host "== Paso 2: retest en vivo contra localhost (backend recompilado y reiniciado)."
    $ErrorActionPreference = "Continue"
    cmd /c "python cli.py probar-api --trials 3 --ids $($ids -join ',') 2>&1"
    $codigoLive = $LASTEXITCODE
    $ErrorActionPreference = "Stop"
    $ultimo = Join-Path $PSScriptRoot "evidencia\ultimo_live.md"
    if (Test-Path $ultimo) {
        Copy-Item $ultimo (Join-Path $salida "live_con_mitigacion_$marca.md") -Force
    }
    Write-Host ("  exit={0} (0 = ningun ataque reproducido)" -f $codigoLive)
    $ok = $ok -and ($codigoLive -eq 0)
}

Write-Host ""
Write-Host "Evidencia en $salida"
if ($ok) {
    Write-Host "RESULTADO: la mitigacion es necesaria (sin ella ganan los 3 ataques) y suficiente (con ella 0 exitos)."
    exit 0
}
Write-Host "RESULTADO: revisar los logs; el retest no dio el patron esperado."
exit 1
