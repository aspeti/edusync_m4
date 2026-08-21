# PR-IMPL-015 — Académico: Periodos de Evaluación (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-015` |
| Título | Módulo `academico`: `PeriodoEvaluacion` (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-015.md` |
| ID origen | `DD-UC-015` (`FSD-UC-013`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 21/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** |

> **Convención de ruta**: este prompt vive en `docs/prompts/impl/`, siguiendo `plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md` §5.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Full-Stack Engineer con experiencia en Java 25 / Spring Boot
4.1.0 (arquitectura hexagonal, Spring Data JPA, Spring Modulith) y Angular 21
(standalone, signals) en el proyecto EduSync.
```

### 1.2 Task

```text
Implementa FSD-UC-013 segun docs/design/DD-UC-015.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico): dominio PeriodoEvaluacion (Aggregate independiente
con orden 1-based y estado PENDIENTE/ABIERTO/CERRADO); seed de 3 trimestres
en CrearGestionEscolarService (misma TX); GET /gestiones-escolares/{id};
POST/GET /gestiones-escolares/{id}/periodos; PATCH/DELETE
/periodos-evaluacion/{id}; PATCH .../estado con apertura secuencial.
A1 422 E_PERIODOS_SOLAPADOS; A2 422 E_PERIODO_NO_SECUENCIAL;
422 E_PERIODOS_INMUTABLES si hay un ABIERTO y se intenta POST/DELETE/PATCH
datos; 422 E_PERIODO_UNICO al borrar el ultimo. Migracion Flyway V9 con
tenant_id + RLS FORCE. Delta GET gestion tambien SECRETARIA.

Frontend: pantalla /academico/gestiones-escolares/:id/periodos (detalle +
alta inline + abrir/cerrar); enlace "Periodos" en la lista de gestiones.
RBAC de la pantalla: ADMIN (igual que DD-UC-009). No tocar role.guard.ts.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-015.md (Aggregate independiente, seed, freeze,
  secuencialidad; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.3 (FSD-UC-013) + nota de seed en §4.6.2.
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009, ADR-0011,
  ADR-0012 (Lombok allowlist), ADR-0013 (modelo generico; este slice
  materializa periodos, NO secciones ni el motor de calculo).
- Precedentes backend: academico/domain/GestionEscolar.java,
  GestionEscolarController, CrearGestionEscolarService, Curso/Paralelo
  (Aggregate hijo independiente, listado sin paginar).
- Precedentes frontend: curso-paralelos.page.ts (detalle anidado + alta
  inline); gestiones-escolares-list.page.ts (transicionesValidas).
- Prerrequisito: PR-IMPL-001..014 ejecutados (GestionEscolar API+UI).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad; no seed de secciones (FSD-UC-014); no
  Evaluacion/calculo (FSD-UC-015/016); no reabrir CERRADO; no exigir
  periodos para ACTIVA; no audit_log (ADR-0009 §3 punto 5); no loguear PII;
  no modificar docs/baseline/**.
```

### 1.4 Reasoning

```text
BACKEND
1. domain: PeriodoEvaluacionId, EstadoPeriodoEvaluacion, PeriodoEvaluacion
   (factory crear, cambiarEstado PENDIENTE->ABIERTO / ABIERTO->CERRADO,
   reconstruir). Excepciones listadas en DD-UC-015 §2. Lombok solo @Getter.
2. V9: tabla periodo_evaluacion (id, tenant_id, gestion_escolar_id, nombre,
   fecha_inicio, fecha_fin, orden, estado) + RLS FORCE + FK a
   gestion_escolar. Sin backfill de filas existentes.
3. PeriodoEvaluacionRepositoryPort: guardar, buscarPorIdYTenant,
   listarPorGestionYTenant (ordenado por orden), eliminar, recompactar
   orden tras DELETE.
4. CrearGestionEscolarService: tras persistir la gestion, sembrar 3
   periodos (partir [fechaInicio, fechaFin] en tramos contiguos).
5. ObtenerGestionEscolarUseCase + GET /{id}.
6. Crear/Listar/Actualizar/Eliminar/CambiarEstadoPeriodo: validar gestion
   del tenant; overlap; freeze; secuencialidad en el servicio (conjunto).
7. PeriodoEvaluacionController + delta GestionEscolarController
   (ExceptionHandler: nuevos E_* -> 422 o 404).
8. Tests: dominio, Mockito, PeriodoEvaluacionIntegrationTest (seed,
   Gherkin N=2, secuencia, freeze, cross-tenant 404).

FRONTEND
9. periodo-evaluacion.model.ts + gestion-periodos.page.ts.
10. Ruta :id/periodos (despues de /nuevo para no capturar "nuevo").
11. Enlace Periodos en gestiones-escolares-list.page.ts.
12. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando:
- POST /gestiones-escolares siembra 3 periodos PENDIENTE (GET .../periodos).
- Abrir k=2 con k=1 ABIERTO -> 422 E_PERIODO_NO_SECUENCIAL.
- Fechas solapadas -> 422 E_PERIODOS_SOLAPADOS.
- POST/DELETE/PATCH datos con un ABIERTO -> 422 E_PERIODOS_INMUTABLES.
- DELETE del ultimo periodo -> 422 E_PERIODO_UNICO.
- GET /gestiones-escolares/{id} 200 / 404.
- Cross-tenant 404 (no 403).
- mvn test (incl. ModularityTests) y ng build en verde.
NO implementes FSD-UC-014/015/016, seed de secciones, motor de notas,
audit_log, ni reapertura de CERRADO. NO edites docs/baseline/**.
```

### 1.6 Output

```text
backend/src/main/java/com/edusync/academico/domain/PeriodoEvaluacion*.java
backend/src/main/java/com/edusync/academico/application/** (use cases periodo)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/** (delta)
backend/src/main/resources/db/migration/V9__academico_periodo_evaluacion.sql
backend/src/test/java/com/edusync/academico/**Periodo*
frontend/src/app/features/academico/periodo-evaluacion.model.ts
frontend/src/app/features/academico/gestion-periodos.page.ts
frontend/src/app/features/academico/gestiones-escolares-list.page.ts (delta)
frontend/src/app/app.routes.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene del cliente — siempre de `TenantContextProvider`.
- `academico` **no** importa `identidad` ni `plataforma`.
- Apertura secuencial: periodo *k* no abre si *k−1* no está `CERRADO`.
- N y datos de periodos se congelan cuando existe un `ABIERTO`.
- N ≥ 1 siempre (seed = 3; DELETE del último prohibido).
- Acceso cross-tenant → `404`, nunca `403`.
- `mvn test` (incluye `ModularityTests`) y `ng build` en verde.
- Logs **MUST NOT** registrar PII.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` |
| `E_SEED_OMITIDO` | `POST` gestión no crea 3 periodos | Rechazar; viola `ADR-0013` / `FSD-UC-012` |
| `E_SECCIONES_COLADAS` | Se implementó seed o CRUD de secciones | Revertir; es `FSD-UC-014` |
| `E_CALCULO_COLADO` | Se implementó promedio / `round` / `floor` | Revertir; es `FSD-UC-016` / SIE |
| `E_SECUENCIA_OMITIDA` | Se abre k sin k−1 `CERRADO` | Rechazar; viola A2 |
| `E_PERIODOS_EMBEBIDOS` | Periodos como colección JPA de `GestionEscolar` | Revertir; Aggregate independiente |
| `E_AUDIT_LOG_INVENTADO` | `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PII_EN_LOG` | Se loguea PII | Rechazar (`AGENTS.md` §7) |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` | Rechazar; solo usarlo |
| `E_BASELINE_TOCCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: seed 3 periodos en la misma TX que el alta de gestión.
- MUST: `422 E_PERIODO_NO_SECUENCIAL` y `422 E_PERIODOS_SOLAPADOS`.
- MUST: freeze `E_PERIODOS_INMUTABLES` si hay un `ABIERTO`.
- MUST: `404` cross-tenant (no 403).
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: `FSD-UC-014`/`015`/`016`, seed de secciones, motor de notas.
- MUST NOT: reabrir `CERRADO`; dos `ABIERTO` a la vez.
- MUST NOT: `audit_log`; `docs/baseline/**`; PII en logs.
- MUST NOT: reescribir `role.guard.ts`.
- MUST NOT: embebido de periodos en el AR `GestionEscolar`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-015` | PR-IMPL-015 | `dev-agent` | `PeriodoEvaluacion`, `V9`, consola Angular de Periodos |
| FSD | `FSD-UC-013` | PR-IMPL-015 | `dev-agent` | Sexto feature de negocio real de `academico` (cuarto fullstack) |
| ADR | `ADR-0013` | PR-IMPL-015 | `dev-agent` | Seed + apertura secuencial materializados en código |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-015` completo; `POST /gestiones-escolares` con un rango de fechas de varios meses.
- **Output esperado**: `201`; `GET .../periodos` → 3 filas `Trimestre 1..3` `PENDIENTE`; abrir T1 → `200`; cerrar T1 → `200`; abrir T2 → `200`; UI lista y opera; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: T1 `ABIERTO`; `PATCH` T2 a `ABIERTO`.
- **Output esperado**: `422 E_PERIODO_NO_SECUENCIAL`. POST de un 4º periodo en el mismo estado → `422 E_PERIODOS_INMUTABLES`.

### 6.3 Caso adversarial

- **Input**: solicitud de sembrar secciones, calcular promedio, o embeber periodos en `GestionEscolar`.
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-015` §3; `FSD-UC-014`/`016` tienen Design Docs propios.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-015.md` v1.0. Cuarto prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución: código real backend+UI, `V9`, `mvn test` 200/200, `ng build` verde. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | **ejecutado** | Código real generado; `mvn test` 200/200; `ng build` verde |
