# Demo 4 — `release/8.4.0` · G-EduSync

> Módulo 4 — Ingeniería de Software con IA Agéntica · Maestría en Ingeniería de Sistemas.
> Demo 4 · release evaluable `release/8.4.0`.
> Grupo unipersonal (n = 1): todas las funcionalidades corresponden a un único integrante.

## 1. Funcionalidades por integrante

| Integrante | Funcionalidad | Tamaño | Caso de uso FSD | Repositorio |
|------------|---------------|--------|-----------------|-------------|
| Rodrigo Aspeti | **F1 — Rangos min/max en `SeccionEvaluacion`** (plantilla única por gestión; seed `rangoMin=0` / `rangoMax=nota`) | Grande | `FSD-UC-014` (`DD-UC-034` / `PR-IMPL-034` / `ADR-0027`, issue #30) | https://github.com/aspeti/edusync_m4 |
| Rodrigo Aspeti | **F2 — Periodos sin panel de parámetros** (UI solo nombre/fechas/estado/acciones; retirar `ParametroPeriodo`) | Mediana | `FSD-UC-013` (`DD-UC-034` / `PR-IMPL-034`) | https://github.com/aspeti/edusync_m4 |
| Rodrigo Aspeti | **F3 — Gates al abrir periodo sin `E_PARAMETROS_*`** (Σ secciones = 100 + rangos válidos + cobertura docente) | Mediana | `FSD-UC-009` (`DD-UC-034` / `ADR-0027`) | https://github.com/aspeti/edusync_m4 |
| Rodrigo Aspeti | **F4 — Validación de calificaciones por rango de sección** (`valor ∈ [rangoMin, rangoMax]`) | Pequeña | `FSD-UC-014` / carga de notas (`PR-IMPL-034`) | https://github.com/aspeti/edusync_m4 |

## 2. F1 — Rangos en Secciones (`FSD-UC-014`)

**Endpoints (declarados en `openapi.json`):**

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/v1/gestiones-escolares/{id}/secciones` | Plantilla con `nombre`, `orden`, `nota`, `rangoMin`, `rangoMax` |
| `PUT` | `/api/v1/gestiones-escolares/{id}/secciones` | Reemplazo atómico; body incluye rangos |
| `POST` | `/api/v1/gestiones-escolares/{id}/secciones` | Alta con `rangoMin`/`rangoMax` required |
| `PATCH` | `/api/v1/secciones-evaluacion/{id}` | Actualiza `rangoMin?`/`rangoMax?` |

**UI:** `/academico/gestiones-escolares/.../secciones` — columnas/inputs mín/máx por fila (`gestion-secciones.page.ts`).

**Criterios de aceptación** (`docs/product/FSD.md` §4.6.4):

| # | Escenario | Verificación |
|---|-----------|--------------|
| CA-1 | Seed Ser/Saber/Hacer/Autoevaluación con Σ `nota` = 100 y rangos `[0, nota]` | Dominio + integración al crear gestión |
| CA-2 | Editar secciones con periodo `ABIERTO` permitido (`ADR-0014`) | `SeccionEvaluacionIntegrationTest` / servicios |
| CA-3 | Σ `nota` ≠ 100 → `422 E_SUMA_SECCIONES_INVALIDA` | Tests de sección |
| CA-4 | `rangoMax > nota` (u otras invariantes) → `422 E_RANGO_INVALIDO` | `SeccionEvaluacionTest` |
| CA-5 | Migración Flyway `V14` añade columnas y dropea `parametro_periodo` | `V14__academico_seccion_rangos.sql` |

## 3. F2 — Periodos sin parámetros (`FSD-UC-013`)

| Elemento | Cambio |
|----------|--------|
| UI | `gestion-periodos.page.ts` sin bloque de parámetros ni llamadas a `/parametros` |
| API | Eliminados `GET`/`PUT /api/v1/periodos-evaluacion/{id}/parametros` |
| Dominio | Retirado aggregate `ParametroPeriodo` (+ servicios, JPA, tests) |

**Criterios de aceptación:**

| # | Criterio | Cómo se comprueba |
|---|----------|-------------------|
| CA-1 | Pantalla de Periodos solo muestra nombre, inicio, fin, estado y acciones | UI + revisión de código |
| CA-2 | Endpoints `/parametros` ausentes del contrato y del código | `openapi.json` + 404 si se invocan |
| CA-3 | Crear periodo reutiliza plantilla de secciones sin paso extra | Flujo `FSD-UC-013` / `ADR-0027` |

## 4. F3 — Gates al abrir (`FSD-UC-009`)

Al `PATCH .../estado` → `ABIERTO`:

| # | Criterio | Código / error |
|---|----------|----------------|
| CA-1 | Σ `nota` = 100 y plantilla no vacía | `422 E_SUMA_SECCIONES_INVALIDA` |
| CA-2 | Rangos de cada sección válidos | `422 E_RANGO_INVALIDO` |
| CA-3 | Toda materia con curso tiene profesor | `409 E_MATERIA_SIN_DOCENTE` |
| CA-4 | **No** se exige `ParametroPeriodo` / `E_PARAMETROS_*` | `AperturaPeriodoPolitica` / `PeriodoEvaluacionServicesTest` |

## 5. F4 — Calificaciones en rango de sección

| # | Criterio | Verificación |
|---|----------|--------------|
| CA-1 | `valor` fuera de `[rangoMin, rangoMax]` → `422 E_RANGO_INVALIDO` | `CalificacionEvaluacionTest` / upsert |
| CA-2 | `puntajeMaximo` de la evaluación sigue = `seccion.nota` (sin cambiar `CalculoNotas`) | Dominio / `ADR-0027` |

## 6. Cómo reproducir

```bash
# 1. Base de datos + API
docker compose -f infra/docker-compose.yml up -d
# 2. Tests
cd backend && mvn test
# 3. UI
cd frontend && npm run build
# 4. Contrato OpenAPI (si hubo cambio de API)
./scripts/export-openapi.sh   # Windows: .\scripts\export-openapi.ps1
```

## 7. Registro en Classroom

El mismo contenido (tabla de integrante, funcionalidades y link del repositorio) se registra en Classroom.
Ver también `8.4.0-aportes.md` (raíz del release).

## 8. Nota sobre CI OpenAPI / oasdiff

El job `oasdiff (opcional, no bloquea)` puede marcar rojo ante breaking changes intencionales (p. ej. retiro de `/parametros`, nuevos required `rangoMin`/`rangoMax`). El job obligatorio `Drift + Spectral` debe permanecer en verde. Ver corrección del workflow en la conversación / PR de Demo 4.
