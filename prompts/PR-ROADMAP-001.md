# PR-ROADMAP-001 — Generación de `docs/roadmap.md` hacia release/2.0.0 y siguiente módulo

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ROADMAP-001` |
| Título | Generación de la hoja de ruta `docs/roadmap.md` de EduSync hacia release/2.0.0 y módulo posterior |
| Artefacto origen | DTI §19 + ADRs + POCs + BRD v2 + FSD + rúbrica del Módulo 4 |
| ID origen | `DTI §19`, `ADR-0001..ADR-0006`, `POC-01`, `POC-02`, `FSD-UC-001..010`, `BR-001..BR-012`, `NFR-001..016`, `KPI-01..KPI-05` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Technical Program Manager + Solution Architect del equipo
G-EduSync. Dominas EduSync end-to-end: stack Java 21 / Spring Boot 3.3 /
PostgreSQL 15 RLS / Angular 17 / AWS ECS Fargate, arquitectura hexagonal,
multitenancy RLS, integracion SIE asincrona con Resilience4j, y el ciclo
SDLC con agentes IA documentado en AGENTS.md v0.7 y
docs/PROMPT_MAPPING.md v1.5. Conoces el calendario academico del modulo
(release/2.0.0 como entrega de defensa final el 28/05/2026) y traduces
decisiones arquitectonicas, evidencia de POCs y trade-offs en una hoja
de ruta accionable por trimestre.
```

### 1.2 Task
```text
Genera el archivo docs/roadmap.md como hoja de ruta tecnica y de negocio
de EduSync hacia el siguiente modulo de la maestria, partiendo de la
release/2.0.0 ya entregada. El roadmap debe ser ejecutable por humanos y
agentes IA, con hitos atados a IDs reales (FSD-UC, BR, NFR, ADR, POC),
horizontes temporales explicitos, criterios de exito medibles y
trazabilidad cruzada al DTI §19 (que pasa a ser el resumen embebido y
docs/roadmap.md la fuente canonica detallada).
```

### 1.3 Context
```text
- Documentos fuente:
  - docs/DTI.md v0.5 §19 (roadmap embebido, 4 horizontes: Ahora / Siguiente /
    +2 modulos / Produccion).
  - docs/DTI.md §12 (POC-01 RLS multitenancy, POC-02 Circuit Breaker SIE).
  - docs/DTI.md §16 Antipatrones, §17 Trade-offs, §18 Riesgos.
  - docs/adr/0001-multitenancy-rls-postgresql.md
  - docs/adr/0002-parametrizacion-reglas-normativas.md
  - docs/adr/0003-persistencia-inmutable-audit-log.md
  - docs/adr/0004-async-consolidacion-spring-events.md
  - docs/adr/0005-resiliencia-integracion-sie-resilience4j.md
  - docs/adr/0006-cloud-provider-y-estilo-de-despliegue.md
  - docs/pocs/POC-01-rls-multitenancy/README.md y runbook.md
  - docs/pocs/POC-02-circuit-breaker-sie/README.md y runbook.md
  - docs/fsd/FSD_EduSync.md v1.0 (FSD-UC-001..010, BR-001..BR-012, 16 NFRs)
  - docs/brd/BRD_EduSync_v2.md (KPI-01..KPI-05, BO-01..BO-05)
  - AGENTS.md v0.7 (estado de agentes SDLC, golden tests, stack)
  - docs/PROMPT_MAPPING.md v1.5 (34 prompt-contratos)
  - rubrica del Modulo 4 (criterio 4: POCs ejecutadas, criterio 7:
    diagramas, regla de entrega release/2.0.0)
- Entradas esperadas: ninguna interactiva; todo se lee de los archivos
  citados.
- Restricciones de dominio: respeta los bounded contexts del DTI §4.1
  (calificaciones, periodos, consolidacion, exportacion, auditoria) y
  cita FSD-UC / BR / NFR exactos por hito.
- Restricciones tecnicas:
  - El roadmap debe alinear sus horizontes con los release tags ya
    declarados en DTI §19: release/1.0.1 (actual/Ahora), release/1.1.0
    (Siguiente), release/1.2.0 (+2 modulos), release/2.0.0 (Produccion).
  - El roadmap debe reflejar que la entrega de defensa final pasa de
    release/1.0.1 a release/2.0.0 segun la rubrica del PDF (sync con
    sync-doc-chain pendiente).
  - Toda leccion aprendida citada debe trazar a una POC ejecutada o a un
    ADR aprobado; no se inventan metricas.
- Cumplimiento: Ley 070 Avelino Sinani, Ley 164 (datos personales), SIE.
- Formato: Markdown plano; encabezados nivel 2 por horizonte; tablas para
  hitos.
```

