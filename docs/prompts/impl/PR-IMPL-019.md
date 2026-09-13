# PR-IMPL-019 — Académico: edición sin restricción por ADMIN y visibilidad de Gestión Escolar por rol

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-019` |
| Título | Relajar reglas de flujo de `GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion` + visibilidad por rol (backend + consola Angular) |
| Artefacto origen | `docs/design/DD-UC-019.md` |
| ID origen | `DD-UC-019` (`FSD-UC-012`, `FSD-UC-013`, `FSD-UC-014`) |
| Tipo de prompt | generación + refactor |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 12/09/2026 |
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
Implementa DD-UC-019 (crea ADR-0014) segun docs/design/DD-UC-019.md §2,
backend Y frontend en el mismo prompt:

Backend (modulo academico):
- GestionEscolar: nombre/fechaInicio/fechaFin mutables + actualizarDatos()
  (nulls conservan valor actual, valida fechaFin > fechaInicio);
  cambiarEstado() sin maquina de estados (cualquier transicion).
- PeriodoEvaluacion.cambiarEstado() sin maquina de estados (reabrir CERRADO
  permitido).
- Eliminar 5 excepciones de dominio: EstadoGestionEscolarInvalidoException,
  EstadoPeriodoEvaluacionInvalidoException, PeriodosInmutablesException,
  SeccionesInmutablesException, PeriodoNoSecuencialException.
- PeriodoEvaluacionPolitica: solo exigirSinSolape (quitar hayAbierto/
  exigirMutables). SeccionEvaluacionPolitica: solo exigirSumaCien (quitar
  hayPeriodoNoPendiente/exigirMutables).
- CambiarEstadoPeriodoEvaluacionService: quitar secuencialidad; mantener
  exigirSumaCien al abrir.
- Crear/Actualizar/Eliminar PeriodoEvaluacionService: quitar exigirMutables.
- Crear/Actualizar/Reemplazar SeccionEvaluacionService: quitar freeze
  (y la dependencia ya no usada de PeriodoEvaluacionRepositoryPort).
- Nuevo ActualizarGestionEscolarUseCase/Command/Service:
  PATCH /gestiones-escolares/{id} {nombre?,fechaInicio?,fechaFin?} (ADMIN).
- Nuevo GestionEscolarVisibilidad (package-private, application/service):
  exigirVisible(gestion, actorVeTodas) -> 404 si !actorVeTodas y
  estado != ACTIVA; filtroEfectivo(filtro, actorVeTodas) -> fuerza
  estado=ACTIVA si !actorVeTodas.
- +boolean actorVeTodas en ListarGestionesEscolaresUseCase,
  ObtenerGestionEscolarUseCase, ListarPeriodosEvaluacionUseCase,
  ListarSeccionesEvaluacionUseCase (y sus Service); usar
  GestionEscolarVisibilidad en cada uno.
- GestionEscolarController: Authentication en listar/obtener/listarPeriodos/
  listarSecciones, pasar ActorSeguridad.esAdmin(authentication); +ASESOR en
  @PreAuthorize de los 4 GET; nuevo PATCH /{id}; limpiar
  @ExceptionHandler/@ApiResponse de codigos muertos (E_ESTADO_INVALIDO,
  E_PERIODO_NO_SECUENCIAL, E_PERIODOS_INMUTABLES, E_SECCIONES_INMUTABLES) en
  GestionEscolarController/PeriodoEvaluacionController/
  SeccionEvaluacionController.

Frontend:
- app.routes.ts: las 3 rutas de Gestion Escolar (lista, :id/periodos,
  :id/secciones) -> data.roles ['ADMIN','SECRETARIA','PROFESOR','ASESOR'].
  /nuevo sigue ADMIN-only.
- shell.component.ts: enlace "Gestion Escolar" en condicional propio (no
  agrupado con Usuarios/Cursos).
- gestiones-escolares-list.page.ts: computed esAdmin; ocultar "+ Nueva",
  filtro estado, "Cambiar estado" para no-admin; nuevo dialogo "Editar"
  (PATCH /{id}); transicionesValidas() = todos los estados menos el actual.
- gestion-periodos.page.ts: computed esAdmin; ocultar columna Acciones y
  form "Nuevo periodo" para no-admin; quitar puedeAbrir()/todosPendiente()
  (Abrir si !=ABIERTO, Cerrar si ABIERTO, Eliminar siempre visible admin);
  nuevo dialogo "Editar" (PATCH /periodos-evaluacion/{id}, endpoint ya
  existente).
- gestion-secciones.page.ts: computed esAdmin; quitar congelada() y el
  fetch de periodos que la calculaba; inputs/botones disabled si !esAdmin.
- No reescribir role.guard.ts.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-019.md §2 (decisiones usuario: 1 ACTIVA,
  2 admin bypasea reglas, 3 nuevo PATCH nombre/fechas).
- FSD: docs/product/FSD.md §4.6.2/§4.6.3/§4.6.4 (FSD-UC-012/013/014).
- ADRs: ADR-0001, 0008..0013 vigentes; crear ADR-0014 (relaja ADR-0013
  §3.1.4/3.1.5/§3.2.2, sin superseder el resto).
- Precedentes: GestionEscolar/PeriodoEvaluacion/SeccionEvaluacion
  (DD-UC-008/015/016); ActorSeguridad.esAdmin (EvaluacionController);
  patron 404-no-403 de aislamiento cross-tenant.
- Prerrequisito: PR-IMPL-001..018 ejecutados.
- Restricciones: tenantId NUNCA del cliente; conservar exigirSumaCien,
  exigirSinSolape, PeriodoUnicoException (integridad del motor de calculo,
  ADR-0013 §3.4); NO tocar CalculoNotas/CalificacionEvaluacion (DD-UC-018);
  NO floor()/notassie; NO audit_log (ADR-0009 §3 punto 5 sigue pendiente);
  NO loguear PII; NO docs/baseline/**.
```

