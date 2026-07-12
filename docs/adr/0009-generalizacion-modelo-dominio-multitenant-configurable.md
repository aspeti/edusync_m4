# Architecture Decision Record (ADR)

## ADR-0009: Generalización del modelo de dominio a plataforma SaaS multi-tenant configurable

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0009` |
| Título | Generalización del modelo de dominio a plataforma SaaS multi-tenant configurable (roles, gestión escolar, periodos y secciones de evaluación configurables) |
| Fecha | 12/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Modelo funcional y de dominio de la capa viva (`docs/product/BRD.md`, `docs/product/PRD.md`, `docs/product/FSD.md`). No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`), que permanece intacto. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |

### 1. Contexto

El baseline de M4 (`docs/baseline/BRD_EduSync_vFinal.md`, `PRD_EduSync_vFinal.md`, `FSD_EduSync_vFinal.md`) especifica EduSync como una plataforma **específica del mercado boliviano**: tenant = colegio individual (sin capa de administración SaaS), tres roles (`DIRECTOR` / `SECRETARÍA` / `DOCENTE`), exactamente 3 periodos trimestrales de apertura secuencial obligatoria, dimensiones pedagógicas fijas (Ser/Saber/Hacer/Decidir/Autoevaluación) con rangos ministeriales, truncado `floor()` como único criterio de redondeo, e identidad de estudiante exclusivamente por código RUDE con exportación obligatoria al Sistema de Información Educativa (SIE) del Ministerio de Educación.

Un nuevo diseño funcional (recibido durante la apertura de `release/3.0.0`) requiere generalizar el producto hacia una plataforma SaaS multi-tenant configurable, agnóstica de país/normativa, con:

- Una capa de plataforma (`SysAdmin`) que administra tenants (Unidades Educativas) y su ciclo de suscripción, ausente en el diseño de M4.
- Roles de tenant renombrados y ampliados: `Admin` (= `Director`), `Secretaria`, `Asesor` (nuevo, tutor/orientador de curso, solo lectura), `Profesor` (= `Docente`).
- Un número **configurable** de periodos de evaluación por Gestión Escolar (no fijo en 3 trimestres).
- Secciones de evaluación **configurables** por Gestión Escolar (nombre, orden, nota máxima, peso porcentual, cantidad máxima de evaluaciones), en lugar de las dimensiones fijas Ser/Saber/Hacer/Decidir/Autoevaluación.
- Tipos de evaluación **configurables** por institución (no codificados de forma fija).
- Módulos estructurales nuevos: Cursos y Paralelos, Materias, Profesores, Estudiantes, Inscripciones (con historial académico), Usuarios y Roles.