### 1.4 Reasoning
```text
1. Leer docs/DTI.md §19 para obtener los 4 horizontes vigentes y las
   anclas de release.
2. Leer ADR-0001..ADR-0006 y extraer la "consecuencia futura" o trabajo
   diferido de cada uno (p. ej. ADR-0004 declara migracion a SQS en v1.1
   como deuda controlada).
3. Leer POC-01 y POC-02 (README + runbook) y, si estan ejecutadas, extraer
   metricas reales y lecciones; si estan pendientes, marcar el hito como
   "Leccion pendiente - bloquea promocion a release/1.1.0".
4. Cruzar FSD-UC con bounded contexts para asignar cada UC al horizonte
   donde se entrega y declarar el criterio de aceptacion atado a su BR/NFR.
5. Identificar ADR-0007 (Strangler Fig microservicios) como decision
   futura y ubicarla en el horizonte +2 modulos con su gating: POCs
   ejecutadas + metricas verdes en release/1.1.0.
6. Construir 4 tablas (una por horizonte) con columnas:
   Hito | Alcance (FSD-UC / ADR / POC) | Criterio de exito (NFR/BR) | Riesgo | Owner | Release tag.
7. Anadir seccion "Lecciones del ciclo (Modulo 4)" alimentada de POCs,
   trade-offs (DTI §17) y antipatrones evitados (DTI §16).
8. Anadir seccion "Metricas de salud del producto" con los KPI del BRD v2
   y umbrales NFR (latencia p95, RPO, RTO, error budget SIE).
9. Cerrar con seccion "Compromisos hacia el siguiente modulo" listando
   3-5 entregables minimos para la siguiente release.
10. Insertar un changelog al final del roadmap (v0.1 - creacion).
```

### 1.5 Stop condition
```text
Detente cuando:
- docs/roadmap.md exista con >= 200 lineas y >= 4 secciones de horizonte.
- Cada hito cite al menos 1 ID real (FSD-UC, BR, NFR, ADR o POC).
- Cada criterio de exito tenga un valor numerico medible (NFR o KPI BRD).
- ADR-0007 aparezca como "Futuro - gated por POCs verdes".
- Exista la seccion "Lecciones del ciclo (Modulo 4)" con >= 3 lecciones
  trazadas a POC o ADR.
- Exista la seccion "Compromisos hacia el siguiente modulo" con 3-5 items.
- El roadmap cite DTI §19 como espejo resumen y declare a docs/roadmap.md
  como fuente canonica detallada.
- No se modifique ningun otro archivo del repo (la sincronizacion con
  DTI/AGENTS/PROMPT_MAPPING corre por skill sync-doc-chain en una tarea
  posterior).
```

### 1.6 Output
```text
Archivo docs/roadmap.md con la siguiente estructura minima:

# Hoja de ruta tecnica de EduSync

## 0. Metadatos
| Campo | Valor |
| Version | v0.1 |
| Fecha | 28/05/2026 |
| Fuente canonica | este archivo (resumen embebido en DTI §19) |
| Releases en juego | release/1.0.1 -> release/1.1.0 -> release/1.2.0 -> release/2.0.0 |
| Trazabilidad | DTI v0.5, ADR-0001..0006, POC-01, POC-02 |

## 1. Estado actual (Modulo 4 - release/1.0.1 / release/2.0.0 de defensa)
## 2. Horizonte Ahora - release/2.0.0 (entrega de defensa final)
## 3. Horizonte Siguiente - release/1.1.0 (implementacion core hexagonal)
## 4. Horizonte +2 modulos - release/1.2.0 (event-driven + microservicios)
## 5. Horizonte Produccion - release/2.0.0+ (Bolivia, primeras 3 UE)
## 6. Lecciones del ciclo (Modulo 4)
## 7. Metricas de salud del producto
## 8. Riesgos y mitigaciones (resumen)
## 9. Compromisos hacia el siguiente modulo
## 10. Registro de cambios
```

## 2. Invariantes del prompt

