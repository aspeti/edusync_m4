# PR-IMPL-016 — Académico: Secciones de Evaluación (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-016` |
| Título | Módulo `academico`: `SeccionEvaluacion` (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-016.md` |
| ID origen | `DD-UC-016` (`FSD-UC-014`) |
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
Implementa FSD-UC-014 segun docs/design/DD-UC-016.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico): dominio SeccionEvaluacion (Aggregate independiente
con orden 1-based y nota BigDecimal; plantilla de la GestionEscolar, NO del
periodo); seed de 4 secciones (Ser 5 / Saber 45 / Hacer 40 / Autoevaluacion 10)
en CrearGestionEscolarService (misma TX, junto al seed de 3 periodos ya
existente); GET/PUT/POST /gestiones-escolares/{id}/secciones; PATCH
/secciones-evaluacion/{id}. A1 422 E_PESO_INVALIDO; A2 422
E_SUMA_SECCIONES_INVALIDA; A3 422 E_SECCIONES_INMUTABLES (freeze sticky:
cualquier periodo ABIERTO o CERRADO). Migracion Flyway V10 con tenant_id +
RLS FORCE. Delta: no abrir un periodo si la plantilla esta vacia o no suma 100.

Frontend: pantalla /academico/gestiones-escolares/:id/secciones (tabla + PUT);
enlace "Secciones" en la lista de gestiones. RBAC de la pantalla: ADMIN.
No tocar role.guard.ts.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-016.md (Aggregate independiente, seed, PUT
  atomico, freeze sticky; ver §2/§3).
- FSD: docs/product/FSD.md §4.6.4 (FSD-UC-014) + nota de seed en §4.6.2.
- ADRs: ADR-0001 (RLS), ADR-0008 (stack vivo), ADR-0009, ADR-0011,
  ADR-0012 (Lombok allowlist), ADR-0013 (modelo generico; este slice
  materializa secciones, NO evaluaciones ni el motor de calculo).
- Precedentes backend: PeriodoEvaluacion, CrearGestionEscolarService
  (ya siembra 3 periodos), GestionEscolarController GET /{id}.
- Precedentes frontend: gestion-periodos.page.ts (detalle anidado).
- Prerrequisito: PR-IMPL-001..015 ejecutados (Periodos API+UI).
- Restricciones: tenantId SIEMPRE desde TenantContextProvider; academico
  NO importa identidad; no Evaluacion/calculo (FSD-UC-015/016); no
  peso_porcentual separado; no DELETE item; no colgar secciones del
  periodo; no audit_log (ADR-0009 §3 punto 5); no loguear PII;
  no modificar docs/baseline/**.
```

### 1.4 Reasoning

```text
BACKEND
1. domain: SeccionEvaluacionId, SeccionEvaluacion (factory crear,
   actualizar nombre/nota, reconstruir). Excepciones listadas en
   DD-UC-016 §2. Lombok solo @Getter. nota BigDecimal escala 2,
   (0, 100].
2. V10: tabla seccion_evaluacion (id, tenant_id, gestion_escolar_id,
   nombre, orden, nota) + RLS FORCE + FK a gestion_escolar + unique
   (gestion_escolar_id, orden). Sin backfill.
3. SeccionEvaluacionRepositoryPort: guardar, guardarTodos, buscarPorIdYTenant,
   listarPorGestionYTenant (ordenado por orden), reemplazarPlantilla.
4. CrearGestionEscolarService: tras el seed de periodos, sembrar las 4
   secciones defaults.
5. Politica: freeze sticky si algun periodo no esta PENDIENTE; suma
   exacta 100.00 tras PUT/POST/PATCH.
6. PUT reemplaza la plantilla (orden = indice 1-based del array, M>=1).
   POST/PATCH item validan suma resultante.
7. Delta CambiarEstadoPeriodoEvaluacionService: ABIERTO exige plantilla
   con Σ=100.
8. SeccionEvaluacionController + delta GestionEscolarController
   (ExceptionHandler: nuevos E_* -> 422 o 404).
9. Tests: dominio, Mockito, SeccionEvaluacionIntegrationTest (seed
   Gherkin, PUT rebalance, freeze ABIERTO y CERRADO, abrir sin
   secciones, cross-tenant 404).

FRONTEND
10. seccion-evaluacion.model.ts + gestion-secciones.page.ts.
11. Ruta :id/secciones (despues de /nuevo para no capturar "nuevo").
12. Enlace Secciones en gestiones-escolares-list.page.ts.
13. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando:
- POST /gestiones-escolares siembra 4 secciones Σ=100 (GET .../secciones).
- PUT con suma 99 -> 422 E_SUMA_SECCIONES_INVALIDA.
- nota 0 o 101 -> 422 E_PESO_INVALIDO.
- PUT/POST/PATCH con un periodo ABIERTO o CERRADO -> 422 E_SECCIONES_INMUTABLES.
- Abrir T1 sin secciones (gestion vieja) -> 422 E_SUMA_SECCIONES_INVALIDA.
- Cross-tenant 404 (no 403).
- mvn test (incl. ModularityTests) y ng build en verde.
NO implementes FSD-UC-015/016, Evaluacion, promedio/round/floor,
DELETE item, peso_porcentual, audit_log. NO edites docs/baseline/**.
```

### 1.6 Output

```text
backend/src/main/java/com/edusync/academico/domain/SeccionEvaluacion*.java
backend/src/main/java/com/edusync/academico/application/** (use cases seccion)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/** (delta)
backend/src/main/resources/db/migration/V10__academico_seccion_evaluacion.sql
backend/src/test/java/com/edusync/academico/**Seccion*
frontend/src/app/features/academico/seccion-evaluacion.model.ts
frontend/src/app/features/academico/gestion-secciones.page.ts
frontend/src/app/features/academico/gestiones-escolares-list.page.ts (delta)
frontend/src/app/app.routes.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene del cliente — siempre de `TenantContextProvider`.
- `academico` **no** importa `identidad` ni `plataforma`.
- Plantilla a nivel de **gestión**, no de periodo.
- Σ `nota` = 100.00 en toda escritura que persista la plantilla.
- Freeze sticky: ningún cambio de secciones si hay un periodo `ABIERTO` o `CERRADO`.
- Acceso cross-tenant → `404`, nunca `403`.
- `mvn test` (incluye `ModularityTests`) y `ng build` en verde.
- Logs **MUST NOT** registrar PII.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar; siempre del contexto |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar; `ApplicationModules.verify()` |
| `E_SEED_OMITIDO` | `POST` gestión no crea 4 secciones Σ=100 | Rechazar; viola `ADR-0013` / `FSD-UC-014` |
| `E_EVALUACION_COLADA` | Se implementó `Evaluacion` o el motor de notas | Revertir; es `FSD-UC-015`/`016` |
| `E_PESO_SEPARADO` | Se añadió `peso_porcentual` además de `nota` | Revertir; `ADR-0013` |
| `E_SECCIONES_POR_PERIODO` | FK a `periodo_evaluacion` en vez de gestión | Revertir |
| `E_SECCIONES_EMBEBIDAS` | Secciones como colección JPA de `GestionEscolar` | Revertir; Aggregate independiente |
| `E_FREEZE_NO_STICKY` | Se permite editar secciones tras cerrar T1 | Rechazar; viola `ADR-0013` §3.1.5 |
| `E_AUDIT_LOG_INVENTADO` | `audit_log` sin resolver `ADR-0009` §3 punto 5 | Revertir |
| `E_PII_EN_LOG` | Se loguea PII | Rechazar (`AGENTS.md` §7) |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` | Rechazar; solo usarlo |
| `E_BASELINE_TOCCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: `tenantId` siempre desde `TenantContextProvider`.
- MUST: seed 4 secciones en la misma TX que el alta de gestión.
- MUST: `422 E_PESO_INVALIDO`, `E_SUMA_SECCIONES_INVALIDA`, `E_SECCIONES_INMUTABLES`.
- MUST: freeze sticky (ABIERTO o CERRADO).
- MUST: `404` cross-tenant (no 403).
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: `FSD-UC-015`/`016`, `Evaluacion`, promedio/`round`/`floor`.
- MUST NOT: `peso_porcentual`; secciones por periodo; `DELETE` item.
- MUST NOT: `audit_log`; `docs/baseline/**`; PII en logs.
- MUST NOT: reescribir `role.guard.ts`.
- MUST NOT: embebido de secciones en el AR `GestionEscolar`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-016` | PR-IMPL-016 | `dev-agent` | `SeccionEvaluacion`, `V10`, consola Angular de Secciones |
| FSD | `FSD-UC-014` | PR-IMPL-016 | `dev-agent` | Séptimo feature de negocio real de `academico` (quinto fullstack) |
| ADR | `ADR-0013` | PR-IMPL-016 | `dev-agent` | Seed 4 + Σ=100 + freeze sticky materializados en código |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-016` completo; `POST /gestiones-escolares`.
- **Output esperado**: `201`; `GET .../secciones` → 4 filas Ser/Saber/Hacer/Autoevaluación Σ=100; PUT a 60+40 (dos filas) → `200`; UI lista y guarda; `mvn test` y `ng build` en verde.

### 6.2 Caso borde

- **Input**: T1 `ABIERTO`; PUT que cambia Saber 45→40.
- **Output esperado**: `422 E_SECCIONES_INMUTABLES`. Tras cerrar T1, el mismo PUT sigue 422 (sticky).

### 6.3 Caso adversarial

- **Input**: solicitud de crear `Evaluacion`, calcular promedio, o colgar secciones del periodo.
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-016` §3; `FSD-UC-015`/`016` tienen Design Docs propios.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-016.md` v1.0. Quinto prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución: `SeccionEvaluacion`, seed 4, PUT atómico, freeze sticky, `V10`, consola Angular. `mvn test` 215/215; `ng build` verde. Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | ejecutado | Código fullstack + tests + `dtp-sync`; commit formal pendiente |