Las fuerzas en tensión son: **generalización del producto** (para no atar el modelo de dominio a la normativa boliviana) vs. **preservación de las invariantes regulatorias ya validadas en M4** (RUDE, `floor`, secuencialidad de 3 trimestres, `audit_log` inalterable) vs. **costo de reconciliar dos modelos de datos** (el fijo de M4 y el configurable nuevo) sin invalidar el trabajo ya evaluado del baseline.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener el modelo específico de Bolivia como único modelo de dominio; los nuevos requisitos (SysAdmin, N periodos, N secciones) se implementan como excepciones puntuales sobre el modelo fijo existente | Cero riesgo de romper las invariantes regulatorias ya validadas (RUDE, `floor`, 3 trimestres); continuidad total con el baseline | No resuelve el requisito explícito de "no asumir que siempre existirán tres trimestres" ni de tipos/secciones no codificados de forma fija; cada colegio con estructura distinta requeriría una rama de código | Bajo a corto plazo; alto a mediano plazo (deuda técnica por excepciones) |
| B. Generalizar el modelo de dominio a N periodos / N secciones / tipos configurables y **reemplazar** el modelo fijo boliviano, migrando `floor`/RUDE/SIE a una implementación específica dentro del nuevo modelo genérico en esta misma iteración | Un solo modelo de dominio, sin duplicación | Exige resolver en esta misma iteración cómo `floor()`, los rangos ministeriales fijos y la secuencialidad de 3 trimestres se expresan como parámetros del modelo genérico — trabajo de diseño no solicitado todavía y con alto riesgo de asumir reglas no confirmadas por el negocio | Alto — rediseño simultáneo de reglas regulatorias sensibles sin validación de negocio |
| C. Generalizar el modelo de dominio como una **extensión aditiva**: se introducen las nuevas entidades genéricas (`Tenant` con suscripción, `GestionEscolar`, `PeriodoEvaluacion` (N), `SeccionEvaluacion` (N), `TipoEvaluacion`, `Evaluacion`, `Curso`/`Paralelo`, `Inscripcion`, `Usuario`/`Rol`) **sin tocar** las entidades y reglas específicas de Bolivia (`GestionAcademica`, `ParametroAcademico`, `floor`, RUDE, SIE), dejando explícitamente pendiente de definición la reconciliación entre ambos modelos y la gobernanza (auditoría/inmutabilidad) de los módulos nuevos | Cero riesgo sobre las invariantes regulatorias ya validadas; satisface el requisito de estructura configurable; no obliga a inventar reglas de negocio no confirmadas; trazable como delta explícito en `docs/product/DTP.md` | Conviven temporalmente dos modelos de Gestión Académica/Escolar en la documentación viva hasta que se resuelva la reconciliación (deuda de diseño explícita, no silenciosa) | Medio — requiere una iteración de diseño posterior para la reconciliación, ya identificada y registrada como pendiente |

### 3. Decisión

> **Elegimos la Alternativa C: generalizar el modelo de dominio mediante una extensión aditiva**, incorporando la capa de plataforma SaaS (`SysAdmin`, `Tenant` con ciclo de suscripción) y los módulos configurables (`GestionEscolar`, `PeriodoEvaluacion`, `SeccionEvaluacion`, `TipoEvaluacion`, `Evaluacion`, `Curso`/`Paralelo`, `Materia`, `Profesor`, `Estudiante`, `Inscripcion`, `Usuario`/`Rol`) en `docs/product/BRD.md`, `docs/product/PRD.md` y `docs/product/FSD.md`, **sin modificar ni eliminar** las entidades, reglas de negocio (`BR-001`..`BR-012`) ni reglas (`RB-01`..`RB-11`) específicas del perfil boliviano (SIE/RUDE/`floor`/3 trimestres), que se re-etiquetan como **"Perfil Bolivia SIE"**: una configuración soportada del producto, ya no la única ni el núcleo del dominio.

Quedan **explícitamente pendientes de definición** (fuera de alcance de este ADR, a resolver en una iteración de diseño posterior antes de implementar código sobre estos puntos):

1. La reconciliación entre `GestionAcademica`/`ParametroAcademico` (perfil Bolivia, fijo) y `GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion` (modelo genérico, configurable) — es decir, si el perfil Bolivia termina siendo una instancia parametrizada del modelo genérico o si ambos coexisten como rutas de código separadas.
2. La generalización de la secuencialidad de apertura (`RB-05`) y de la regla de "promedio final solo con todos los periodos cerrados" (`RB-11`) a **N** periodos en lugar de 3 trimestres fijos.
3. El criterio de redondeo/truncado para el cálculo de notas del modelo genérico (si `floor()` se mantiene como default configurable o se abre a otras estrategias).
4. La validación de que los pesos porcentuales de las secciones de evaluación sumen exactamente 100 % (o si se permite otra normalización).
5. La gobernanza (auditoría inalterable, inmutabilidad post-cierre, ventana de corrección retroactiva) para los módulos nuevos: `Tenant`, `GestionEscolar`, `PeriodoEvaluacion`, `SeccionEvaluacion`, `Evaluacion`, `Curso`/`Paralelo`, `Inscripcion`, `Usuario`. Los módulos ya existentes (`Calificacion`, `audit_log`) conservan su gobernanza actual sin cambios.