### 1.4 Reasoning

```text
BACKEND
1. domain: GestionEscolar (mutable + actualizarDatos), PeriodoEvaluacion
   (cambiarEstado sin validacion); borrar 5 excepciones.
2. application/service: reducir las 2 Politica a la invariante de
   integridad; quitar freeze de los 5 servicios de escritura afectados
   (ajustar constructores que pierden PeriodoEvaluacionRepositoryPort).
3. Nuevo ActualizarGestionEscolarUseCase/Command/Service.
4. Nuevo GestionEscolarVisibilidad; +actorVeTodas en 4 interfaces + 4
   servicios de lectura.
5. GestionEscolarController: Authentication + ASESOR + nuevo PATCH +
   ActualizarGestionEscolarRequest DTO; limpiar exception handlers en los
   3 controladores.
6. mvn -DskipTests compile / test-compile para detectar breakage temprano.
7. Actualizar tests existentes que referenciaban las 5 excepciones/codigos
   eliminados (unit domain, unit aplicacion Mockito, integracion
   Testcontainers) — no borrar cobertura, invertir la aserción (de
   "rechaza" a "permite").
8. mvn test verde (con Docker/Testcontainers).

FRONTEND
9. app.routes.ts + shell.component.ts (roles ampliados).
10. gestiones-escolares-list.page.ts, gestion-periodos.page.ts,
    gestion-secciones.page.ts (esAdmin, dialogos Editar, quitar gating
    obsoleto).
11. ng build verde.

DOCS
12. ADR-0014; DD-UC-019 (ejecutado); PR-IMPL-019 (este); PROMPT_MAPPING;
    dtp-sync sobre DTP.md.
```

### 1.5 Stop condition

```text
Detente cuando:
- Abrir T2 con T1 ABIERTO (sin cerrar T1) -> 200, sin
  E_PERIODO_NO_SECUENCIAL.
- Crear/editar periodo o PUT/PATCH secciones con un periodo ABIERTO o
  CERRADO -> 200/201, sin E_PERIODOS_INMUTABLES / E_SECCIONES_INMUTABLES.
- Transicion directa PLANIFICACION -> CERRADA de GestionEscolar -> 200.
- Suma de secciones != 100 o solape de fechas -> sigue en 422 (invariantes
  de integridad intactas).
- SECRETARIA/PROFESOR/ASESOR listan gestiones -> solo ACTIVA, sin importar
  el filtro enviado; GET detalle/periodos/secciones de una gestion no
  ACTIVA -> 404 (no 403).
- ADMIN sigue viendo y editando cualquier gestion sin restriccion.
- mvn test (incl. ModularityTests) y ng build en verde.
NO toques CalculoNotas/CalificacionEvaluacion. NO implementes floor SIE,
audit_log, TipoEvaluacion. NO edites docs/baseline/**. NO reescribas
role.guard.ts.
```

### 1.6 Output

