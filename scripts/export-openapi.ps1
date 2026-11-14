<#
.SYNOPSIS
  Exporta el contrato OpenAPI generado desde el código (springdoc) a openapi.json.

.DESCRIPTION
  Requisitos: JDK 25, Maven y PostgreSQL accesible (infra/docker-compose.yml) con las
  variables DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD (por defecto las del perfil dev).

.EXAMPLE
  .\scripts\export-openapi.ps1
  .\scripts\export-openapi.ps1 -Salida .\openapi.generated.json
#>
param([string]$Salida = "")

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($Salida)) { $Salida = Join-Path $raiz "openapi.json" }
elseif (-not [System.IO.Path]::IsPathRooted($Salida)) { $Salida = Join-Path (Get-Location) $Salida }

Push-Location (Join-Path $raiz "backend")
try {
  mvn -B -q test "-Dtest=OpenApiContractExportTest" "-Dsurefire.failIfNoSpecifiedTests=false" "-Dopenapi.export.path=$Salida"
  if ($LASTEXITCODE -ne 0) { throw "Falló la exportación del contrato OpenAPI (mvn exit $LASTEXITCODE)" }
} finally { Pop-Location }

Write-Host "Contrato exportado en: $Salida"
