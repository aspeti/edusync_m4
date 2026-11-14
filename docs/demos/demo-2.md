# Demo 2 — `release/8.2.0` · G-EduSync

> Módulo 4 — Ingeniería de Software con IA Agéntica · Maestría en Ingeniería de Sistemas.
> Demo 2: lunes 26 de octubre · release evaluable `release/8.2.0`.
> Grupo unipersonal (n = 1): todas las funcionalidades corresponden a un único integrante.

## 1. Funcionalidades por integrante

| Integrante | Funcionalidad | Tamaño | Caso de uso FSD | Repositorio |
|------------|---------------|--------|-----------------|-------------|
| Rodrigo Aspeti | **F1 — Planilla del Profesor por Curso/Paralelo/Materia** (landing "Mis cursos" + planilla estudiantes × evaluaciones con carga inline) | Mediana | `FSD-UC-022` (`DD-UC-033` / `PR-IMPL-033`, issue #21) | https://github.com/aspeti/edusync_m4 |
| Rodrigo Aspeti | **F2 — Contrato OpenAPI generado desde el código + flujo de validación en CI** (export `openapi.json`, drift, Spectral, oasdiff opcional) | Mediana | Transversal (calidad de contrato; endpoints de F1 incluidos) | https://github.com/aspeti/edusync_m4 |


## 2. F1 — Planilla del Profesor (`FSD-UC-022`)

**Endpoints (declarados en `openapi.json`):**

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/profesores/me/asignaciones` | Asignaciones (curso × paralelo × materia) del Profesor autenticado |
| `GET` | `/api/v1/materias/{id}/planilla` | Planilla estudiantes × evaluaciones del periodo, con `notaPeriodo` calculada por `CalculoNotas` |
| `PUT` | `/api/v1/evaluaciones/{id}/calificaciones` | Upsert de calificaciones; `valor: null` limpia la nota |

**UI:** `/academico/mis-cursos` (landing) y `/academico/mis-cursos/planilla` (planilla con autosave en blur/Enter; estilos del prototipo Figma, nodos `774:35346` y `774:37846`).

**Criterios de aceptación** (`docs/product/FSD.md` §4.6.12, escenarios Gherkin):

| # | Escenario | Verificación |
|---|-----------|--------------|
| CA-1 | Landing del Profesor muestra curso concatenado, materia e **Ingresar** | `PlanillaProfesorIntegrationTest` (`GET /profesores/me/asignaciones`) + UI `mis-cursos` |
| CA-2 | Planilla con una columna por evaluación `ACTIVA` y **Promedio** de solo lectura | `PlanillaProfesorIntegrationTest` (`GET .../planilla`) |
| CA-3 | Carga inline sin botón Guardar (blur/Enter); valor vacío limpia la nota | `PlanillaProfesorIntegrationTest` (null-clear) + UI `planilla-curso` |
| CA-4 | Rango inválido → `422 E_RANGO_INVALIDO` mostrado en la celda | `PlanillaProfesorIntegrationTest` + UI (error inline) |
| CA-5 | Tripleta no asignada / otro tenant → `404` (sin filtrado cruzado) | `PlanillaProfesorIntegrationTest` (aislamiento cross-tenant) |

## 3. F2 — Contrato OpenAPI y flujo de validación

| Elemento | Ubicación |
|----------|-----------|
| Generación desde código (springdoc + anotaciones `@Operation`/`@ApiResponse`) | `backend/` (`shared.web.OpenApiConfig`, controladores REST) |
| Comando de exportación | `./scripts/export-openapi.sh` (Linux/macOS) · `.\scripts\export-openapi.ps1` (Windows) → `openapi.json` |
| Test que exporta el contrato (determinista: claves ordenadas, servidor fijo) | `backend/src/test/java/com/edusync/shared/web/OpenApiContractExportTest.java` |
| Contrato versionado | `openapi.json` (raíz) |
| Reglas de lint | `.spectral.yaml` |
| Workflow de CI (PR → drift + Spectral; oasdiff opcional) | `.github/workflows/openapi.yml` |

**Criterios de aceptación de F2:**

| # | Criterio | Cómo se comprueba |
|---|----------|-------------------|
| CA-1 | `openapi.json` se genera desde el código y contiene los endpoints de F1 | `OpenApiContractExportTest` falla si faltan las rutas de F1 |
| CA-2 | Un PR con `openapi.json` distinto del generado por el código **falla** | Paso "Drift" de `openapi.yml` (`diff -u`) |
| CA-3 | Un PR con el contrato incumpliendo las reglas **falla** | Paso "Spectral (lint)" (`--fail-severity=error`) |
| CA-4 | (Opcional) Cambios incompatibles frente a la rama base se reportan | Job `breaking-changes` (`oasdiff breaking`) |

## 4. Cómo reproducir

```bash
# 1. Base de datos local
docker compose -f infra/docker-compose.yml up -d
# 2. Regenerar el contrato
./scripts/export-openapi.sh            # Windows: .\scripts\export-openapi.ps1
# 3. Lint local
npx @stoplight/spectral-cli@6.15.0 lint openapi.json --ruleset .spectral.yaml
```

## 5. Registro en Classroom

El mismo contenido (tabla de integrante, funcionalidades y link del repositorio) se registra en Classroom.
Ver también `8.2.0-aportes.md` (raíz del release).
