# Crea el lote completo del dry-run github-issues-edusync (10 issues).
# Uso (PowerShell, raíz del repo):
#   gh auth login
#   .\scripts\create-backlog-issues.ps1
#
# Requiere: gh autenticado contra github.com/aspeti/edusync_m4

$ErrorActionPreference = "Stop"

function Ensure-Label([string]$Name, [string]$Color, [string]$Description) {
  $exists = gh label list --limit 200 --json name -q ".[].name" 2>$null
  if ($exists -notcontains $Name) {
    gh label create $Name --color $Color --description $Description 2>$null
    if ($LASTEXITCODE -ne 0) {
      Write-Host "Label '$Name' no creado (puede existir o faltar permiso); se intenta el issue igual."
    }
  }
}

function New-Issue([string]$Title, [string[]]$Labels, [string]$Body) {
  $labelArgs = @()
  foreach ($l in $Labels) { $labelArgs += @("--label", $l) }
  $tmp = New-TemporaryFile
  Set-Content -Path $tmp -Value $Body -Encoding utf8
  try {
    $url = gh issue create --title $Title @labelArgs --body-file $tmp
    Write-Host "OK  $Title"
    Write-Host "    $url"
    return $url
  }
  finally {
    Remove-Item $tmp -ErrorAction SilentlyContinue
  }
}

Write-Host "Comprobando autenticacion..."
gh auth status
if ($LASTEXITCODE -ne 0) {
  throw "E_GH_NO_AUTH: ejecuta 'gh auth login' y vuelve a correr este script."
}

Write-Host "Asegurando labels..."
Ensure-Label "type:impl"     "1D76DB" "Implementacion / PR-IMPL"
Ensure-Label "type:adr"      "5319E7" "Decision ADR"
Ensure-Label "type:docs"     "0E8A16" "Documentacion / chore / POC"
Ensure-Label "type:deferred" "BFDADC" "Trabajo diferido a proposito"
Ensure-Label "area:backend"  "FBCA04" "Backend"
Ensure-Label "area:frontend" "F9D0C4" "Frontend"
Ensure-Label "area:notassie" "D93F0B" "Modulo notassie / SIE"
Ensure-Label "blocked"       "B60205" "Bloqueado por otro issue o ADR"

$created = @()

$created += New-Issue `
  -Title "adr: Aprobar ADR-0025 (SIE nota final / sin floor)" `
  -Labels @("type:adr") `
  -Body @"
## Resumen
Pasar ``ADR-0025`` de Propuesta a Aceptada para desbloquear ``PR-IMPL-031``.

## Trazabilidad
- FSD: ``FSD-UC-004`` (y reduccion de ``FSD-UC-001``/``003``)
- Design Doc: ``DD-UC-031``
- Prompt: ``PR-ADR-012`` / ``PR-IMPL-031``
- ADR: ``ADR-0025`` (Propuesta)

## Alcance
- [ ] Revision humana del ADR (perfil = modelo generico; SIE solo destino)
- [ ] Estado → **Aceptada** + fila de historial
- Fuera: implementar codigo (eso es el issue de ``PR-IMPL-031``)

## Criterio de hecho
- [ ] Cabecera del ADR en *Aceptada*
- [ ] Sin tocar ``docs/baseline/**``

## Notas
Bloqueado por: ninguno. Bloquea: issue de ``PR-IMPL-031``.
"@

$created += New-Issue `
  -Title "impl: Exportacion SIE nota final de periodo (PR-IMPL-031)" `
  -Labels @("type:impl", "area:notassie", "blocked") `
  -Body @"
## Resumen
Ejecutar ``PR-IMPL-031``: modulo ``notassie``, exportacion asincrona e idempotente de la nota final de periodo.

## Trazabilidad
- FSD: ``FSD-UC-004``
- Design Doc: ``DD-UC-031``
- Prompt: ``PR-IMPL-031``
- ADR: ``ADR-0025`` (debe estar *Aceptada*), ``ADR-0005``, ``ADR-0013``

## Alcance
- [ ] ``CalculoNotas.notaPeriodoSiCompleto`` + puerto de consulta por lote
- [ ] Dominio/aplicacion/infra ``notassie`` + ``V14`` + Resilience4j
- [ ] Consola Angular (PrimeNG, tras ``PR-IMPL-032``)
- [ ] Corregir corpus del asistente (``floor()`` reservado al SIE)
- Fuera: inmutabilidad, audit_log, centralizador, boletines, cierre de materia (diferidos)