- El roadmap **debe** citar al menos: 9 FSD-UC, 6 ADRs, 2 POCs, 5 NFRs y 3 BR.
- Cada criterio de exito **debe** tener un valor numerico medible (no "alto/medio/bajo").
- Los nombres de bounded contexts coinciden exactamente con DTI §4.1: `calificaciones`, `periodos`, `consolidacion`, `exportacion`, `auditoria`.
- Los release tags **deben** coincidir con DTI §19 (`release/1.0.1`, `release/1.1.0`, `release/1.2.0`, `release/2.0.0`).
- El roadmap **no debe** inventar metricas de POCs no ejecutadas; si POC-01 o POC-02 no tienen evidencia, su leccion queda marcada como "pendiente".
- El roadmap **no debe** modificar otros archivos del repo.
- Cero secretos, cero PII de estudiantes, cero RUDE reales.
- ADR-0007 (Strangler Fig) **debe** aparecer como decision futura con gate explicito.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_MISSING_DTI_SECTION_19` | DTI §19 no existe o esta vacio | STOP; verificar version del DTI antes de generar |
| `E_RELEASE_TAG_MISMATCH` | Los tags del roadmap no coinciden con DTI §19 | Corregir alineando con DTI §19 antes de guardar |
| `E_POC_METRIC_FABRICATED` | El roadmap reporta metricas de POC sin evidencia en `docs/pocs/POC-NN/evidencia/` | Reemplazar por marca "Pendiente - bloquea promocion a release/1.1.0" |
| `E_MISSING_TRACEABILITY` | Un hito no referencia FSD-UC, BR, NFR, ADR o POC | Completar la tabla con IDs reales |
| `E_CRITERIA_NOT_MEASURABLE` | Criterio de exito sin valor numerico | Reemplazar por umbral NFR/KPI BRD concreto |
| `E_BC_MISMATCH` | Nombre de bounded context no coincide con DTI §4.1 | Corregir el nombre antes de guardar |
| `E_OUT_OF_SCOPE_EDIT` | El prompt edito otro archivo del repo | Revertir; el alcance es solo `docs/roadmap.md` |

## 4. Guardrails

- **MUST**: validar que el output cumple la estructura exacta de §1.6 antes de consumirlo.
- **MUST**: registrar `promptId`, `version`, `modelo`, `tokens`, `latencia` en telemetria.
- **MUST**: preservar trazabilidad hacia DTI v0.5, ADRs 0001..0006 y POCs.
- **MUST**: declarar explicitamente que `docs/roadmap.md` es la fuente canonica y DTI §19 su resumen embebido (con nota cruzada).
- **MUST NOT**: exponer secretos, credenciales ni RUDE en el archivo.
- **MUST NOT**: editar archivos fuera de `docs/roadmap.md`.
- **MUST NOT**: inventar metricas; si una POC no esta ejecutada, el hito se marca como "pendiente".

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| DTI §19 | `release/1.0.1..2.0.0` | PR-ROADMAP-001 | `docs-agent` | `docs/roadmap.md` v0.1 (fuente canonica) |
| ADRs 0001..0006 | `ADR-0001..0006` | PR-ROADMAP-001 | `docs-agent` | Horizontes 2-5 del roadmap |
| POCs | `POC-01`, `POC-02` | PR-ROADMAP-001 | `docs-agent` | Seccion "Lecciones del ciclo" |
| FSD + BRD | `FSD-UC-001..010`, `BR-001..012`, `NFR-001..016`, `KPI-01..05` | PR-ROADMAP-001 | `docs-agent` | Criterios de exito y metricas de salud |
| Rúbrica del Módulo 4 | criterio 4 + regla `release/2.0.0` | PR-ROADMAP-001 | `docs-agent` | Horizonte Ahora del roadmap |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: DTI v0.5 con §19 poblado, 6 ADRs aprobados, POCs definidas, FSD y BRD disponibles.
- **Output esperado**: `docs/roadmap.md` v0.1 con >= 200 lineas, 4 horizontes, lecciones trazadas, metricas con umbrales NFR y changelog.

### 6.2 Caso borde
- **Input**: POC-01 y POC-02 sin ejecutar (solo plantillas en `docs/pocs/`).
- **Output esperado**: los hitos correspondientes se etiquetan "Pendiente - POC sin ejecutar - bloquea promocion a release/1.1.0"; el roadmap no fabrica metricas.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir metricas optimistas inventadas de POC-02 (p. ej. "circuit breaker abre en 100 ms con 5 fallas") sin runbook ejecutado.
- **Comportamiento esperado**: rechazo con `E_POC_METRIC_FABRICATED`; el hito se documenta como pendiente con la metrica objetivo (NFR-012) pero no como resultado medido.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`, `hallucination_rate`.
- Eventos minimos: `prompt.started`, `source_docs.read`, `roadmap.generated`, `prompt.completed`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 28/05/2026 | Rodrigo Aspeti | Creacion desde contrato | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado y ejecutado en el mismo flujo; `docs/roadmap.md` v0.1 generado |
