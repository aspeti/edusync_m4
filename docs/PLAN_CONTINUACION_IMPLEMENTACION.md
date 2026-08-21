# Plan de continuación — EduSync

**Revisión de documentación y hoja de ruta para los features pendientes**

| Campo | Valor |
|-------|-------|
| Fecha de la revisión | 21/08/2026 |
| Autor | Claude (Cowork), a pedido de Rodrigo Aspeti |
| Alcance | `docs/roadmap.md`, `docs/product/DTP.md` (v1.22), `docs/product/FSD.md`, `AGENTS.md`, `CLAUDE.md`, `ADR-0009`, árbol `backend/src` y `frontend/src` |
| Naturaleza de este documento | Complementario, **no** sustituye a `docs/product/DTP.md` (fuente de verdad de estado) ni a `docs/roadmap.md` (fuente de verdad de la hoja de ruta). No está pensado para que `dtp-sync` lo procese automáticamente — es un análisis externo para decidir los próximos pasos. |

---

## 1. Resumen del estado actual

EduSync ya tiene una cadena documental madura y coherente: el baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`) permanece intacto, y la capa viva de `release/3.0.0` avanza siguiendo un ciclo disciplinado — `FSD-UC` vivo → Design Doc (`DD-UC-NNN`) → prompt de implementación (`PR-IMPL-NNN`) → ejecución de código → sincronización en `docs/product/DTP.md` vía el skill `dtp-sync`. Ese ciclo se ha repetido once veces (`DD-UC-001`..`011`, `PR-IMPL-001`..`011`, los once ya ejecutados) y hoy sostiene 134 tests de backend y un build de Angular en verde.

De los 16 casos de uso funcionales del FSD vivo (`FSD-UC-001`, `003`, `004`, `005`, `009`, `011`..`021`), cuatro están completos de punta a punta — backend y UI: **Gestión de Tenants** (`FSD-UC-011`), **Usuarios y Roles / login** (`FSD-UC-021`), **Gestión Escolar** (`FSD-UC-012`) y **Cursos y Paralelos** (`FSD-UC-017`). Los doce restantes siguen sin `Design Doc`, y entre ellos está el corazón funcional del producto: el módulo `notassie` (registro de calificaciones, consolidación, exportación SIE, modificación retroactiva) todavía no tiene una sola línea de dominio, solo el `package-info.java` que dejó el bootstrap inicial.

Antes de proponer el plan de implementación, esta revisión también encontró varias brechas de **higiene documental y técnica** que conviene cerrar primero, porque condicionan la fiabilidad de cualquier plan que se construya sobre `docs/product/DTP.md`.

---

## 2. Mapa de la documentación (diseño propuesto)

La documentación del repositorio ya está bien estratificada; lo que falta es hacerlo explícito en un solo lugar y mantener la vigencia de cada capa. Se propone este mapa como referencia:

| Capa | Ubicación | Rol | Vigencia | Quién la actualiza |
|------|-----------|-----|----------|---------------------|
| Baseline congelado (M4) | `docs/baseline/{DTI,BRD,MRD,PRD,FSD}*.md` | Fotografía técnica evaluada de M4, tag `release/2.0.0` | **Inmutable** — protegida por `CODEOWNERS` y `.cursor/rules/baseline-congelado.mdc` | Nadie (solo lectura) |
| Producto vivo | `docs/product/{BRD,PRD,FSD}.md` | Copias editables del contrato funcional vigente | Vivo, crece con cada `ADR` de dominio | Manual + skill `sync-doc-chain` |
| Tracking de implementación | `docs/product/DTP.md` | **Fuente de verdad del estado real de cada `FSD-UC`** (qué está hecho, con qué tests, con qué Design Doc) | Vivo, v1.22 al 21/08/2026 | Skill `dtp-sync`, tras cada ejecución de código |
| Hoja de ruta estratégica | `docs/roadmap.md` | Horizontes, KPIs, riesgos, compromisos por release | Vivo — **pero desactualizado** (ver hallazgo H1) | Skill `sync-doc-chain` / manual |
| Decisiones arquitectónicas | `docs/adr/000N-*.md` | Registro formal de decisiones irreversibles o de alto impacto | Append-only, 11 ADRs aceptados | Skill `adr-edusync` |
| Diseño de feature | `docs/design/DD-UC-NNN.md` | Diseño técnico de un `FSD-UC` antes de generar código | Uno por feature, 11 hasta ahora | Skill `feature-design-doc` |
| Prompt de implementación | `docs/prompts/impl/PR-IMPL-NNN.md` | Contrato ejecutable que produce el código real | Uno por `DD-UC`, 11 hasta ahora | Skill `feature-design-doc` |
| Catálogo de prompts | `docs/PROMPT_MAPPING.md` | Índice trazable de todos los prompts-contrato del repo (v2.21, 48 contratos) | Vivo, crece con cada `PR-*` | Skill `update-prompt-mapping` |
| Gobierno de agentes | `AGENTS.md` | Reglas invariantes de dominio, guardrails, stack autoritativo | Vivo, v0.32 — **sync pendiente** (ver H4) | Skill `dtp-sync` (paso final) |

Esta tabla en sí misma es el "diseño de la documentación" que se pidió: deja explícito qué documento manda sobre qué pregunta, para que nadie tenga que adivinarlo releyendo el histórico de `DTP.md`.

---

## 3. Hallazgos (brechas a cerrar antes o en paralelo al plan de features)

**H1 — `docs/roadmap.md` está desactualizado.** Su versión v0.3 es del 28/05/2026 y describe el cierre de M4; no refleja que hoy (21/08/2026, `DTP.md` v1.22) ya existen cuatro `FSD-UC` completos y que el módulo `academico` tiene dos features reales en producción de código. El roadmap sigue siendo la referencia de KPIs, riesgos y horizontes de release, pero su §1 "Estado actual" ya no es cierto. Conviene decidir explícitamente si se actualiza manualmente, se corre `sync-doc-chain`, o se declara que `DTP.md` es desde ahora la única fuente operativa de estado y el roadmap queda solo como referencia estratégica de horizontes (sin datos de avance).

**H2 — Diagrama duplicado sin resolver.** El propio `roadmap.md` §2 pidió, desde el cierre de M4, eliminar `docs/diagrams/estados.cargarnotas.mmd` por ser un duplicado con drift de `docs/diagrams/estados_cargar_notas.mmd`. Ambos archivos siguen presentes (9.8 KB y 13 KB respectivamente, contenido distinto), lo que significa que cualquier agente o persona que abra el diagrama equivocado trabajará sobre una versión desactualizada del flujo de estados de calificaciones — justo el flujo que se va a diseñar en el próximo paso del plan.

**H3 — Seis prompts ejecutados sin commit real.** Según el checklist de `DTP.md` §A.4, `PR-IMPL-006`..`PR-IMPL-011` ya se ejecutaron y verificaron (tests en verde) pero no tienen commit formal en git; solo `PR-IMPL-001`..`005` están commiteados. Es trabajo real, verificado, pendiente de versionar — riesgo de pérdida si el working tree se corrompe.

**H4 — `AGENTS.md` con sync pendiente.** El checklist de `DTP.md` marca sin cerrar la actualización de `AGENTS.md` (v0.32) tras `DD-UC-011`/`PR-IMPL-011`.

**H5 — `checkstyle` nunca estuvo en verde.** `mvn checkstyle:check` falla con 1073 violaciones porque `pom.xml` usa el ruleset por defecto (`sun_checks.xml`) en vez de uno acorde a Google Java Style, que es lo que exige `AGENTS.md` §5. Se detectó durante `DD-UC-005` (04/08/2026) y quedó anotado como "tarea de seguimiento dedicada" sin ejecutar todavía. Cuantas más clases se agreguen al módulo `academico` y al futuro `notassie`, más caro será corregirlo después.

**H6 — POCs bloqueantes sin ejecutar.** `docs/pocs/` no existe en el repositorio: `POC-01` (RLS multitenancy) y `POC-02` (Circuit Breaker SIE) siguen sin evidencia real, pese a que `roadmap.md` §9 las marca como condición explícita para promover de `release/2.0.0` a `release/1.1.0`. El skill `poc-runner-edusync` está documentado en `CLAUDE.md` pero no se ha invocado todavía.

**H7 — El bloqueador de diseño más importante: `ADR-0009` §3.** El propio ADR deja cinco puntos pendientes de definición, y el `FSD.md` vivo es explícito en que **ninguno debe implementarse en código sin resolverlos primero**:

1. Reconciliación entre el modelo fijo de Bolivia (`GestionAcademica`/`ParametroAcademico`, del baseline) y el modelo genérico configurable ya construido (`GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion`).
2. Generalización de la secuencialidad de apertura de periodos (hoy `RB-05` exige 3 trimestres secuenciales fijos) y del promedio final a N periodos.
3. Criterio de redondeo del cálculo de notas en el modelo genérico (si `floor()` sigue siendo el default o se abre a otras estrategias).
4. Validación de que la suma de pesos porcentuales de las secciones de evaluación sea 100 %.
5. Gobernanza (auditoría/inmutabilidad) de los módulos nuevos.

Esto no es un detalle menor: es la razón concreta por la que el módulo `notassie` — el que resuelve el problema de negocio descrito en el `README.md` (triple digitación, cuello de botella del SIE, notas alteradas) — sigue en 0 % de implementación mientras `academico` ya avanzó dos features completos. Resolver estos cinco puntos es, con diferencia, el paso de mayor apalancamiento del plan.

---

## 4. Inventario de features (los 16 `FSD-UC` vivos)

| `FSD-UC` | Nombre | Módulo | Estado (`DTP.md` v1.22) | Depende de |
|----------|--------|--------|--------------------------|------------|
| `FSD-UC-011` | Gestión de Tenants y Suscripciones | `plataforma` | **Completo** (API + UI + filtros) | — |
| `FSD-UC-021` | Usuarios y Roles / login | `identidad` | **Completo** (backend + UI + filtros) | `FSD-UC-011` (tenant activo) |
| `FSD-UC-012` | Gestión Escolar | `academico` | **Completo** (backend + UI) | `FSD-UC-011` |
| `FSD-UC-017` | Cursos y Paralelos | `academico` | **Completo** (backend + UI) | `FSD-UC-011` |
| `FSD-UC-018` | Gestión de Materias | `academico` | Pendiente, sin `DD-UC` | `FSD-UC-017` (Curso) |
| `FSD-UC-019` | Gestión de Profesores | `academico`/`identidad` | Pendiente (parcial: el rol `PROFESOR` ya existe vía `FSD-UC-021`; falta el endpoint de asignaciones) | `FSD-UC-018` |
| `FSD-UC-020` | Estudiantes e Inscripciones | `academico` | Pendiente, sin `DD-UC` | `FSD-UC-012`, `FSD-UC-017` |
| `FSD-UC-013` | Configuración de Periodos de Evaluación | `academico` | Pendiente — **bloqueado por H7 (puntos 1–2)** | `FSD-UC-012` |
| `FSD-UC-014` | Configuración de Secciones de Evaluación | `academico` | Pendiente — **bloqueado por H7 (punto 4)** | `FSD-UC-013` |
| `FSD-UC-015` | Evaluaciones y Tipos de Evaluación | `academico` | Pendiente | `FSD-UC-014`, `FSD-UC-018` |
| `FSD-UC-016` | Cálculo de Notas configurable | `academico`/`notassie` | Pendiente — **bloqueado por H7 (punto 3)** | `FSD-UC-015` |
| `FSD-UC-009` | Administración de periodos académicos institucionales (perfil Bolivia) | `notassie` | Pendiente — **bloqueado por H7 (punto 1)** | — |
| `FSD-UC-001` | Registro descentralizado de calificaciones por dimensión | `notassie` | Pendiente — **bloqueado por H7** | `FSD-UC-009`/`013`, `FSD-UC-018`, `FSD-UC-020` |
| `FSD-UC-003` | Consolidación algorítmica de centralizadores | `notassie` | Pendiente | `FSD-UC-001` |
| `FSD-UC-004` | Exportación masiva al SIE por RUDE | `notassie` | Pendiente | `FSD-UC-003` |
| `FSD-UC-005` | Autorización jerárquica de modificación retroactiva | `notassie` | Pendiente | `FSD-UC-003` |

---

## 5. Plan de pasos a seguir

El orden respeta las dependencias reales de datos (no se puede registrar una calificación sin materia ni estudiante inscrito) y el propio criterio del proyecto de no codificar nada sobre `ADR-0009` §3 sin resolverlo antes.

**Paso 0 — Higiene documental y técnica (antes de tocar código nuevo).**
Cerrar H2 (eliminar el diagrama `.mmd` duplicado, conservando `estados_cargar_notas.mmd` como canónico), H3 (commit real de `PR-IMPL-006`..`011`), H4 (correr el paso final de `dtp-sync` para sincronizar `AGENTS.md` a v0.33) y decidir el tratamiento de H1 (actualizar `roadmap.md` o declarar `DTP.md` como única fuente operativa). Es trabajo de una sesión, sin dependencias, y evita que el resto del plan se construya sobre documentación con drift.

**Paso 1 — Resolver `ADR-0009` §3 (el bloqueador crítico).**
Usar el skill `adr-edusync` para producir un ADR de seguimiento (`ADR-0013` sería el siguiente número libre) que decida explícitamente los cinco puntos de H7. La decisión más estructural es la primera: si el perfil Bolivia (`GestionAcademica`/`ParametroAcademico`, `FSD-UC-009`) se implementa como una instancia particular del modelo genérico ya construido (`GestionEscolar` con 3 `PeriodoEvaluacion` fijos y 5 `SeccionEvaluacion` fijas — Ser/Saber/Hacer/Decidir/Autoevaluación), o si conviven dos modelos de datos paralelos. La primera opción evita duplicar el motor de consolidación y de exportación SIE en dos rutas de código distintas, pero exige confirmar que la secuencialidad y los rangos ministeriales fijos se puedan expresar como configuración dentro del modelo genérico sin perder las invariantes ya validadas (RUDE, `floor`, `audit_log` inalterable). Ningún `DD-UC` de `notassie` debería abrirse antes de cerrar este ADR.

**Paso 2 — Prerrequisitos de datos para poder registrar una nota.**
Con el ADR cerrado, construir `FSD-UC-018` (Materias, con asignación a Curso/Paralelo y a Profesor) y `FSD-UC-020` (Estudiantes e Inscripciones, con RUDE) siguiendo el mismo patrón ya probado en `DD-UC-008`/`DD-UC-010` (backend primero con filtros y paginación reutilizando `DD-UC-007`, UI en un Design Doc de seguimiento). `FSD-UC-019` (Profesores) se cierra casi gratis en este mismo paso: solo falta el endpoint `GET /profesores/{id}/asignaciones` una vez que `FSD-UC-018` existe, porque el alta del usuario con rol `PROFESOR` ya está resuelta desde `FSD-UC-021`.

**Paso 3 — La columna vertebral (la que el propio `roadmap.md` §3/§9 declara como prioridad núcleo).**
Implementar, en este orden, el equivalente resuelto en el Paso 1 de `FSD-UC-009`/`FSD-UC-013` (periodos), luego `FSD-UC-001` (registro de calificaciones) y `FSD-UC-003` (consolidación algorítmica con truncado `floor` — vigilar el golden test `FloorTest` que el propio `AGENTS.md` exige). Cada uno sigue el ciclo completo: `feature-design-doc` → `DD-UC-NNN` + `PR-IMPL-NNN` → ejecución → `dtp-sync`. Este es el primer código real del módulo `notassie`.

**Paso 4 — Cerrar el ciclo de negocio.**
`FSD-UC-004` (exportación SIE, con idempotencia por `rude + periodo_id`) y `FSD-UC-005` (modificación retroactiva con ventana 1–72 h) — ambos dependen de que exista un centralizador `OFICIAL`, producido en el Paso 3.

**Paso 5 — Terminar la configurabilidad genérica.**
`FSD-UC-014`/`015`/`016` (secciones, tipos y cálculo de notas configurable), la UI pendiente de `FSD-UC-018`/`019`/`020`, y activar la validación diferida `E_ASESOR_SIN_CURSO` en `identidad` (mencionada como pendiente desde `DD-UC-010`).

**Paso 6 — Deuda técnica de plataforma antes de `release/1.1.0`.**
Ejecutar `POC-01`/`POC-02` con evidencia real (skill `poc-runner-edusync`, cierra H6), corregir el ruleset de `checkstyle` (cierra H5), y verificar cobertura Jacoco ≥ 80 % en `domain/`+`application/` (`NFR-013`) antes de declarar cerrado el horizonte "Siguiente" del roadmap.

---

## 6. Próxima acción sugerida

Si se quiere avanzar de inmediato, el mayor apalancamiento está en el Paso 1: redactar el ADR de reconciliación (`ADR-0013` o el número que corresponda) antes de abrir ningún `Design Doc` nuevo de `notassie`. Puedo ayudar a redactarlo siguiendo el formato de los ADRs existentes (`docs/adr/0009-*.md` como plantilla de estructura) en cuanto se defina la dirección preferida para el punto 1 (perfil Bolivia como instancia del modelo genérico, o modelo paralelo).
