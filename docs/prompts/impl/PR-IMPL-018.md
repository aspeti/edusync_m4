# PR-IMPL-018 — Académico: Calificaciones de evaluación y cálculo de notas (backend + UI)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-018` |
| Título | Módulo `academico`: `CalificacionEvaluacion` + motor `CalculoNotas` (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-018.md` |
| ID origen | `DD-UC-018` (`FSD-UC-016`) |
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
Implementa FSD-UC-016 segun docs/design/DD-UC-018.md §2, backend Y frontend
en el mismo prompt:

Backend (modulo academico):
- Aggregate CalificacionEvaluacion (NO llamar Calificacion — ese nombre es
  del Perfil SIE). Unicidad (tenant, evaluacion, estudiante).
- Motor de dominio puro CalculoNotas (ADR-0013 §3.4 / BR-020):
  nota_seccion = round_2d HALF_UP(Σ/n); nota_periodo = round entero HALF_UP
  de Σ secciones COMPLETAS; promedio_gestion = round((Σ periodo_o_cero)/N)
  marcado PROVISIONAL. Sin floor(). Evals ANULADA no cuentan. n=0 →
  seccion INCOMPLETO (omitida del Σ periodo).
- PUT /api/v1/evaluaciones/{id}/calificaciones {items:[{estudianteId,valor}]}
  upsert atómico; GET misma ruta (nómina + valor?).
- GET /materias/{materiaId}/estudiantes/{estudianteId}/nota-provisional?periodoId=
- Validaciones: periodo ABIERTO + eval ACTIVA en escrituras; valor en
  [0, puntajeMaximo]; estudiante en nómina (inscripciones ACTIVA de la
  gestion del periodo ∩ asignaciones curso/paralelo de la materia);
  E_RANGO_INVALIDO, E_ESTUDIANTE_NO_INSCRITO, E_EVALUACION_NO_ACTIVA,
  E_PERIODO_NO_ABIERTO; alcance PROFESOR como DD-UC-017.
- Delta InscripcionRepositoryPort para listar por gestion + pares
  (curso,paralelo). V12 + RLS FORCE + unique.
- actorId = JWT principal; tenantId = TenantContextProvider.

Frontend:
- /academico/materias/:id/evaluaciones/:evaluacionId/calificaciones
  (matriz inscritos × celda; Guardar lote; resumen PROVISIONAL).
- Enlace desde MateriaEvaluacionesPage. data.roles ADMIN+PROFESOR.
- No tocar role.guard.ts.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-018.md (CalificacionEvaluacion + CalculoNotas +
  matriz UI; ver §2/§3). Decisiones usuario: fullstack, notas+motor, matriz.
- FSD: docs/product/FSD.md §4.6.6 (FSD-UC-016).
- ADRs: ADR-0001, 0008, 0009, 0010, 0011, 0012, 0013 (§3.4 fórmula canónica
  35/40 → 37.50 → 93 → 31).
- Precedentes: Evaluacion (DD-UC-017), SeccionEvaluacion, PeriodoEvaluacion,
  AsignacionMateriaCurso, Inscripcion, MateriaAccesoService / alcance PROFESOR.
- Prerrequisito: PR-IMPL-001..017 ejecutados (Evaluaciones API+UI).
- Restricciones: tenantId/actorId NUNCA del cliente; academico NO importa
  identidad; NO floor()/notassie/FSD-UC-001..009; NO TipoEvaluacion; NO
  audit_log; NO tablas de promedios materializados; NO append-only
  (eso es FSD-UC-005); NO loguear PII/valor/rude; NO docs/baseline/**.
```

### 1.4 Reasoning

```text
BACKEND
1. domain: CalificacionEvaluacionId, CalificacionEvaluacion, CalculoNotas,
   NotaProvisional / EstadoSeccionNota, excepciones listadas en DD §2.
   Lombok solo @Getter en aggregates.
2. V12: tabla calificacion_evaluacion (id, tenant_id, evaluacion_id,
   estudiante_id, valor) + unique + RLS FORCE + FKs. Sin backfill.
3. CalificacionEvaluacionRepositoryPort: guardar, listarPorEvaluacionYTenant,
   buscarPorEvaluacionEstudianteYTenant.
4. Delta InscripcionRepositoryPort: listarActivasPorGestionYAsignaciones
   (gestionId + lista (cursoId,paraleloId)).
5. UpsertCalificacionesService: cargar eval+periodo+materia; exigir ABIERTO
   y ACTIVA; construir nómina; validar cada item (rango, inscrito); upsert;
   invocar CalculoNotas para devolver provisional.
6. ObtenerNotaProvisionalService: cargar evals ACTIVA del materia×periodo,
   calificaciones del estudiante, plantilla secciones, N periodos de la
   gestion; CalculoNotas.
7. Controller(s) + ExceptionHandler (422 UNPROCESSABLE_CONTENT).
8. Tests: CalculoNotasTest (canónico + INCOMPLETO + ANULADA ignorada +
   no-floor); servicios Mockito; CalificacionEvaluacionIntegrationTest;
   ModularityTests.

FRONTEND
9. calificacion-evaluacion.model.ts, evaluacion-calificaciones.page.ts.
10. Ruta con data.roles; enlace desde materia-evaluaciones.page.ts.
11. ng build verde.
```

### 1.5 Stop condition

```text
Detente cuando:
- PUT dos notas Saber 35 y 40 + Ser/Hacer/AE completos → GET
  nota-provisional = 37.50 / 93 / 31 PROVISIONAL (N=3).
- valor=46 con max=45 → 422 E_RANGO_INVALIDO.
- Estudiante no inscrito → 422 E_ESTUDIANTE_NO_INSCRITO.
- Periodo no ABIERTO / eval ANULADA → 422.
- PROFESOR no asignado → 404 (no 403). Cross-tenant 404.
- Ningún uso de floor() en el motor genérico.
- mvn test (incl. ModularityTests) y ng build en verde.
NO implementes notassie, floor SIE, TipoEvaluacion, audit_log,
promedios materializados, append-only. NO edites docs/baseline/**.
NO reescribas role.guard.ts.
```

### 1.6 Output

```text
backend/src/main/java/com/edusync/academico/domain/CalificacionEvaluacion*.java
backend/src/main/java/com/edusync/academico/domain/CalculoNotas.java
backend/src/main/java/com/edusync/academico/domain/NotaProvisional.java
backend/src/main/java/com/edusync/academico/application/** (use cases calificacion/nota)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/** (delta)
backend/src/main/resources/db/migration/V12__academico_calificacion_evaluacion.sql
backend/src/test/java/com/edusync/academico/**CalculoNotas*
backend/src/test/java/com/edusync/academico/**Calificacion*
frontend/src/app/features/academico/calificacion-evaluacion.model.ts
frontend/src/app/features/academico/evaluacion-calificaciones.page.ts
frontend/src/app/features/academico/materia-evaluaciones.page.ts (delta)
frontend/src/app/app.routes.ts (delta)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene del cliente — siempre de `TenantContextProvider`.
- `actorId` **nunca** proviene del cliente — siempre del JWT principal.
- `academico` **no** importa `identidad` ni `plataforma`.
- El cálculo de promedios ocurre **solo** en `CalculoNotas` (dominio), nunca en SQL/REST/Angular.
- **MUST NOT** usar `floor()` en el motor genérico.
- Acceso cross-tenant / Profesor no asignado → `404`, nunca `403`.
- `mvn test` (incluye `ModularityTests`) y `ng build` en verde.
- Logs **MUST NOT** registrar `rude`, `valor` de calificación ni PII.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar |
| `E_ACTOR_DESDE_CLIENTE` | El body trae `profesorId`/`actorId` | Rechazar |
| `E_CICLO_MODULO` | `academico` importa `identidad`/`plataforma` | Rechazar |
| `E_FLOOR_EN_GENERICO` | Se usó `floor()` en `CalculoNotas` | Revertir; viola `ADR-0013` |
| `E_CALCULO_FUERA_DOMINIO` | Promedio en SQL/adapter/UI | Revertir (`AGENTS.md` §6) |
| `E_NOMBRE_CALIFICACION_SIE` | Aggregate nombrado `Calificacion` (colisión SIE) | Renombrar a `CalificacionEvaluacion` |
| `E_PROMEDIOS_MATERIALIZADOS` | Tablas de nota_seccion/periodo persistidas | Revertir; on-read |
| `E_NOTASSIE_COLADO` | Se implementó `floor`/SIE/`FSD-UC-001` | Revertir |
| `E_AUDIT_LOG_INVENTADO` | `audit_log` sin resolver punto 5 | Revertir |
| `E_PII_EN_LOG` | Se loguea PII/`rude`/`valor` | Rechazar |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` | Rechazar |
| `E_BASELINE_TOCCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: `tenantId` / `actorId` desde contexto/JWT.
- MUST: motor exclusivo en dominio; canónico 37.50 → 93 → 31.
- MUST: `422 E_RANGO_INVALIDO` fuera de `[0, puntajeMaximo]`.
- MUST: `404` cross-tenant y Profesor no asignado.
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: `floor()`, `notassie`, `TipoEvaluacion`, `audit_log`.
- MUST NOT: `docs/baseline/**`; PII en logs; reescribir `role.guard.ts`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-018` | PR-IMPL-018 | `dev-agent` | `CalificacionEvaluacion`, `CalculoNotas`, `V12`, matriz Angular |
| FSD | `FSD-UC-016` | PR-IMPL-018 | `dev-agent` | Noveno feature de negocio real de `academico` (séptimo fullstack) |
| ADR | `ADR-0013` | PR-IMPL-018 | `dev-agent` | Motor `round` HALF_UP genérico (`BR-020`) |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-018` completo; T1 `ABIERTO`; Saber max 45; dos evals con notas 35 y 40; Ser/Hacer/AE con nota completa; N=3.
- **Output esperado**: `nota_seccion(Saber)=37.50`, `nota_periodo=93`, `promedio_gestion=31 PROVISIONAL`; UI guarda matriz; `mvn test` y `ng build` verdes.

### 6.2 Caso borde

- **Input**: `valor=46` con max 45; o sección sin notas; o eval `ANULADA`.
- **Output esperado**: `422 E_RANGO_INVALIDO`; sección `INCOMPLETO`; eval anulada no cuenta / escritura rechazada.

### 6.3 Caso adversarial

- **Input**: solicitud de usar `floor()`, materializar promedios, o nombrar el AR `Calificacion`.
- **Comportamiento esperado**: rechazo — alternativas descartadas en `DD-UC-018` §3 / `ADR-0013`.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`, `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 21/08/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-018.md` v1.0. Séptimo prompt fullstack (backend + UI) del módulo `academico`. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 21/08/2026 | Rodrigo Aspeti | Ejecución completa: `CalificacionEvaluacion`, `CalculoNotas`, `V12`, matriz UI. Stop condition verde (`mvn test` 235/235, `ng build`). Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 21/08/2026 | aprobado (prompt) | Diseño fullstack; decisiones 1A/2A/3A; ejecución de código pendiente |
| Rodrigo Aspeti | 21/08/2026 | ejecutado | Código + tests + docs vivos sincronizados (`DTP` v1.38, `FSD` v2.14, `PROMPT_MAPPING` v2.38) |
