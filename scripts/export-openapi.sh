#!/usr/bin/env bash
# Exporta el contrato OpenAPI generado desde el código (springdoc) a openapi.json.
# Requisitos: JDK 25, Maven y PostgreSQL accesible (infra/docker-compose.yml) con las
# variables DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD (por defecto las del perfil dev).
# Uso: ./scripts/export-openapi.sh [salida]   (por defecto ./openapi.json)
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SALIDA="${1:-$RAIZ/openapi.json}"
case "$SALIDA" in /*) ;; *) SALIDA="$PWD/$SALIDA" ;; esac

cd "$RAIZ/backend"
mvn -B -q test \
  -Dtest=OpenApiContractExportTest \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dopenapi.export.path="$SALIDA"

echo "Contrato exportado en: $SALIDA"
