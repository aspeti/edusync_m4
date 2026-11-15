<#
.SYNOPSIS
  Laboratorio S08 (opción A): construye y mide la imagen del backend, y guarda la evidencia.

.DESCRIPTION
  Ejecuta los pasos A1-A5 del laboratorio para la versión indicada y deja todas las salidas en
  docs/laboratorios/evidencia/<etiqueta>.txt (y el log del build en build-<etiqueta>.log).
  Requisitos: Docker Desktop funcionando (docker version muestra Client y Server).
  Las herramientas (Dive, Trivy, hadolint) corren como contenedores con versión y digest fijos.

.EXAMPLE
  .\scripts\lab-s08-medir.ps1 -Etiqueta antes
  .\scripts\lab-s08-medir.ps1 -Etiqueta despues
#>
param([ValidateSet('antes', 'despues')][string]$Etiqueta = 'antes')

$ErrorActionPreference = 'Continue'
$raiz = Split-Path -Parent $PSScriptRoot
$dockerfile = if ($Etiqueta -eq 'antes') { 'Dockerfile.antes' } else { 'Dockerfile' }
$imagen = "mi-imagen:$Etiqueta"
$dirEvidencia = Join-Path $raiz 'docs\laboratorios\evidencia'
New-Item -ItemType Directory -Force -Path $dirEvidencia | Out-Null
$salida = Join-Path $dirEvidencia "$Etiqueta.txt"
$logBuild = Join-Path $dirEvidencia "build-$Etiqueta.log"

# Versiones fijas con digest (README del laboratorio). Nunca :latest.
$DIVE = 'wagoodman/dive:v0.13.1@sha256:f1886e6c32c094fc41a623c1989f5cb3e48aa766da5f0be233f911fc1d85ce10'
$TRIVY = 'aquasec/trivy:0.76.1@sha256:790fd66d1697395d8693d1acaa1ea702406a20f109b6fc7bac8db623eca6c06c'
$HADOLINT = 'hadolint/hadolint:v2.15.1@sha256:32dac94127fd60b7b7e3fbfc65e1383b9b5e25c9bfd7b8536de7a539fe68a12d'

"LAB S08 - evidencia '$Etiqueta' - $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" | Set-Content -Path $salida -Encoding utf8

function Registrar([string]$titulo, [string]$comando, [scriptblock]$bloque) {
    Write-Host ">> $titulo"
    "`n==== $titulo ====`n`$ $comando" | Add-Content -Path $salida -Encoding utf8
    & $bloque 2>&1 | Out-String | Add-Content -Path $salida -Encoding utf8
}

# --- A1: construir (se mide el tiempo; la salida del build va a su propio log) ---
Write-Host ">> A1 build ($dockerfile) ..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()
docker build --no-cache -f (Join-Path $raiz "backend\$dockerfile") -t $imagen (Join-Path $raiz 'backend') *> $logBuild
$sw.Stop()
if ($LASTEXITCODE -ne 0) { Write-Host "El build FALLO. Revise $logBuild" -ForegroundColor Red; exit 1 }
"`n==== A1 build ====`n`$ docker build --no-cache -f backend/$dockerfile -t $imagen backend`nTiempo de build (sin caché): $([math]::Round($sw.Elapsed.TotalSeconds,1)) s" | Add-Content -Path $salida -Encoding utf8

# --- A2: tamaño y capas ---
Registrar 'A2 tamaño' "docker image ls $imagen" { docker image ls $imagen }
Registrar 'A2 número de capas' "docker image history -q $imagen" { (docker image history -q $imagen | Measure-Object).Count }
Registrar 'A2 capas (history)' "docker image history $imagen" { docker image history $imagen }
Registrar 'A2 Dive (CI=true)' "docker run --rm -e CI=true ... dive $imagen" {
    docker run --rm -e CI=true -v /var/run/docker.sock:/var/run/docker.sock $DIVE $imagen
}

# --- A3: usuario ---
Registrar 'A3 usuario configurado' "docker image inspect $imagen --format {{.Config.User}}" {
    docker image inspect $imagen --format '{{.Config.User}}'
}
Registrar 'A3 id dentro del contenedor' "docker run --rm --entrypoint id $imagen" {
    docker run --rm --entrypoint id $imagen
}

# --- A4: CVE (Trivy, misma herramienta para antes y después) ---
Registrar 'A4 Trivy (todas)' "trivy image --scanners vuln $imagen" {
    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v trivy-cache:/root/.cache $TRIVY image --scanners vuln $imagen
}
Registrar 'A4 Trivy (HIGH,CRITICAL)' "trivy image --scanners vuln --severity HIGH,CRITICAL $imagen" {
    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v trivy-cache:/root/.cache $TRIVY image --scanners vuln --severity HIGH,CRITICAL $imagen
}

# --- A5: hadolint ---
Registrar "A5 hadolint ($dockerfile)" "Get-Content $dockerfile | hadolint" {
    Get-Content (Join-Path $raiz "backend\$dockerfile") | docker run --rm -i $HADOLINT
}

Write-Host "Listo. Evidencia en: $salida" -ForegroundColor Green