### 4. Consecuencias

#### 4.1 Positivas

- El producto queda documentado como una plataforma SaaS multi-tenant genérica y configurable, cumpliendo el requisito explícito de no codificar de forma fija ni el número de periodos ni las secciones/tipos de evaluación.
- Ninguna invariante regulatoria ya validada en M4 (`RUDE`, `floor`, secuencialidad de 3 trimestres, `audit_log` inalterable) se pierde, se contradice ni se reescribe silenciosamente: se conserva como "Perfil Bolivia SIE".
- Los 6 ADR de infraestructura previos (`0001`–`0006`) y el ADR de stack (`0008`) permanecen vigentes sin contradicción (ver §8 Referencias, tabla de estado).
- Los puntos de reconciliación pendientes quedan documentados explícitamente (no como divergencia silenciosa), cumpliendo la regla de oro del modelo documental (`plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`).

#### 4.2 Negativas / costos

- Conviven temporalmente dos modelos de "gestión académica/escolar" en `docs/product/FSD.md` (el fijo boliviano y el genérico configurable) hasta que se resuelva la reconciliación del §3 punto 1 — riesgo de confusión para cualquier agente o persona que lea el documento sin notar la nota de convivencia explícita.
- Los módulos nuevos no tienen todavía reglas de gobernanza (auditoría, inmutabilidad) definidas; no deben implementarse en código con garantías de trazabilidad legal hasta que se resuelva el punto 5 del §3.
- El renombrado de roles (`Director` → `Admin`, `Docente` → `Profesor`) se aplica en las tablas estructurales nuevas y en la nomenclatura vigente hacia adelante, pero los casos de uso, contratos de prompt y escenarios Gherkin ya redactados para el perfil Bolivia (creados en M4/apertura de `release/3.0.0`) conservan la nomenclatura `DIRECTOR`/`DOCENTE` por fidelidad a su trazabilidad original; ambos términos son equivalentes desde este ADR en adelante (ver nota de nomenclatura en `docs/product/BRD.md`, `PRD.md`, `FSD.md`).

#### 4.3 Neutras / observables

- No se crea ningún ADR que "supersede" o invalide a `0001`–`0006`/`0008`; todos permanecen con estado `Aceptada` sin cambios en su contenido.
- El directorio `src/` sigue vacío (greenfield): este ADR es puramente de modelo funcional/de dominio documental, sin impacto en código todavía.

### 5. Impacto en el sistema

- **Documentación**: `docs/product/BRD.md` (nuevos BR-013+, persona `SysAdmin`, alcance ampliado), `docs/product/PRD.md` (nuevas épicas/historias/requisitos por módulo), `docs/product/FSD.md` (nuevos actores, `FSD-UC-011`+, entidades nuevas en el modelo ER, notas de "pendiente de definición"). `docs/product/DTP.md` §A.1/§A.2 registra este ADR como delta.
- **Código**: sin impacto directo (`src/` vacío). Cuando se inicie la implementación, los módulos nuevos deberán respetar `ADR-0001` (RLS por `tenant_id`) desde su primera migración Flyway.
- **Seguridad**: el nuevo rol `SysAdmin` opera a nivel plataforma (fuera del `tenant_id` de un colegio específico); su modelo de aislamiento y auditoría queda pendiente de diseño (ver §3 punto 5).
- **Equipo**: cualquier Design Doc (`DD-UC-NNN`) que aborde un `FSD-UC-011`+ debe citar explícitamente este ADR y, si toca alguno de los 5 puntos pendientes del §3, debe primero proponer un ADR de seguimiento antes de codificar.
- **Costo**: sin impacto en infraestructura (`ADR-0006` no cambia).

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si al diseñar el primer `DD-UC-NNN` de un módulo genérico se concluye que la convivencia de dos modelos de "gestión académica" (Bolivia vs. genérico) es inviable de mantener sin refactor inmediato, o si el negocio decide que el perfil Bolivia SIE deja de ser prioritario y debe eliminarse en lugar de coexistir.
- **Costo estimado de revertir**: bajo — no hay código productivo (`src/` vacío); revertir implica solo ajustar la documentación viva (`docs/product/`) sin afectar al baseline (`docs/baseline/`, intacto por diseño).
- **Plan B**: si la reconciliación (§3 punto 1) resulta impráctica, migrar el perfil Bolivia SIE a una instancia parametrizada del modelo genérico (Alternativa B original) mediante un ADR de seguimiento, sin reabrir este ADR-0009.

