# PR-IMPL-029 — Parámetros del periodo y cobertura docente al abrir

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-029` |
| Título | `ParametroPeriodo` + gates de apertura de `FSD-UC-009` |
| Artefacto origen | `docs/design/DD-UC-029.md` |
| ID origen | `DD-UC-029` (`FSD-UC-009`) |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 29/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Ejecutado** 29/09/2026 |

> Vive en `docs/prompts/impl/`. No tocar `docs/baseline/**`. No restaurar la apertura secuencial ni el freeze de `ADR-0014`.

## 1. Anatomía del prompt

### 1.1 Role

```text
Eres un Senior Full-Stack Engineer con experiencia en Java 25 / Spring Boot
4.1.0 (arquitectura hexagonal, Spring Data JPA, Spring Modulith) y Angular 21
(standalone) en el proyecto EduSync.
```

### 1.2 Task

```text
Implementa el delta de FSD-UC-009 descrito en docs/design/DD-UC-029.md §2,
backend y frontend en el mismo prompt. No reimplementes gestion, periodos,
secciones ni asignaciones.

Backend (modulo academico):
- Aggregate ParametroPeriodo (rangoMin, rangoMax, reglaCombinacion =
  PROMEDIO_SIMPLE). Lombok solo @Getter.
- PUT/GET /api/v1/periodos-evaluacion/{id}/parametros, reemplazo atomico.
  Periodo distinto de PENDIENTE -> 422 E_PARAMETROS_INMUTABLES.
  Conjunto de secciones distinto al de la gestion -> 422
  E_PARAMETROS_INCOMPLETOS. Seccion ajena -> 404 E_SECCION_NO_ENCONTRADA.
  rangoMax > seccion.nota -> 422 E_RANGO_INVALIDO.
  Regla distinta de PROMEDIO_SIMPLE -> 422 E_REGLA_NO_SOPORTADA.
- En CambiarEstadoPeriodoEvaluacionService, al pasar a ABIERTO: ademas de
  la suma 100 existente, exigir parametros completos (422
  E_PARAMETROS_INCOMPLETOS) y cobertura docente (409 E_MATERIA_SIN_DOCENTE):
  toda materia del tenant con AsignacionMateriaCurso tiene al menos una
  AsignacionMateriaProfesor. Sin asignaciones a curso, no bloquea.
- listarPorTenant en los dos puertos de asignacion.
- Flyway V13__academico_parametro_periodo.sql con tenant_id y RLS FORCE.

Frontend: bloque Parametros en gestion-periodos.page.ts. Solo lectura si el
periodo no esta PENDIENTE. Sin ruta nueva.
```

### 1.3 Context

```text
- Fuente: docs/design/DD-UC-029.md.
- FSD: docs/product/FSD.md §4.5 (FSD-UC-009). BR-007 y ADR-0002 exigen
  parametros inmutables fuera de PENDIENTE. BR-006 / RB-05 (apertura
  secuencial) NO se implementan: ADR-0014 los elimino.
- ADR-0013: un solo motor; el peso es SeccionEvaluacion.nota; no hay enum
  de dimensiones ni floor() en este slice.
- Precedentes: PeriodoEvaluacion, CambiarEstadoPeriodoEvaluacionService,
  SeccionEvaluacion, AsignacionMateriaCursoRepositoryPort,
  AsignacionMateriaProfesorRepositoryPort, gestion-periodos.page.ts.
- Restricciones: tenantId siempre desde TenantContextProvider; academico
  no importa identidad; no audit_log; no notificaciones; no
  E_CENTRALIZADORES_INCOMPLETOS; no tocar CalculoNotas; no loguear PII;
  no modificar docs/baseline/**.
```

### 1.4 Reasoning

```text
1. Confirmar que ADR-0014 sigue vigente y que el servicio de cambio de
   estado no reintroduce E_PERIODO_NO_SECUENCIAL.
2. Modelar ParametroPeriodo como aggregate independiente, unido a la
   seccion de la gestion, no a un enum ministerial.
3. Validar el conjunto contra las secciones vigentes dentro de la
   transaccion del PUT y otra vez al abrir.
4. Anadir listarPorTenant y comparar materiaIds en aplicacion.
5. Tests de dominio, servicio e integracion antes de dar el slice por
   cerrado. ng build de la pantalla de periodos.
```

### 1.5 Stop condition

```text
Detente cuando mvn test y ng build esten en verde, V13 tenga RLS FORCE,
el PUT rechace un periodo no PENDIENTE, y la apertura rechace parametros
incompletos y materia con curso sin profesor. No abras PR si aparece
codigo de apertura secuencial, floor() o una tabla en notassie.
```

### 1.6 Output

```text
Codigo Java en com.edusync.academico, V13, delta de
gestion-periodos.page.ts, tests JUnit 5. Sin editar docs/baseline/**.
```

## 2. Invariants

- El peso de la seccion no se duplica en `parametro_periodo`.
- `floor()` no aparece en este slice. El motor de `FSD-UC-016` no se modifica.
- Ninguna transicion de periodo vuelve a exigir que el predecesor este `CERRADO`.
- `tenant_id` y RLS `FORCE` en la tabla nueva.
- Sin PII en logs. Sin `audit_log` (punto 5 de `ADR-0009` sigue pendiente).

## 3. Failure modes

- `E_SECUENCIALIDAD_RESTAURADA`: el diff reintroduce `E_PERIODO_NO_SECUENCIAL` o `E_TRIMESTRE_PREVIO_ABIERTO` → revertir; contradice `ADR-0014`.
- `E_MOTOR_PARALELO`: aparece un enum de dimensiones o `floor()` en `academico` → rechazar; contradice `ADR-0013`.
- `E_PARAMETROS_EDITABLES_ABIERTO`: un `PUT` con periodo `ABIERTO` o `CERRADO` persiste → corregir (`BR-007`).
- `E_RLS_FALTANTE`: `V13` sin `tenant_id` o sin `FORCE` → rechazar la migracion.
- `E_BASELINE_TOCADO`: cualquier edicion bajo `docs/baseline/**` → revertir.

## 4. Trazabilidad

| Campo | Valor |
|-------|-------|
| FSD-UC | `FSD-UC-009` |
| Design Doc | `DD-UC-029` |
| ADRs | `ADR-0002`, `ADR-0013`, `ADR-0014` |
| Depende de | `PR-IMPL-012`, `PR-IMPL-015`, `PR-IMPL-016`, `PR-IMPL-019` |

## 5. Registro de cambios del prompt

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 29/09/2026 | Rodrigo Aspeti | Creación a partir de `docs/design/DD-UC-029.md`. Estado: **Borrador — pendiente de aprobación humana**. |