```text
backend/src/main/java/com/edusync/academico/domain/GestionEscolar.java (delta)
backend/src/main/java/com/edusync/academico/domain/PeriodoEvaluacion.java (delta)
backend/src/main/java/com/edusync/academico/domain/*InvalidoException.java (eliminados)
backend/src/main/java/com/edusync/academico/domain/Periodos/Seccion*InmutablesException.java (eliminados)
backend/src/main/java/com/edusync/academico/domain/PeriodoNoSecuencialException.java (eliminado)
backend/src/main/java/com/edusync/academico/application/port/in/ActualizarGestionEscolar*.java (nuevos)
backend/src/main/java/com/edusync/academico/application/service/ActualizarGestionEscolarService.java (nuevo)
backend/src/main/java/com/edusync/academico/application/service/GestionEscolarVisibilidad.java (nuevo)
backend/src/main/java/com/edusync/academico/application/service/** (delta, 8 servicios)
backend/src/main/java/com/edusync/academico/application/port/in/*UseCase.java (delta, 4 interfaces)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/GestionEscolarController.java (delta)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/ActualizarGestionEscolarRequest.java (nuevo)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/PeriodoEvaluacionController.java (delta)
backend/src/main/java/com/edusync/academico/infrastructure/adapter/in/rest/SeccionEvaluacionController.java (delta)
backend/src/test/java/com/edusync/academico/** (delta, unit + integration)
frontend/src/app/app.routes.ts (delta)
frontend/src/app/shared/layout/shell.component.ts (delta)
frontend/src/app/features/academico/gestiones-escolares-list.page.ts (delta)
frontend/src/app/features/academico/gestion-periodos.page.ts (delta)
frontend/src/app/features/academico/gestion-secciones.page.ts (delta)
docs/adr/0014-relajacion-restricciones-edicion-periodos-secciones-visibilidad-gestion-por-rol.md (nuevo)
```

## 2. Invariantes del prompt

- `tenantId` **nunca** proviene del cliente — siempre de `TenantContextProvider`.
- Se **conservan** las invariantes de integridad del motor de cálculo: suma de secciones = 100, no solape de fechas, ≥1 periodo por gestión.
- Acceso a una gestión no `ACTIVA` por un actor no-admin → `404`, nunca `403`.
- `mvn test` (incluye `ModularityTests`) y `ng build` en verde.
- Logs **MUST NOT** registrar PII.
- **No** se toca `CalculoNotas`/`CalificacionEvaluacion` (`DD-UC-018`).

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_TENANT_DESDE_CLIENTE` | El endpoint acepta `tenantId` del cliente | Rechazar |
| `E_SUMA_CIEN_ELIMINADA` | Se quitó también `exigirSumaCien`/`exigirSinSolape` | Revertir — son invariantes de integridad, no de flujo |
| `E_403_EN_VEZ_DE_404` | Un actor no-admin recibe `403` en vez de `404` al ver una gestión no `ACTIVA` | Corregir — viola el patrón 404-no-403 |
| `E_CALCULO_TOCADO` | Se modificó `CalculoNotas` o `CalificacionEvaluacion` | Revertir; fuera de alcance de `DD-UC-019` |
| `E_ROLE_GUARD_REESCRITO` | Se modificó `role.guard.ts` | Rechazar |
| `E_BASELINE_TOCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: conservar `exigirSumaCien`, `exigirSinSolape`, `PeriodoUnicoException`.
- MUST: `404` (no `403`) para visibilidad `ACTIVA`-only de actor no-admin.
- MUST: `mvn test` + `ng build` en verde, incluyendo `ModularityTests`.
- MUST NOT: tocar `CalculoNotas`/`CalificacionEvaluacion`.
- MUST NOT: `docs/baseline/**`; PII en logs; reescribir `role.guard.ts`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-019` | PR-IMPL-019 | `dev-agent` | Relajación de reglas + visibilidad por rol |
| FSD | `FSD-UC-012`/`013`/`014` | PR-IMPL-019 | `dev-agent` | Reglas de edición/visibilidad actualizadas |
| ADR | `ADR-0014` | PR-IMPL-019 | `dev-agent` | Relaja `ADR-0013` §3.1.4/3.1.5/§3.2.2 |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-019` completo; tenant con gestión `ACTIVA` y otra `CERRADA`; Admin abre T2 con T1 aún `ABIERTO`.
- **Output esperado**: `200 OK`; `SECRETARIA` solo ve la gestión `ACTIVA` al listar; `mvn test` y `ng build` verdes.

### 6.2 Caso borde

- **Input**: `PUT` de secciones con suma 99; `POST` de periodo con fechas solapadas.
- **Output esperado**: `422 E_SUMA_SECCIONES_INVALIDA` / `422 E_PERIODOS_SOLAPADOS` (invariantes de integridad intactas).

### 6.3 Caso adversarial

- **Input**: solicitud de eliminar también la validación de suma 100, o de exponer el histórico completo a `PROFESOR`.
- **Comportamiento esperado**: rechazo — alternativas descartadas en `ADR-0014` §2/§3.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `mvn_test_pass`, `modularity_tests_pass`, `ng_build_pass`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 12/09/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-019.md` v1.0. Estado: **Aprobado (prompt)**, ejecución pendiente. | Sonnet |
| v0.2 | 12/09/2026 | Rodrigo Aspeti | Ejecución completa: relajación de reglas + visibilidad por rol + `ADR-0014`. Stop condition verde (`mvn test` 238/238, `ng build`). Estado: **Ejecutado**. | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 12/09/2026 | ejecutado | Código + tests + docs vivos sincronizados |