## Criterio de hecho
- [ ] DoD ``DD-UC-031``
- [ ] ``mvn test`` + ``ng build`` verdes; sin ``rude``/notas en logs INFO
- [ ] Sin tocar ``docs/baseline/**``

## Notas
Bloqueado por: ADR-0025. Confirmar antes/durante los 3 datos abiertos del §8 (formato SIE, id materia, longitud RUDE).
"@

$created += New-Issue `
  -Title "impl: RAG vectorial camino PROCESO (PR-IMPL-030)" `
  -Labels @("type:impl", "area:backend") `
  -Body @"
## Resumen
Ejecutar ``PR-IMPL-030``: embeddings en memoria para el camino PROCESO del asistente.

## Trazabilidad
- FSD / NFR: ``NFR-007``, ``FSD-UC-016`` (contexto)
- Design Doc: ``DD-UC-030``
- Prompt: ``PR-IMPL-030``
- ADR: ``ADR-0024`` (Aceptada)

## Alcance
- [ ] Indice en memoria + cita textual
- [ ] Fallback lexico de ``DD-UC-028`` si falla el embedding
- Fuera: Chroma / pgvector

## Criterio de hecho
- [ ] Stop condition del prompt; tests en verde
- [ ] Sin PII/RUDE/notas al modelo

## Notas
Bloqueado por: ninguno.
"@

$created += New-Issue `
  -Title "chore: Prueba manual matriz calificaciones >= 45 estudiantes (DD-UC-032)" `
  -Labels @("type:docs", "area:frontend") `
  -Body @"
## Resumen
Validar en UI real el techo de ``p-table`` (senal de reversion de ``ADR-0026`` §6).

## Trazabilidad
- FSD: ``FSD-UC-016``
- Design Doc: ``DD-UC-032`` §6
- Prompt: ``PR-IMPL-032`` (ya ejecutado)
- ADR: ``ADR-0026`` (Aceptada)

## Alcance
- [ ] Curso >= 45 alumnos: Tab/Enter, rango, 3 cambios = 3 items en red, borrado invalido, ``57,5`` → 57.5, sin rude/notas en consola
- Fuera: cambiar libreria sin ADR nuevo

## Criterio de hecho
- [ ] Casilla DoD de prueba manual marcada en ``DD-UC-032``
- [ ] Si falla → plan B ``ADR-0026`` §6 (ADR de grilla, sin descartar PrimeNG)

## Notas
Bloqueado por: ninguno.
"@

$created += New-Issue `
  -Title "fsd-gap: Residuos FSD-UC-009 (notificacion / audit_log)" `
  -Labels @("type:docs", "area:backend") `
  -Body @"
## Resumen
``FSD-UC-009`` esta parcial: faltan notificacion de cierre y ``audit_log`` (fuera de ``PR-IMPL-029``).

## Trazabilidad
- FSD: ``FSD-UC-009``
- Design Doc: ``DD-UC-029`` (ejecutado, alcance acotado)
- ADR: gobernanza pendiente (``ADR-0009`` §3 pto 5 / diferidos ``ADR-0025``)

## Alcance
- [ ] Decidir si se abre DD+PR-IMPL propios o se absorbe en la epica de diferidos
- Fuera: restaurar apertura secuencial (``ADR-0014`` la elimino a proposito)

## Criterio de hecho
- [ ] Issue hijo de impl o cierre explicito como diferido

## Notas
Bloqueado por: ninguno (decision de alcance).
"@

$created += New-Issue `
  -Title "deferred: Epica diferidos ADR-0025 §3.7" `
  -Labels @("type:deferred") `
  -Body @"
## Resumen
Cola explicitamente diferida: no implementar en el sprint de exportacion SIE.

## Trazabilidad
- ADR: ``ADR-0025`` §3.7
- FSD: ``FSD-UC-005``, ``FSD-UC-002``, ``FSD-UC-007``; centralizador de ``FSD-UC-003``; ``audit_log``

## Alcance (solo seguimiento)
- [ ] Inmutabilidad / append-only / ``registro_padre_id``
- [ ] ``AutorizacionCorreccion`` + ventana 1–72 h
- [ ] ``audit_log`` modulos genericos
- [ ] Tabla centralizador PROVISIONAL/OFICIAL
- [ ] Boletines PDF; notificaciones; cierre de materia
- Fuera: cualquier implementacion sin ADR de gobernanza

## Criterio de hecho
- [ ] Cada item tiene issue hijo o queda documentado como fuera de ``release/3.0.0``

## Notas
Bloqueado por: ADR de gobernanza (aun no creado).
"@

$created += New-Issue `
  -Title "deferred: Diseno tenant demo (FSD-UC-011)" `
  -Labels @("type:deferred") `
  -Body @"
## Resumen
Diseno del tenant demo como funcionalidad de producto (no solo seed), diferido desde ``ADR-0010`` / ``DD-UC-003``.

## Trazabilidad
- FSD: ``FSD-UC-011``
- ADR: ``ADR-0010`` §3
- Design Doc: seguimiento pendiente (distinto de ``DD-UC-003``)

## Alcance
- [ ] Crear Design Doc + PR-IMPL cuando se priorice
- Fuera: cambiar el modelo ``Usuario``/``Rol`` ya decidido

## Criterio de hecho
- [ ] DD aprobado o decision explicita de no hacerlo en ``release/3.0.0``

## Notas
No bloqueante para el resto del producto.
"@

$created += New-Issue `
  -Title "chore: Ejecutar POC-01 RLS y POC-02 Circuit Breaker SIE" `
  -Labels @("type:docs") `
  -Body @"
## Resumen
Ejecutar con evidencia real las POCs documentadas (``DTP`` §12).

## Trazabilidad
- ``docs/pocs/POC-01-rls-multitenancy/``
- ``docs/pocs/POC-02-circuit-breaker-sie/``
- ``docs/product/DTP.md`` §12

## Alcance
- [ ] Correr POC-01 y capturar evidencia pass/fail
- [ ] Correr POC-02 y capturar evidencia pass/fail
- Fuera: reescribir ADRs salvo que la POC falle el criterio SMART

## Criterio de hecho
- [ ] README/evidencia actualizados; veredicto registrado

## Notas
Bloqueado por: ninguno.
"@

$created += New-Issue `
  -Title "chore: Reconfigurar checkstyle a Google Java Style (no sun_checks)" `
  -Labels @("type:docs", "area:backend") `
  -Body @"
## Resumen
``mvn checkstyle:check`` usa ``sun_checks.xml`` por defecto y falla con violaciones preexistentes (gap desde ``PR-IMPL-005``).

## Trazabilidad
- ``AGENTS.md`` §5 / checklist
- ``backend/pom.xml``

## Alcance
- [ ] Configurar ruleset acorde a Google Java Style
- [ ] Plan de adopcion incremental (no reformatear todo a ciegas)
- Fuera: silenciar el plugin o ``--no-verify``

## Criterio de hecho
- [ ] ``mvn checkstyle:check`` usable como gate en codigo nuevo

## Notas
Bloqueado por: ninguno.
"@

$created += New-Issue `
  -Title "docs: Confirmar formato SIE + id materia + longitud RUDE (DD-UC-031 §8)" `
  -Labels @("type:docs", "area:notassie") `
  -Body @"
## Resumen
Cerrar las 3 preguntas abiertas del payload provisional antes o durante ``PR-IMPL-031``.

## Trazabilidad
- Design Doc: ``DD-UC-031`` §8
- FSD: ``FSD-UC-004``
- ADR: ``ADR-0025`` / ``ADR-0005``

## Alcance
- [ ] Formato/autenticacion real del SIE (o confirmar que el provisional basta)
- [ ] Identificador de materia que espera el SIE
- [ ] Longitud min/max del RUDE (``edusync.sie.rude-longitud-min/max``)
- Fuera: cambiar el motor de calculo

## Criterio de hecho
- [ ] Respuestas anotadas en ``DD-UC-031`` §8 o en el PR de ``PR-IMPL-031``

## Notas
Preferible antes de cerrar la exportacion como lista para produccion.
"@

Write-Host ""
Write-Host "Creados: $($created.Count) issues"
$created | ForEach-Object { Write-Host $_ }