### 7. Validación

- **Revisión de consistencia documental**: `docs/product/BRD.md`, `PRD.md` y `FSD.md` deben citar este ADR en su changelog y mantener la nota de nomenclatura de roles sincronizada entre los tres documentos.
- **Checklist de no divergencia**: ningún BR/RB/NFR del perfil Bolivia SIE (`BR-001`..`BR-012`, `RB-01`..`RB-11`) queda editado ni eliminado; verificable por diff contra la versión previa a este ADR.
- **Verificación de los 5 pendientes**: cada uno de los puntos del §3 debe tener, antes de su implementación en código, un Design Doc o ADR de seguimiento que lo resuelva explícitamente (no se permite implementación directa sobre un punto marcado "pendiente de definición").

### 8. Referencias

- Estado de los ADR previos tras este pivote (ninguno se supersede; todos permanecen `Aceptada` sin cambios):

| ADR | Decisión | Estado tras ADR-0009 |
|-----|----------|-----------------------|
| `ADR-0001` | Multitenancy vía RLS PostgreSQL (`tenant_id`) | Vigente sin cambios — la nueva entidad `Tenant` (con suscripción) se añade como capa de plataforma sobre el mismo mecanismo de aislamiento, no lo reemplaza |
| `ADR-0002` | Parametrización de reglas normativas (`parametro_academico`, alcance `tenant + periodo`) | Vigente sin cambios — el mecanismo (tabla paramétrica) es compatible en espíritu con `SeccionEvaluacion`/`PeriodoEvaluacion` configurables; la reconciliación formal queda pendiente (§3 punto 1) |
| `ADR-0003` | `audit_log` inalterable + append-only | Vigente sin cambios — aplica igual a los módulos ya existentes (`Calificacion`); no se exige todavía a los módulos nuevos (§3 punto 5) |
| `ADR-0004` | Consolidación asíncrona vía Spring Events + `floor()` | Vigente sin cambios — decisión de arquitectura (asincronía) no depende del modelo de dominio; las referencias a `floor()`/3 trimestres son del Perfil Bolivia SIE, que sigue vigente como perfil |
| `ADR-0005` | Resiliencia SIE con Resilience4j + idempotencia RUDE | Vigente sin cambios — SIE pasa a ser opcional por tenant (Perfil Bolivia SIE), no eliminado |
| `ADR-0006` | Cloud provider AWS ECS Fargate | Vigente sin cambios — no depende del modelo funcional |
| `ADR-0008` | Stack Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS | Vigente sin cambios — no relacionado con el modelo de dominio |

- `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md` (regla de oro de cero divergencia silenciosa).
- `docs/product/DTP.md` §A.2 (delta registrado a partir de este ADR).
- `docs/product/BRD.md`, `PRD.md`, `FSD.md` (documentos actualizados a partir de esta decisión).
- `docs/baseline/BRD_EduSync_vFinal.md`, `PRD_EduSync_vFinal.md`, `FSD_EduSync_vFinal.md` (baseline de M4, no modificado).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 12/07/2026 | Rodrigo Aspeti | ADR formal creado a partir del nuevo diseño funcional recibido para `release/3.0.0`; generaliza el modelo de dominio a plataforma SaaS multi-tenant configurable como extensión aditiva sobre el Perfil Bolivia SIE (sin reemplazarlo); documenta 5 puntos pendientes de definición; estado Aceptada |
