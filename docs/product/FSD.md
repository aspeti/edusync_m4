> COPIA VIVA - release/3.0.0 (capa de implementacion, FSD vivo en modo LFSD ⚡)
>
> Este archivo nace como copia editable de docs/baseline/FSD_EduSync_vFinal.md (congelado en release/2.0.0, tag de M4) para evolucionar junto al codigo desde release/3.0.0 en adelante, en modo LFSD ⚡ (mantenimiento sprint-a-sprint), siguiendo el modelo documental de implementacion (plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md).
>
> **Este archivo SI se edita** a medida que la implementacion lo requiera. El registro historico inmutable de M4 vive en docs/baseline/FSD_EduSync_vFinal.md (tag release/2.0.0) y NUNCA se modifica; cada cambio de criterio/flujo se traza a un FSD-UC, opcionalmente a un `DD-UC-NNN` en docs/design/, y se registra en docs/product/DTP.md §A.2/§A.3.
>
> | Campo | Valor |
> |-------|-------|
> | Fuente historica (congelada) | docs/baseline/FSD_EduSync_vFinal.md |
> | Version de partida | v1.0 |
> | Fecha de apertura de capa viva | 28/05/2026 |
> | Release vivo | release/3.0.0 |
> | Documento rector | plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md |
> | Agente | docs-agent |

---
# Functional Specification Document (FSD) — EduSync

---

## 0. Metadatos ⚡🔧

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync |
| **Grupo** | G-EduSync |
| **Versión del documento** | v2.8 |
| **Fecha** | 21/08/2026 |
| **Autores** | Rodrigo Aspeti — Dev Lead / PM |
| **Revisores** | Docente + 1 grupo par |
| **Estado** | En revisión |
| **Modo elegido** | **FSD Clásico 🔧** |
| **Trazabilidad a PRD** | `docs/product/PRD.md` (v2.2) |
| **Insumos M2 (UI/UX)** | Sistema Atomic Design · Design Tokens · Semáforos visuales de reprobación · WCAG 2.2 AA |
| **Fase Spec Kit cubierta** | Specify ✅ / Plan ✅ / Tasks ✅ / Implement ⬜ |
| **Prompts utilizados** | `PR-ARCH-001`, `PR-BRD-002`, `PR-DIAG-001`, `PR-DIAG-002` (ver `docs/PROMPT_MAPPING.md`) |

### 0.1 Nomenclatura de roles (vigente desde v2.0 — `ADR-0009`)

> Ver detalle completo en `docs/product/BRD.md` §0.1. Resumen: **SysAdmin** (plataforma, nuevo) · **Admin** = `DIRECTOR` · **Secretaria** = `SECRETARÍA` · **Asesor** (tenant, nuevo, solo lectura de su curso/paralelo) · **Profesor** = `DOCENTE`. Las secciones §1–§3 (tabla de actores original), §4.1–§4.5 (`FSD-UC-001`..`FSD-UC-009`), §5 (`BR-001`..`BR-012`), §6.2 (diccionario de datos con enum `DIRECTOR/SECRETARIA/DOCENTE`) y §7 (prompt-contratos) fueron redactadas antes de `ADR-0009` y usan la nomenclatura y el modelo histórico del **Perfil Bolivia SIE**; ambos pares de nombres de rol son equivalentes 1:1 desde esta versión y no representan roles distintos. Las secciones nuevas (§3.1, §4.6+, §5.1, §6.3) usan la nomenclatura y las entidades genéricas vigentes.

---

## 1. Resumen ejecutivo ⚡🔧

EduSync es una plataforma SaaS B2B multitenant de gestión académica construida sobre Java 25 (LTS), Spring Boot 4.1.0 (Spring Framework 7.0.8), PostgreSQL 15 y Angular 21 (LTS), desplegada en AWS (stack vivo vigente desde `release/3.0.0`, `ADR-0008`; el baseline congelado de M4 documentó Java 21 / Spring Boot 3.3 / Angular 17, ver `docs/baseline/DTI.md` §4). Su misión técnica es eliminar la "triple digitación manual" que obliga al personal de colegios bolivianos a trabajar de madrugada para cumplir con los plazos del Sistema de Información Educativa (SIE) del Ministerio de Educación.

El sistema descentraliza el registro de calificaciones por rol (RBAC estricto): cada Docente ingresa notas únicamente en sus materias asignadas, con validación paramétrica en tiempo real. Un motor de consolidación algorítmico calcula promedios trimestrales aplicando el criterio `floor` como única regla de truncado, garantizando consistencia con la escala del SIE. La Secretaría exporta masivamente al SIE con un clic, con resiliencia ante fallos parciales mediante reintentos idempotentes por `rude + periodo_id`. El Director administra la gestión académica anual, define parámetros configurables por periodo y autoriza correcciones retroactivas con ventanas temporales de 1–72 horas.

Todo el ciclo queda sellado en un `audit_log` append-only inalterable, con aislamiento multitenant mediante Row-Level Security en PostgreSQL. El diseño arquitectónico sigue los principios de arquitectura hexagonal con separación Domain / Application / Infrastructure, garantizando que ningún cálculo de promedios ni conversión de escala SIE ocurra fuera del motor de dominio.

> **Generalización desde v2.0 (`ADR-0009`):** lo anterior describe el **Perfil Bolivia SIE** (§4.1–§4.5, `FSD-UC-001`..`FSD-UC-009`), vigente sin cambios. Desde esta versión, el dominio de EduSync se extiende con un modelo genérico configurable: un `SysAdmin` de plataforma administra `Tenant`s con ciclo de suscripción propio; cada tenant define su `GestionEscolar` con un número **N** de `PeriodoEvaluacion`, sus propias `SeccionEvaluacion` (peso y nota máxima configurables) y su catálogo de `TipoEvaluacion` — sin asumir 3 trimestres ni dimensiones fijas. Ver los nuevos casos de uso `FSD-UC-011`..`FSD-UC-021` (§4.6) y el modelo de entidades ampliado (§6.3). La reconciliación entre este modelo genérico y el modelo fijo boliviano (`GestionAcademica`/`ParametroAcademico`) queda **pendiente de definición** (`ADR-0009` §3).

---

## 2. Alcance ⚡🔧

### 2.1 Dentro del alcance

- Autenticación JWT con RBAC (DIRECTOR / SECRETARÍA / DOCENTE) y aislamiento multitenant por `tenant_id`.
- Administración de gestión académica anual: creación, calendario de 3 trimestres, parámetros, asignación docente-materia, apertura/cierre secuencial.
- Registro de calificaciones por dimensión con validación paramétrica en tiempo real (tipo REGULAR / AYUDA).
- Cierre atómico de materia con verificación de completitud al 100 %.
- Motor de consolidación algorítmico: vista provisional continua (`PROVISIONAL`) y centralizador oficial (`CERRADO`) con criterio `floor`.
- Exportación masiva al SIE por RUDE con idempotencia y reintentos asíncronos ante fallos.
- Flujo de modificaciones retroactivas: solicitud, autorización jerárquica, ventana temporal, revocación automática, modelo append-only.
- Gestión de nóminas estudiantiles con identidad exclusiva por RUDE.
- Generación de boletines PDF desde centralizador cerrado.
- Control de asistencia por materia.
- Dashboard de indicadores institucionales con separación trimestral / anual.
- Log de auditoría inalterable para toda operación de escritura.

### 2.2 Fuera del alcance (explícito)

- Módulo de comunicación con padres (previsto en v1.2).
- Gestión de finanzas / cobro de pensiones (v2.0).
- Módulo de matrícula digital — el RUDE llega preregistrado al sistema.
- Aplicación móvil nativa (iOS/Android) — v2.0; el v1.0 es web responsiva.
- Integración con sistemas de nómina de personal docente.
- Módulo de benchmark anónimo entre colegios (v1.2).

### 2.3 Supuestos y dependencias

**Supuestos técnicos:**
- Cada estudiante posee un código RUDE único y válido asignado por el Ministerio antes del onboarding.
- El servidor SIE acepta payloads en el formato ministerial vigente (HTTP/REST); el formato exacto es parametrizable en BD sin redespliegue.
- Los colegios tienen conectividad a internet suficiente para operar la interfaz web (no se requiere modo offline).
- PostgreSQL 15 soporta RLS con `tenant_id` sin degradación de rendimiento a escala de colegios medianos (< 1.000 estudiantes, < 50 materias).

**Dependencias externas:**
- SIE (Ministerio de Educación Bolivia): endpoint HTTP de exportación de calificaciones. Sin garantía de idempotencia; alta tasa de fallos en horario pico.
- AWS (RDS PostgreSQL, EC2, SQS en v1.1): infraestructura de hosting y mensajería.
- Motor de PDF (Apache PDFBox): generación de boletines con plantilla ministerial.
- Proveedor de notificaciones in-app: alertas de vencimiento de ventana y cambios de estado.

### 2.4 Plan técnico 🔧

| Bloque | Contenido |
|--------|-----------|
| **Stack tecnológico** | Java 25 (LTS) · Spring Boot 4.1.0 (Spring Framework 7.0.8) · Spring Security 7 (JWT + RBAC) · Spring Data JPA · PostgreSQL 15 (RLS) · Angular 21 (LTS) · AWS (RDS db.t3.medium, EC2 t3.small / ECS Fargate) · Apache PDFBox — stack vivo vigente desde `release/3.0.0` (`ADR-0008`) |
| **Arquitectura prevista** | Hexagonal (Ports & Adapters) con separación en capas: `domain/` (entidades, servicios de dominio, reglas) · `application/` (casos de uso, ports) · `infrastructure/` (JPA adapters, REST controllers, SIE client) · `frontend/` (Angular SPA con Design System Atomic Design) |
| **Project structure** | `backend/src/main/java/bo/edusync/{domain,application,infrastructure}/` · `frontend/src/app/{modules,shared,core}/` · `docs/fsd/`, `docs/adr/`, `docs/diagramas/` · `infra/` (Docker Compose, Flyway migrations) |
| **Decisiones técnicas anticipadas** | DA-01: RLS PostgreSQL con `tenant_id` · DA-02: parámetros académicos en BD sin redespliegue · DA-03: `audit_log` append-only con Hibernate Envers en entidades críticas · DA-04: consolidación asíncrona con Spring Events (migrable a SQS) · DA-05: idempotencia SIE por `rude+periodo_id` |
| **Restricciones técnicas** | Stack Java/Spring obligatorio (sin alternativas). El `audit_log` es inalterable: sin UPDATE ni DELETE. El cálculo de `floor` y la conversión SIE solo ocurren en la capa de dominio. |

### 2.5 Descomposición en Tasks (Spec Kit) ⚡🔧

| Task ID | Descripción | Caso de uso (FSD-UC) | Dependencias | Prompt asociado | Estado |
|---------|-------------|----------------------|--------------|-----------------|--------|
| T-001 | Setup multitenant: configurar RLS en PostgreSQL + inyección de `tenant_id` en SecurityContext de Spring | FSD-UC-001 | — | PR-ARCH-001 | pendiente |
| T-002 | Módulo de Autenticación: JWT issue/validate + RBAC con Spring Security | FSD-UC-001 | T-001 | PR-UC-001 | pendiente |
| T-003 | CRUD Gestión Académica + 3 periodos + validación de apertura secuencial | FSD-UC-009 | T-002 | PR-UC-009 | pendiente |
| T-004 | Motor de parámetros: tabla `parametro_academico` + API de configuración por periodo | FSD-UC-009 | T-003 | PR-UC-009 | pendiente |
| T-005 | Asignación docente-materia + verificación de cobertura antes de apertura | FSD-UC-009 | T-003 | PR-UC-009 | pendiente |
| T-006 | Endpoint POST /calificaciones con validación paramétrica en tiempo real | FSD-UC-001 | T-004, T-005 | PR-UC-001 | pendiente |
| T-007 | Motor de consolidación: PROMEDIO_SIMPLE + floor + estado PROVISIONAL/OFICIAL | FSD-UC-003 | T-006 | PR-UC-003 | pendiente |
| T-008 | Cierre atómico de materia: verificación completitud + transición SOLO_LECTURA | FSD-UC-002 | T-006, T-007 | PR-UC-002 | pendiente |
| T-009 | Exportación masiva SIE: payload RUDE + idempotencia + reintentos asíncronos | FSD-UC-004 | T-007, T-008 | PR-UC-004 | pendiente |
| T-010 | Flujo de modificaciones retroactivas: solicitud + autorización + ventana temporal + revocación automática | FSD-UC-005 | T-006, T-008 | PR-UC-005 | pendiente |
| T-011 | Gestión de nóminas estudiantiles: alta/baja/transferencia por RUDE | FSD-UC-006 | T-002 | PR-UC-001 | pendiente |
| T-012 | Generación de boletines PDF con plantilla ministerial parametrizable | FSD-UC-007 | T-007 | PR-ADR-001 | pendiente |
| T-013 | Dashboard Director: indicadores trimestrales vs. anuales con separación estricta | FSD-UC-010 | T-007 | PR-INF-001 | pendiente |
| T-014 | `audit_log` append-only: AOP interceptor + Hibernate Envers en entidades críticas | Todos los FSD-UC | T-001 | PR-AUD-001 | pendiente |

---

## 3. Actores y roles del sistema ⚡🔧

> Tabla original del Perfil Bolivia SIE (nomenclatura histórica `DIRECTOR`/`DOCENTE`, equivalente desde `ADR-0009` a `ADMIN`/`PROFESOR` — ver §0.1).

| Actor | Tipo | Responsabilidad principal | Permisos clave |
|-------|------|---------------------------|----------------|
| **DIRECTOR** (= `ADMIN`, §0.1) | Humano | Administrar la gestión académica anual: crear la gestión, definir parámetros, asignar docentes, abrir/cerrar periodos secuencialmente, autorizar correcciones retroactivas, visualizar indicadores institucionales | Escritura en gestión académica, periodos, parámetros, asignaciones, autorizaciones; lectura de todos los datos del tenant |
| **SECRETARÍA** (= `SECRETARIA`, §0.1) | Humano | Gestionar nóminas estudiantiles, monitorizar avance de carga docente, exportar al SIE, generar boletines PDF, administrar altas/bajas/transferencias | Escritura en nóminas, exportación SIE, boletines; lectura de centralizadores y asistencia; sin acceso a parámetros ni periodos |
| **DOCENTE** (= `PROFESOR`, §0.1) | Humano | Registrar calificaciones y asistencia en sus materias asignadas, cerrar materia, solicitar correcciones retroactivas | Escritura restringida a sus materias+dimensiones; lectura de su materia y nómina en solo lectura; sin acceso a otras materias ni datos del Director |
| **Motor de Consolidación** | Sistema (Spring Event) | Calcular promedios trimestrales y anuales con criterio `floor` al dispararse el cierre de la última materia de un curso | Lectura de calificaciones cerradas; escritura en tabla `centralizador` (solo el motor) |
| **Motor de Exportación SIE** | Sistema (proceso asíncrono) | Construir el payload SIE por RUDE y gestionar reintentos idempotentes ante fallos del servidor ministerial | Lectura de centralizador CERRADO; escritura en tabla `exportacion_registro`; invocación HTTP al SIE |
| **Scheduler de Ventanas** | Sistema (Spring Scheduler) | Revocar automáticamente las ventanas de corrección retroactiva expiradas y enviar alertas a 30 min del vencimiento | Escritura en `autorizacion_correccion` (estado); envío de notificaciones in-app |
| **compliance-agent** | Agente IA | Validar que ningún output de dev-agent viole las invariantes regulatorias del SIE (RUDE, floor, rangos) antes del merge | Solo lectura de artefactos del repositorio + ejecución de golden tests |

### 3.1 Actores del modelo generalizado *(nuevos desde v2.0 — `ADR-0009`)*

| Actor | Tipo | Nivel | Responsabilidad principal | Permisos clave |
|-------|------|-------|---------------------------|----------------|
| **SYSADMIN** | Humano | Plataforma (SaaS) | Registrar Unidades Educativas (`Tenant`), gestionar su suscripción y estado, administrar usuarios `ADMIN` de cada tenant | Escritura en `Tenant` y su ciclo de suscripción; escritura en usuarios `ADMIN`; **sin** acceso a datos académicos de ningún tenant. `tenant_id` nulo de forma **permanente**; no se combina con ningún rol de tenant en el mismo usuario (`ADR-0010`, ver §6.3.2) |
| **ADMIN** | Humano | Tenant | Equivalente vigente de `DIRECTOR` (§0.1); además administra `GestionEscolar`, `PeriodoEvaluacion`, `SeccionEvaluacion`, `Curso`/`Paralelo`, `Materia`, `Profesor`, `Usuario` de su tenant | Escritura en todos los módulos configurables de su tenant; lectura de todos los datos de su tenant |
| **SECRETARIA** | Humano | Tenant | Equivalente vigente de `SECRETARÍA` (§0.1); además administra `Estudiante` e `Inscripcion` | Escritura en `Estudiante`, `Inscripcion`; lectura de indicadores de su tenant |
| **ASESOR** | Humano | Tenant | Tutor/orientador de un `Curso`/`Paralelo` asignado; sin equivalente en el Perfil Bolivia SIE | Solo lectura del avance académico de su `Curso`/`Paralelo` asignado; **sin** escritura sobre calificaciones ni nóminas |
| **PROFESOR** | Humano | Tenant | Equivalente vigente de `DOCENTE` (§0.1); además registra `Evaluacion` dentro de las `SeccionEvaluacion` de sus materias asignadas | Escritura restringida a sus materias/secciones/evaluaciones asignadas |

---

## 4. Casos de uso funcionales ⚡🔧

---

### 4.1 FSD-UC-001 — Registro descentralizado de calificaciones por dimensión

- **Trazabilidad:** `PRD-REQ-006`, `PRD-REQ-007`, `PRD-US-007`, `PRD-US-008`
- **Actor principal:** Docente
- **Precondiciones:**
  1. El Docente tiene sesión JWT activa con rol DOCENTE y `tenant_id` del colegio.
  2. El periodo está en estado `ABIERTO`.
  3. El Docente está asignado a la materia seleccionada (tabla `asignacion_docente`).
  4. Los parámetros académicos del periodo están configurados (dimensiones, pesos, reglas).
- **Disparador:** El Docente selecciona un estudiante y una dimensión y envía `POST /api/v1/calificaciones`.
- **Flujo principal:**
  1. El sistema valida el JWT y extrae `{tenant_id, user_id, rol}` del SecurityContext.
  2. El sistema verifica que la materia pertenezca al tenant y esté asignada al docente (RBAC).
  3. El sistema verifica que el periodo esté en estado `ABIERTO`.
  4. El sistema recupera el parámetro activo de la dimensión solicitada (`peso_max`, `rango_min`, `rango_max`).
  5. El sistema valida que `valor` ∈ [`rango_min`, `rango_max`].
  6. El sistema persiste el registro `Calificacion` con campos: `{docente_id, materia_id, rude, dimension, indice_evaluacion, tipo (REGULAR|AYUDA), valor, periodo_id, timestamp_utc}`.
  7. El sistema genera entrada en `audit_log`: `{actor: docente_id, accion: CALIFICACION_REGISTRADA, entidad: calificacion.id, valor_anterior: null, valor_nuevo: valor}`.
  8. El motor de consolidación recalcula el promedio provisional del estudiante en esa materia.
  9. El sistema retorna retroalimentación visual: promedio provisional actualizado marcado `PROVISIONAL`.
- **Flujos alternativos / excepciones:**
  - **A1 — Valor fuera de rango:** Si `valor` < `rango_min` o `valor` > `rango_max` → HTTP 422 con `{error: "E_RANGO_INVALIDO", dimension: ..., rango_permitido: [min, max]}`. No se persiste nada.
  - **A2 — Periodo CERRADO o SOLO_LECTURA:** HTTP 409 con `{error: "E_PERIODO_NO_MODIFICABLE"}`.
  - **A3 — RBAC: materia no asignada al docente:** HTTP 403 con `{error: "E_RBAC_VIOLATION"}`. Entrada en `audit_log` tipo `RBAC_VIOLATION`.
  - **A4 — RUDE inválido o nulo:** HTTP 400 con `{error: "E_RUDE_INVALIDO"}`.
  - **A5 — Nota AYUDA sin nota REGULAR previa:** HTTP 422 con `{error: "E_REGULAR_REQUERIDA"}` si la regla del periodo exige nota regular como prerequisito.
- **Postcondiciones:**
  1. Registro `Calificacion` persistido en la base de datos.
  2. Entrada en `audit_log` creada.
  3. Vista provisional del centralizador actualizada.
- **Reglas de negocio aplicables:** BR-001 (RBAC), BR-002 (rangos paramétricos), BR-008 (`floor`), RB-01 (RUDE), RB-02 (límites dimensionales).
- **Datos de entrada:**
  ```json
  {
    "materiaId": "uuid",
    "periodoId": "uuid",
    "rude": "string(10–20)",
    "dimension": "SER|SABER|HACER|DECIDIR|AUTOEVALUACION",
    "indiceEvaluacion": "integer >= 1",
    "tipo": "REGULAR|AYUDA",
    "valor": "decimal(5,2)"
  }
  ```
- **Datos de salida:**
  ```json
  {
    "calificacionId": "uuid",
    "promedioProvisional": { "valor": 67, "estado": "PROVISIONAL" },
    "timestamp": "ISO-8601"
  }
  ```
- **Criterios de aceptación:**

```gherkin
Escenario: Registro válido de nota en dimensión Saber
  Dado un Docente autenticado con materia "Matemáticas 2A" asignada
    Y periodo Trimestre 1 en estado ABIERTO
    Y dimensión Saber configurada con rango [0, 45]
  Cuando envía POST /api/v1/calificaciones con {rude:"1234567", dimension:"SABER", valor:38, tipo:"REGULAR"}
  Entonces el sistema responde HTTP 201
    Y persiste el registro con timestamp UTC
    Y crea entrada en audit_log
    Y retorna promedio provisional actualizado con etiqueta PROVISIONAL

Escenario: Rechazo por valor fuera de rango
  Dado dimensión Ser configurada con rango [0, 5]
  Cuando el Docente envía valor = 7
  Entonces el sistema responde HTTP 422 con error E_RANGO_INVALIDO
    Y no persiste ningún registro
```

---

### 4.2 FSD-UC-003 — Consolidación algorítmica de centralizadores

- **Trazabilidad:** `PRD-REQ-010`, `PRD-REQ-011`, `PRD-US-011`
- **Actor principal:** Sistema (Motor de Consolidación, disparado por evento de dominio)
- **Precondiciones:**
  1. Al menos una materia del curso tiene estado `CERRADO`.
  2. Los parámetros académicos del periodo están configurados (regla de combinación, criterio de truncado).
- **Disparador:** Evento de dominio `MateriaCerradaEvent` publicado por FSD-UC-002 (cierre de materia). En modo previsional, también disparado por cada `CalificacionRegistradaEvent`.
- **Flujo principal (Vista Provisional):**
  1. El motor recibe el evento con `{materia_id, periodo_id, curso_id}`.
  2. El motor recupera todas las calificaciones registradas para el curso en el periodo.
  3. Por cada estudiante y dimensión, aplica la regla de combinación del periodo (`PROMEDIO_SIMPLE`, `SUMA`, o `MEJOR_N`).
  4. Aplica el criterio de truncado `floor(resultado)` al puntaje de cada dimensión.
  5. Suma los puntajes de todas las dimensiones activas para obtener el puntaje total del estudiante.
  6. Escribe el resultado en `centralizador` con estado `PROVISIONAL`.
  7. El resultado se marca visualmente en la UI como `PROVISIONAL — en curso`.
- **Flujo principal (Centralizador Oficial):**
  1. El sistema verifica que el 100 % de las materias del curso en el periodo están en estado `CERRADO`.
  2. Si es así, el motor ejecuta el mismo cálculo sobre las calificaciones cerradas e inmutables.
  3. El centralizador se escribe en `centralizador` con estado `OFICIAL` e inmutable.
  4. Se habilita la generación de boletines para ese curso (FSD-UC-007).
  5. Si el Trimestre 3 también se cierra: el motor calcula el promedio anual = `floor((T1 + T2 + T3) / 3)`.
- **Flujos alternativos / excepciones:**
  - **A1 — Trimestre parcialmente cerrado:** El centralizador permanece en `PROVISIONAL`. La columna de promedio anual muestra `EN CURSO — promedio anual no disponible`.
  - **A2 — Error en el cálculo (división por cero):** El motor registra el error en `audit_log` tipo `CONSOLIDACION_ERROR` y deja el centralizador en estado `ERROR`.
- **Postcondiciones:**
  1. Tabla `centralizador` actualizada con estado `PROVISIONAL` u `OFICIAL`.
  2. Si oficial: boletines habilitados; indicadores del Director actualizados.
- **Reglas de negocio aplicables:** BR-003 (floor), RB-08 (criterio único de truncado), RB-11 (indicadores anuales con 3 trimestres cerrados).
- **Datos de entrada (evento):**
  ```json
  {
    "tipo": "MATERIA_CERRADA",
    "materiaId": "uuid",
    "cursoId": "uuid",
    "periodoId": "uuid",
    "tenantId": "uuid"
  }
  ```
- **Datos de salida:**
  ```json
  {
    "centralizadorId": "uuid",
    "cursoId": "uuid",
    "periodoId": "uuid",
    "estado": "PROVISIONAL|OFICIAL",
    "promedios": [
      { "rude": "1234567", "puntaje": 78, "aprobado": true }
    ],
    "promedioAnual": null
  }
  ```
- **Criterios de aceptación:**

```gherkin
Escenario: Truncado floor correcto
  Dado que un estudiante tiene en Saber evaluación 1 = 40, evaluación 2 = 29, evaluación 3 = 31
    Y la regla de combinación es PROMEDIO_SIMPLE
    Y el peso máximo de Saber es 45
  Cuando el motor consolida
  Entonces promedio_bruto = (40 + 29 + 31) / 3 = 33.333...
    Y el motor aplica floor(33.333) = 33
    Y el puntaje de dimensión Saber = floor(33 * 45 / 100) = floor(14.85) = 14
    Y el resultado almacenado es 14, no 15

Escenario: Promedio anual bloqueado con solo 2 trimestres cerrados
  Dado Trimestre 1 en estado CERRADO y Trimestre 2 en estado CERRADO
    Y Trimestre 3 en estado ABIERTO
  Cuando se consulta el centralizador anual
  Entonces el campo promedioAnual = null
    Y se muestra etiqueta "EN CURSO — promedio anual no disponible"
```

---

### 4.3 FSD-UC-004 — Exportación masiva al SIE por RUDE

- **Trazabilidad:** `PRD-REQ-012`, `PRD-REQ-013`, `PRD-US-012`, `PRD-US-013`
- **Actor principal:** Secretaría
- **Precondiciones:**
  1. Todos los centralizadores del periodo están en estado `OFICIAL` (100 % materias cerradas).
  2. Existe una configuración del formato SIE vigente en la tabla `parametro_sie`.
- **Disparador:** La Secretaría invoca `POST /api/v1/exportaciones/sie` con `{periodoId}`.
- **Flujo principal:**
  1. El sistema valida que todas las materias del periodo y todos los cursos tienen centralizador `OFICIAL`. Si no: HTTP 409 `E_MATERIAS_INCOMPLETAS` con lista de pendientes.
  2. El sistema crea un registro `ExportacionSIE` con estado `EN_PROGRESO` y `exportacion_id`.
  3. Por cada estudiante del periodo (identificado por RUDE):
     a. El sistema aplica el filtro pre-exportación: descarta filas con RUDE nulo/inválido (marca `EXCLUIDO_SIN_RUDE`) y notas nulas en dimensiones requeridas (marca `EXCLUIDO_NOTA_INCOMPLETA`).
     b. Los estudiantes válidos se insertan en `exportacion_registro` con estado `PENDIENTE`.
  4. El motor de exportación procesa los registros `PENDIENTE` de forma asíncrona:
     a. Construye el payload en el formato SIE vigente para cada estudiante.
     b. Invoca el endpoint SIE con el payload del estudiante.
     c. Si respuesta 200: actualiza `exportacion_registro.estado = ENVIADO`.
     d. Si respuesta != 200: actualiza `exportacion_registro.estado = FALLIDO` y registra el error.
  5. Al finalizar todos los registros, el sistema actualiza `ExportacionSIE.estado = COMPLETA` o `PARCIAL` (si hay FALLIDOs).
  6. Genera entrada en `audit_log`: `{actor: secretaria_id, accion: EXPORTACION_SIE, periodo: periodoId, result: COMPLETA|PARCIAL}`.
  7. El sistema retorna el reporte: `{enviados: N, fallidos: M, excluidos: K}`.
- **Flujos alternativos / excepciones:**
  - **A1 — Fallo parcial del SIE:** Los registros en estado `FALLIDO` son reintentados automáticamente cada 5 minutos por el Scheduler. La clave de idempotencia es `rude + periodo_id`: un registro con ese par en estado `ENVIADO` nunca se reenvía.
  - **A2 — Timeout total del SIE:** Si el SIE no responde en 30 s, el registro pasa a `FALLIDO` con `error: "TIMEOUT"`. Se programa reintento.
  - **A3 — RUDE inválido detectado en el filtro:** El registro se marca `EXCLUIDO_SIN_RUDE` y aparece en el reporte de resultado. No genera error HTTP; la exportación continúa.
- **Postcondiciones:**
  1. `ExportacionSIE` y cada `ExportacionRegistro` en estado final (`ENVIADO` / `FALLIDO` / `EXCLUIDO_*`).
  2. Entrada en `audit_log` con resumen de la operación.
  3. Reporte de resultado disponible para descarga en PDF.
- **Reglas de negocio aplicables:** RB-01 (RUDE como clave), BR-004 (exportación por RUDE), DA-05 (idempotencia).
- **Datos de entrada:**
  ```json
  { "periodoId": "uuid", "tenantId": "uuid" }
  ```
- **Datos de salida:**
  ```json
  {
    "exportacionId": "uuid",
    "estado": "COMPLETA|PARCIAL|EN_PROGRESO",
    "enviados": 78,
    "fallidos": 2,
    "excluidosSinRude": 0,
    "excluidosNotaIncompleta": 1,
    "detalleUrl": "/api/v1/exportaciones/{exportacionId}/reporte"
  }
  ```
- **Criterios de aceptación:**

```gherkin
Escenario: Reanudación sin duplicados tras fallo parcial
  Dado que la exportación envió 46 de 80 registros exitosamente
    Y el SIE responde HTTP 503 para el registro 47
  Cuando el Scheduler reintenta los registros FALLIDOS
  Entonces el sistema envía solo registros en estado FALLIDO o PENDIENTE
    Y no reenvía los 46 registros en estado ENVIADO
    Y la clave de idempotencia rude+periodo_id garantiza unicidad en el SIE

Escenario: Bloqueo con materias abiertas
  Dado que la materia "Historia 3A" está en estado ABIERTO
  Cuando la Secretaría intenta iniciar la exportación
  Entonces el sistema responde HTTP 409 con E_MATERIAS_INCOMPLETAS
    Y lista las materias pendientes de cierre
```

---

### 4.4 FSD-UC-005 — Autorización jerárquica de modificación retroactiva

- **Trazabilidad:** `PRD-REQ-014`, `PRD-REQ-015`, `PRD-REQ-017`, `PRD-US-014`, `PRD-US-015`, `PRD-US-017`
- **Actor principal:** Docente (solicitante) + Director (autorizador)
- **Precondiciones:**
  1. La materia está en estado `SOLO_LECTURA` (ya cerrada).
  2. El Docente está autenticado y asignado a la materia.
  3. El Director está autenticado con rol DIRECTOR del mismo tenant.
- **Disparador:** El Docente invoca `POST /api/v1/solicitudes-correccion`.
- **Flujo principal:**
  1. El Docente envía `{materiaId, rude, dimension, indiceEvaluacion, justificacion}`.
  2. El sistema crea `SolicitudCorreccion` con estado `PENDIENTE` sin alterar el registro original.
  3. El sistema notifica al Director (in-app).
  4. El Director revisa la solicitud y decide: APROBAR o RECHAZAR.
  5. Si RECHAZA: `SolicitudCorreccion.estado = RECHAZADA`. Notificación al Docente.
  6. Si APRUEBA:
     a. El Director define `alcance` (ESTUDIANTE_ESPECIFICO | CURSO_COMPLETO) y `duracion_horas` (1–72, default 24).
     b. El sistema crea `AutorizacionCorreccion` con `ventana_inicio = now()` y `ventana_fin = now() + duracion_horas`.
     c. El Docente recibe notificación con alcance y `ventana_fin` exacto.
     d. El Docente puede modificar o crear calificaciones dentro del alcance y la materia durante la ventana activa.
     e. Las validaciones de rango de FSD-UC-001 siguen activas.
     f. Cada modificación genera un nuevo registro versionado con `registro_padre_id` → modelo append-only.
  7. El Scheduler verifica periódicamente las ventanas activas:
     - A 30 min del vencimiento: envía alerta al Docente.
     - Al vencer: actualiza `AutorizacionCorreccion.estado = EXPIRADA`. El Docente pierde el permiso de escritura.
  8. Triple entrada en `audit_log`: (1) solicitud del Docente, (2) resolución del Director, (3) cierre de ventana.
- **Flujos alternativos / excepciones:**
  - **A1 — Ventana expirada al intentar modificar:** HTTP 403 con `{error: "E_VENTANA_EXPIRADA"}`. Sugiere nueva solicitud.
  - **A2 — Director aprueba sin definir duración:** El sistema aplica el default de 24 horas y lo notifica explícitamente.
  - **A3 — Docente intenta modificar fuera del alcance autorizado:** HTTP 403 `E_RBAC_VIOLATION`.
- **Postcondiciones:**
  1. Si aprobada: nuevo registro versionado con `registro_padre_id`. El original inmutable.
  2. Tres entradas en `audit_log`.
  3. Centralizador provisional recalculado automáticamente.
- **Reglas de negocio aplicables:** BR-005 (inmutabilidad post-cierre), BR-009, RB-07 (ventana 1–72 h), RB-10 (append-only).
- **Datos de entrada (solicitud):**
  ```json
  {
    "materiaId": "uuid",
    "rude": "string",
    "dimension": "SABER",
    "indiceEvaluacion": 2,
    "justificacion": "string (min 20 chars)"
  }
  ```
- **Datos de salida (autorización):**
  ```json
  {
    "autorizacionId": "uuid",
    "alcance": "ESTUDIANTE_ESPECIFICO|CURSO_COMPLETO",
    "ventanaFin": "ISO-8601",
    "estado": "ACTIVA"
  }
  ```
- **Criterios de aceptación:**

```gherkin
Escenario: Revocación automática al vencer la ventana
  Dado que existe una AutorizacionCorreccion activa con ventana_fin = ahora + 0 min
  Cuando el Scheduler ejecuta la verificación de ventanas
  Entonces autorizacion.estado = EXPIRADA
    Y el Docente recibe notificación de cierre con inventario de cambios realizados
    Y el sistema registra entrada en audit_log tipo VENTANA_EXPIRADA
    Y cualquier intento posterior del Docente responde HTTP 403 E_VENTANA_EXPIRADA

Escenario: Modelo append-only en la modificación
  Dado que el Docente modifica la nota con rude="1234567" dentro de la ventana activa
  Cuando guarda el nuevo valor
  Entonces se crea un nuevo registro Calificacion con {registro_padre_id: id_original, valor: nuevo_valor}
    Y el registro original permanece inmutable con su valor original
    Y audit_log contiene {accion: CALIFICACION_MODIFICADA, valor_anterior: original, valor_nuevo: nuevo}
```

---

### 4.5 FSD-UC-009 — Administración de periodos académicos institucionales

- **Trazabilidad:** `PRD-REQ-002`, `PRD-REQ-003`, `PRD-REQ-004`, `PRD-REQ-005`, `PRD-US-003..PRD-US-006`
- **Actor principal:** Director
- **Precondiciones:**
  1. El Director tiene sesión JWT activa con rol DIRECTOR.
  2. No existe una gestión académica activa con periodos abiertos (para creación de nueva gestión).
- **Disparador:** El Director accede a la sección "Gestión Académica" y crea o administra una gestión.
- **Flujo principal — Creación de gestión:**
  1. El Director invoca `POST /api/v1/gestiones` con `{anio, nombre}`.
  2. El sistema crea `GestionAcademica` con estado `CONFIGURANDO`.
  3. El sistema crea 3 registros `Periodo` (T1, T2, T3) con estado `PENDIENTE`.
  4. El Director configura fechas de inicio/fin para cada trimestre (pueden configurarse todas, la apertura es secuencial).
  5. El Director configura los parámetros académicos de cada periodo: `POST /api/v1/periodos/{id}/parametros`.
  6. El Director asigna docentes a materias: `POST /api/v1/asignaciones`.
  7. El Director invoca `POST /api/v1/periodos/{id}/apertura` para abrir el Trimestre 1.
  8. El sistema verifica que todas las materias del periodo tienen al menos un docente asignado. Si no: HTTP 409 `E_MATERIA_SIN_DOCENTE`.
  9. El sistema verifica que no existe ningún periodo previo en estado `ABIERTO`. Si aplica: HTTP 409 `E_TRIMESTRE_PREVIO_ABIERTO`.
  10. El sistema transiciona el Periodo T1 a estado `ABIERTO`. Los parámetros se congelan (inmutables).
  11. El sistema notifica a todos los docentes del colegio.
- **Flujo principal — Cierre de periodo:**
  1. El Director verifica en el dashboard que todos los centralizadores del periodo están en estado `CERRADO`.
  2. El Director invoca `POST /api/v1/periodos/{id}/cierre`.
  3. El sistema verifica que el 100 % de los centralizadores del periodo estén en `CERRADO`. Si no: HTTP 409 `E_CENTRALIZADORES_INCOMPLETOS`.
  4. El sistema transiciona el Periodo a estado `CERRADO`.
  5. El siguiente periodo queda disponible para apertura.
- **Flujos alternativos:**
  - **A1 — Apertura no secuencial:** HTTP 409 `E_TRIMESTRE_PREVIO_ABIERTO`.
  - **A2 — Parámetros incompletos al intentar abrir:** HTTP 422 `E_PARAMETROS_INCOMPLETOS`.
- **Postcondiciones:**
  1. Periodo en estado `ABIERTO` con parámetros inmutables.
  2. Docentes notificados.
  3. Entrada en `audit_log`: `{accion: PERIODO_ABIERTO, actor: director_id}`.
- **Reglas de negocio aplicables:** RB-04 (solo el Director abre/cierra), RB-05 (apertura secuencial), RB-06 (parámetros inmutables post-apertura), BR-006, BR-007.
- **Criterios de aceptación:**

```gherkin
Escenario: Apertura secuencial válida
  Dado que el Trimestre 1 está en estado CERRADO
    Y el Trimestre 2 está en estado PENDIENTE con parámetros configurados
    Y todas las materias tienen docente asignado
  Cuando el Director ejecuta POST /api/v1/periodos/{t2_id}/apertura
  Entonces el Trimestre 2 pasa a estado ABIERTO
    Y los parámetros quedan inmutables
    Y los docentes reciben notificación
```

---

## 4.6 Casos de uso funcionales — Módulos generalizados *(nuevos desde v2.0 — `ADR-0009`)*

> Nomenclatura vigente (§0.1). Formato abreviado respecto a §4.1–§4.5 para mantener el documento manejable; cada caso de uso puede ampliarse con su propio Design Doc (`DD-UC-NNN`) antes de implementarse. Ningún punto marcado **Pendiente** debe codificarse sin resolver primero el punto correspondiente de `ADR-0009` §3.

### 4.6.1 FSD-UC-011 — Gestión de Tenants y Suscripciones

- **Trazabilidad:** `PRD-REQ-021`, `PRD-US-018`, `PRD-US-019`
- **Actor principal:** SysAdmin
- **Precondiciones:** El SysAdmin tiene sesión activa a nivel plataforma (fuera de cualquier `tenant_id`).
- **Disparador:** El SysAdmin registra una nueva Unidad Educativa o cambia el estado de una existente.
- **Flujo principal:**
  1. El SysAdmin lista los tenants existentes: `GET /api/v1/plataforma/tenants` (consola UI, `DD-UC-004`).
  2. El SysAdmin invoca `POST /api/v1/plataforma/tenants` con `{nombre, fechaInicioSuscripcion, fechaVencimientoSuscripcion}`.
  3. El sistema crea `Tenant` con `estado = ACTIVO`.
  4. El SysAdmin crea el primer usuario `ADMIN` del tenant: `POST /api/v1/plataforma/tenants/{id}/admins`.
  5. Para cambiar el estado: `PATCH /api/v1/plataforma/tenants/{id}/estado` con `{estado: ACTIVO|SUSPENDIDO|VENCIDO}`.
  6. Un scheduler diario marca `VENCIDO` automáticamente a los tenants cuya `fechaVencimientoSuscripcion` ya pasó y no fue renovada.
- **Flujos alternativos / excepciones:**
  - **A1 — Registro sin fecha de vencimiento:** HTTP 422 `E_SUSCRIPCION_INCOMPLETA`.
  - **A2 — Usuario de tenant `SUSPENDIDO`/`VENCIDO` intenta iniciar sesión:** HTTP 403 `E_TENANT_NO_ACTIVO`.
- **Postcondiciones:** `Tenant` persistido con estado y suscripción; ningún dato académico del tenant se elimina ante suspensión/vencimiento.
- **Reglas de negocio aplicables:** BR-013, BR-014, BR-015.
- **Nota (`ADR-0010`, pendiente no bloqueante):** el primer `Usuario` con rol `SYSADMIN` se crea vía *seed*/migración con `tenant_id = NULL` antes de que exista cualquier `Tenant` (arranque del sistema); ese `tenant_id` permanece nulo para siempre, incluso después de crear el primer tenant. La alta y gestión del `Tenant` (registro, estado, suscripción, alta de su primer `ADMIN` en dos pasos separados) quedó implementada en `docs/design/DD-UC-003.md`. Se confirmó que el primer `Tenant` registrado será un **tenant "demo"** con fines de venta (sandbox para prospectos), pero su diseño detallado (alta única vs. bajo demanda, reglas especiales de datos) queda **explícitamente diferido a un Design Doc de seguimiento aún sin crear** (distinto de `DD-UC-003`, que no lo cubre — ver `DD-UC-003` §1 "Fuera de alcance"); no bloquea el modelo de `Usuario`/`Rol` decidido en `ADR-0010` ni la implementación ya realizada de `FSD-UC-011`.
- **Criterios de aceptación:**

```gherkin
Escenario: Bloqueo de acceso por tenant vencido
  Dado un Tenant con fechaVencimientoSuscripcion en el pasado y sin renovación
  Cuando cualquier usuario de ese Tenant intenta iniciar sesión
  Entonces el sistema responde HTTP 403 E_TENANT_NO_ACTIVO
    Y los datos académicos del Tenant permanecen intactos
```

---

### 4.6.2 FSD-UC-012 — Gestión Escolar

- **Trazabilidad:** `PRD-REQ-022`, `PRD-US-020`
- **Actor principal:** Admin
- **Precondiciones:** El Admin tiene sesión activa con `tenant_id` propio; el `Tenant` está `ACTIVO`.
- **Disparador:** El Admin crea una `GestionEscolar` para su tenant.
- **Flujo principal:**
  1. `POST /api/v1/gestiones-escolares` con `{nombre, fechaInicio, fechaFin}`.
  2. El sistema crea `GestionEscolar` con `estado = PLANIFICACION`.
  3. El Admin transiciona a `estado = ACTIVA` una vez configurados sus periodos (FSD-UC-013) y secciones (FSD-UC-014).
  4. Al finalizar el ciclo, el Admin transiciona a `estado = CERRADA`.
- **Flujos alternativos / excepciones:**
  - **A1 — Fechas inválidas (`fechaFin` ≤ `fechaInicio`):** HTTP 422 `E_FECHAS_INVALIDAS`.
- **Postcondiciones:** `GestionEscolar` disponible como contenedor de `PeriodoEvaluacion`, `Curso` e `Inscripcion`.
- **Reglas de negocio aplicables:** BR-016.
- **Datos de entrada:** `{ "nombre": "string", "fechaInicio": "date", "fechaFin": "date" }`

---

### 4.6.3 FSD-UC-013 — Configuración de Periodos de Evaluación

- **Trazabilidad:** `PRD-REQ-023`, `PRD-US-021`
- **Actor principal:** Admin
- **Precondiciones:** `GestionEscolar` en estado `PLANIFICACION` o `ACTIVA`.
- **Disparador:** El Admin define los periodos de su `GestionEscolar`.
- **Flujo principal:**
  1. `POST /api/v1/gestiones-escolares/{id}/periodos` con `{nombre, fechaInicio, fechaFin}`, repetible **N** veces (N ≥ 1, sin máximo fijo en el dominio).
  2. Cada `PeriodoEvaluacion` se crea con `estado = PENDIENTE`.
- **Flujos alternativos / excepciones:**
  - **A1 — Periodos con fechas solapadas:** HTTP 422 `E_PERIODOS_SOLAPADOS`.
- **Postcondiciones:** `GestionEscolar` con N `PeriodoEvaluacion` asociados.
- **Reglas de negocio aplicables:** BR-017.
- **Pendiente de definición (`ADR-0009` §3):** la secuencialidad de apertura entre periodos (equivalente genérico de `RB-05`) y la condición para habilitar un promedio final "anual" (equivalente genérico de `RB-11`) no están definidas; no implementar bloqueo/desbloqueo de apertura entre periodos sin resolver este punto.
- **Criterios de aceptación:**

```gherkin
Escenario: Institución define 2 bimestres en lugar de 3 trimestres
  Dado una GestionEscolar en estado PLANIFICACION
  Cuando el Admin crea los periodos "Bimestre 1" y "Bimestre 2" con sus fechas
  Entonces el sistema acepta la configuración con N=2 sin exigir un tercer periodo
```

---

### 4.6.4 FSD-UC-014 — Configuración de Secciones de Evaluación

- **Trazabilidad:** `PRD-REQ-024`, `PRD-US-022`
- **Actor principal:** Admin
- **Precondiciones:** `PeriodoEvaluacion` existente (§4.6.3).
- **Disparador:** El Admin configura las secciones de evaluación de un periodo.
- **Flujo principal:**
  1. `POST /api/v1/periodos-evaluacion/{id}/secciones` con `{nombre, orden, notaMaxima, pesoPorcentual, cantidadMaximaEvaluaciones}`, repetible.
  2. Cada `SeccionEvaluacion` se crea con `estado = ACTIVA`.
- **Flujos alternativos / excepciones:**
  - **A1 — `pesoPorcentual` fuera de rango `[0, 100]`:** HTTP 422 `E_PESO_INVALIDO`.
- **Postcondiciones:** `PeriodoEvaluacion` con sus `SeccionEvaluacion` configuradas.
- **Reglas de negocio aplicables:** BR-018.
- **Pendiente de definición (`ADR-0009` §3):** validación de que la suma de `pesoPorcentual` de todas las secciones de un periodo sea exactamente 100 %; no implementar esta validación como bloqueante sin confirmación explícita de negocio.
- **Datos de entrada:** `{ "nombre": "string", "orden": "integer", "notaMaxima": "decimal", "pesoPorcentual": "decimal(0-100)", "cantidadMaximaEvaluaciones": "integer" }`

---

### 4.6.5 FSD-UC-015 — Gestión de Evaluaciones y Tipos de Evaluación

- **Trazabilidad:** `PRD-REQ-025`, `PRD-US-023`
- **Actor principal:** Profesor (creación de evaluaciones) / Admin (catálogo de tipos)
- **Precondiciones:** `SeccionEvaluacion` configurada; el Profesor está asignado a la `Materia`/`Curso`/`Paralelo` correspondiente.
- **Disparador:** El Admin crea un `TipoEvaluacion` en el catálogo del tenant, o el Profesor crea una `Evaluacion` dentro de una sección.
- **Flujo principal — Catálogo de tipos:**
  1. `POST /api/v1/tipos-evaluacion` con `{nombre}` (ej. "Examen", "Práctica", "Quiz"), con alcance por `tenant_id`.
- **Flujo principal — Evaluación:**
  1. `POST /api/v1/evaluaciones` con `{nombre, tipoEvaluacionId, seccionEvaluacionId, fecha, puntajeMaximo, descripcion?}`.
  2. El sistema verifica que `tipoEvaluacionId` exista en el catálogo del tenant y que el número de evaluaciones de la sección no exceda `cantidadMaximaEvaluaciones` (§4.6.4).
- **Flujos alternativos / excepciones:**
  - **A1 — Tipo de evaluación no existe en el catálogo del tenant:** HTTP 422 `E_TIPO_EVALUACION_INVALIDO`.
  - **A2 — Cantidad máxima de evaluaciones de la sección excedida:** HTTP 409 `E_LIMITE_EVALUACIONES_SECCION`.
- **Postcondiciones:** `Evaluacion` persistida, referenciando `TipoEvaluacion` y `SeccionEvaluacion`.
- **Reglas de negocio aplicables:** BR-019.
- **Datos de entrada:** `{ "nombre": "string", "tipoEvaluacionId": "uuid", "seccionEvaluacionId": "uuid", "fecha": "date", "puntajeMaximo": "decimal", "descripcion": "string?" }`

---

### 4.6.6 FSD-UC-016 — Cálculo de Notas configurable

- **Trazabilidad:** `PRD-REQ-026`, `PRD-US-024`
- **Actor principal:** Sistema (Motor de Cálculo de Notas)
- **Precondiciones:** Existen `Evaluacion` calificadas dentro de las `SeccionEvaluacion` de un periodo.
- **Disparador:** Registro/actualización de una calificación de `Evaluacion`, análogo al patrón de `FSD-UC-003`.
- **Flujo principal:**
  1. El motor agrupa las calificaciones de un estudiante por `SeccionEvaluacion`.
  2. Calcula el promedio de cada sección sobre las evaluaciones registradas.
  3. Calcula la nota final del periodo como la suma ponderada: `Σ (promedio_seccion × pesoPorcentual_seccion / 100)`.
  4. Persiste el resultado como vista provisional del periodo.
- **Flujos alternativos / excepciones:**
  - **A1 — Sección sin evaluaciones registradas para un estudiante:** el sistema excluye la sección del cálculo y marca el resultado como `INCOMPLETO` hasta que exista al menos una evaluación.
- **Postcondiciones:** Nota final del periodo disponible como vista provisional (no oficial).
- **Reglas de negocio aplicables:** BR-020.
- **Pendiente de definición (`ADR-0009` §3):** el criterio de redondeo/truncado (si `floor()` se mantiene como default configurable o se abre a otras estrategias) y la gobernanza de inmutabilidad de este cálculo (equivalente genérico de `BR-005`/`BR-011`) no están definidos; no asumir `floor()` como comportamiento por defecto sin confirmación explícita.

---

### 4.6.7 FSD-UC-017 — Gestión de Cursos y Paralelos

- **Trazabilidad:** `PRD-REQ-027`, `PRD-US-025`
- **Actor principal:** Admin
- **Precondiciones:** `Tenant` activo.
- **Disparador:** El Admin crea un `Curso` y sus `Paralelo`.
- **Flujo principal:**
  1. `POST /api/v1/cursos` con `{nombre}` (ej. "Primero de Primaria").
  2. `GET /api/v1/cursos` (lista filtrable y paginada del tenant: `q`, `page`, `size`) — `DD-UC-010` / `PR-IMPL-010`.
  3. `POST /api/v1/cursos/{id}/paralelos` con `{nombre}` (ej. "A"), repetible.
  4. `GET /api/v1/cursos/{id}/paralelos` (lista simple, sin paginar) — `DD-UC-010` / `PR-IMPL-010`.
- **Flujos alternativos / excepciones:**
  - **A1 — Curso inexistente o de otro tenant:** HTTP 404 `E_CURSO_NO_ENCONTRADO` en las operaciones sobre `/cursos/{id}/paralelos`.
- **Postcondiciones:** `Curso` con uno o más `Paralelo` asociados, disponibles para `Materia` e `Inscripcion`.
- **Reglas de negocio aplicables:** BR-021.
- **Implementación:** backend `DD-UC-010`/`PR-IMPL-010` (ejecutado 20/08/2026); UI Angular `DD-UC-011`/`PR-IMPL-011` (ejecutado 21/08/2026). Fuera de este slice: `PATCH`/`DELETE` de `Curso`/`Paralelo`.

---

### 4.6.8 FSD-UC-018 — Gestión de Materias

- **Trazabilidad:** `PRD-REQ-028`, `PRD-US-026` — `DD-UC-012` / `PR-IMPL-012` (backend + UI fullstack, 21/08/2026).
- **Actor principal:** Admin / Secretaria
- **Precondiciones:** `Curso` existente (§4.6.7).
- **Disparador:** CRUD de `Materia` y su asignación a `Curso` y a `Profesor`.
- **Flujo principal:**
  1. `POST /api/v1/materias` con `{nombre}`.
  2. `GET /api/v1/materias` (lista filtrable y paginada del tenant: `q`, `page`, `size`).
  3. `GET /api/v1/materias/{id}` (detalle).
  4. `GET /api/v1/materias/profesores-disponibles` (catálogo `{id, nombreCompleto}` de usuarios `PROFESOR` activos del tenant; no es `FSD-UC-019`).
  5. `POST /api/v1/materias/{id}/asignaciones-curso` con `{cursoId, paraleloId}`.
  6. `GET /api/v1/materias/{id}/asignaciones-curso` (lista simple, sin paginar).
  7. `POST /api/v1/materias/{id}/asignaciones-profesor` con `{profesorId, cursoId, paraleloId}`.
  8. `GET /api/v1/materias/{id}/asignaciones-profesor` (lista simple, sin paginar).
- **Flujos alternativos / excepciones:**
  - **A1 — Asignación de profesor a materia sin curso previamente asignado:** HTTP 409 `E_MATERIA_SIN_CURSO`.
  - **A2 — Materia inexistente o de otro tenant:** HTTP 404 `E_MATERIA_NO_ENCONTRADA`.
  - **A3 — Curso / Paralelo / Profesor inexistente o de otro tenant:** HTTP 404 `E_CURSO_NO_ENCONTRADO` / `E_PARALELO_NO_ENCONTRADO` / `E_PROFESOR_NO_ENCONTRADO`.
- **Notas de implementación (`DD-UC-012`):**
  - `Materia` es catálogo del tenant; las FKs a curso/profesor viven en aggregates de asignación independientes (no embebidas en `Materia`).
  - `GET /api/v1/cursos` y `GET /api/v1/cursos/{id}/paralelos` también admiten `SECRETARIA` (los `POST` de Curso/Paralelo siguen `ADMIN`).
  - `PATCH`/`DELETE` de Materia o asignaciones permanecen fuera de este slice.
- **Postcondiciones:** `Materia` con `Curso`/`Paralelo` y `Profesor` asignados; prerequisito de `FSD-UC-015`.
- **Reglas de negocio aplicables:** BR-022.

---

### 4.6.9 FSD-UC-019 — Gestión de Profesores

- **Trazabilidad:** `PRD-REQ-029`, `PRD-US-026`
- **Actor principal:** Admin / Secretaria
- **Precondiciones:** `Tenant` activo.
- **Disparador:** CRUD de `Profesor` (como perfil dentro de `Usuario` con rol `PROFESOR`) y consulta de sus asignaciones vigentes.
- **Flujo principal:**
  1. `POST /api/v1/usuarios` con `rol = PROFESOR` (ver `FSD-UC-021`).
  2. `GET /api/v1/profesores/{id}/asignaciones` retorna las `Materia`/`Curso`/`Paralelo` asignadas (originadas en `FSD-UC-018`).
- **Postcondiciones:** `Profesor` disponible para asignación a `Materia`.
- **Reglas de negocio aplicables:** BR-022.

---

### 4.6.10 FSD-UC-020 — Gestión de Estudiantes e Inscripciones

- **Trazabilidad:** `PRD-REQ-030`, `PRD-US-027`, `PRD-US-028` — `DD-UC-013` / `PR-IMPL-013` (backend + UI fullstack, 21/08/2026).
- **Actor principal:** Secretaria / Admin
- **Precondiciones:** `GestionEscolar`, `Curso` y `Paralelo` existentes para la inscripción.
- **Disparador:** Alta de un `Estudiante` (independiente de su matrícula) y su posterior `Inscripcion`.
- **Flujo principal:**
  1. `POST /api/v1/estudiantes` con `{rude, nombreCompleto, estado?, datosPersonales?}` — `rude` obligatorio y único por tenant (`BR-004`/`RB-01`); sin requerir `GestionEscolar`/`Curso` en este paso.
  2. `GET /api/v1/estudiantes` (lista filtrable y paginada: `q` sobre `nombreCompleto` contains **o** `rude` exacto, `estado`, `page`, `size`).
  3. `GET /api/v1/estudiantes/{id}` (detalle).
  4. `POST /api/v1/inscripciones` con `{estudianteId, gestionEscolarId, cursoId, paraleloId, fechaInscripcion}` — el body **no** acepta `estado`.
  5. El sistema crea `Inscripcion` con `estado = ACTIVA`.
  6. `GET /api/v1/estudiantes/{id}/inscripciones` (historial, lista simple sin paginar).
- **Flujos alternativos / excepciones:**
  - **A1 — Inscripción duplicada del mismo estudiante en la misma `GestionEscolar`:** HTTP 409 `E_INSCRIPCION_DUPLICADA`.
  - **A2 — Estudiante / Gestión Escolar / Curso / Paralelo inexistente o de otro tenant:** HTTP 404 `E_ESTUDIANTE_NO_ENCONTRADO` / `E_GESTION_ESCOLAR_NO_ENCONTRADA` / `E_CURSO_NO_ENCONTRADO` / `E_PARALELO_NO_ENCONTRADO`.
  - **A3 — RUDE duplicado en el tenant:** HTTP 409 `E_RUDE_DUPLICADO` (el mensaje **no** interpola el código).
- **Notas de implementación (`DD-UC-013`):**
  - `Estudiante` e `Inscripcion` son Aggregates independientes (`BR-023`); el historial se reconstruye listando inscripciones, no embebiendo una colección.
  - `GET /api/v1/gestiones-escolares` también admite `SECRETARIA` (los `POST`/`PATCH` de Gestión Escolar siguen `ADMIN`). Los GET de Cursos/Paralelos ya admitían `SECRETARIA` (`DD-UC-012`).
  - `PATCH`/`DELETE` de Estudiante o Inscripcion permanecen fuera de este slice.
- **Postcondiciones:** El historial académico del `Estudiante` es reconstructible a través de todas sus `Inscripcion` en distintas `GestionEscolar`.
- **Reglas de negocio aplicables:** BR-023, BR-004.
- **Criterios de aceptación:**

```gherkin
Escenario: Historial académico a través de dos gestiones escolares
  Dado un Estudiante con una Inscripcion en la GestionEscolar "2026" (curso "1ro A")
  Cuando la Secretaria crea una nueva Inscripcion del mismo estudiante en la GestionEscolar "2027" (curso "2do A")
  Entonces ambas Inscripciones permanecen consultables como historial académico del estudiante
```

---

### 4.6.11 FSD-UC-021 — Gestión de Usuarios y Roles

- **Trazabilidad:** `PRD-REQ-031`, `PRD-US-029`, `PRD-US-030`
- **Actor principal:** Admin (usuarios de su tenant) / SysAdmin (usuarios `ADMIN`, ver `FSD-UC-011`)
- **Precondiciones:** `Tenant` activo.
- **Disparador:** CRUD de `Usuario`, asignación de rol, activación/desactivación, restablecimiento de contraseña.
- **Flujo principal:**
  1. `POST /api/v1/usuarios` con `{nombre, email, roles: [ADMIN|SECRETARIA|ASESOR|PROFESOR, ...], cursoAsignado? (requerido si roles incluye ASESOR)}` — `roles` acepta uno o más valores simultáneos (`ADR-0010`). El rol `SYSADMIN` **no** se crea por este endpoint (ver `FSD-UC-011`, alcance de plataforma).
  2. El sistema valida la invariante de exclusión mutua: si `roles` incluyera `SYSADMIN`, se rechaza (este endpoint solo crea usuarios de tenant, con `tenant_id` no nulo).
  3. `PATCH /api/v1/usuarios/{id}/roles` con `{roles: [...]}` permite modificar el conjunto de roles vigentes de un usuario de tenant, repitiendo la misma validación.
  4. `PATCH /api/v1/usuarios/{id}/estado` con `{activo: boolean}`.
  5. `POST /api/v1/usuarios/{id}/restablecer-password` inicia el flujo de restablecimiento (enlace de un solo uso).
  6. `GET /api/v1/usuarios` lista los usuarios del tenant del Admin autenticado (paso de lectura no explícito en la redacción original; añadido en `DD-UC-005`/`PR-IMPL-005`, mismo precedente que `GET /tenants` en `FSD-UC-011`/`DD-UC-004`: un CRUD sin lectura no es operable).
- **Flujos alternativos / excepciones:**
  - **A1 — Rol `ASESOR` sin curso/paralelo asignado:** HTTP 422 `E_ASESOR_SIN_CURSO`. **Diferido** (`DD-UC-005`, 04/08/2026): el rol `ASESOR` ya puede asignarse (el modelo de roles de `ADR-0010` lo soporta), pero esta validación de la referencia a `Curso`/`Paralelo` no está implementada — depende del módulo `academico` (`FSD-UC-012..020`), bloqueado por los 5 puntos pendientes de `ADR-0009` §3.
  - **A2 — Enlace de restablecimiento ya usado o expirado:** HTTP 410 `E_ENLACE_INVALIDO`.
  - **A3 — Intento de asignar `SYSADMIN` combinado con un rol de tenant, o a un usuario con `tenant_id` no nulo:** HTTP 422 `E_ROL_INCOMPATIBLE` (`ADR-0010`).
  - **A4 — `roles` vacío:** HTTP 422 `E_ROLES_VACIO` (todo usuario activo requiere al menos un rol vigente).
- **Postcondiciones:** `Usuario` con uno o más roles vigentes (`UsuarioRol`); ningún usuario combina `SYSADMIN` con un rol de tenant; historial de restablecimientos no reutilizables.
- **Reglas de negocio aplicables:** BR-024, `ADR-0010`.
- **Criterios de aceptación:**

```gherkin
Escenario: Admin asigna dos roles al mismo usuario
  Dado un Admin autenticado en su Unidad Educativa
  Cuando crea el usuario "Marco Ríos" con roles ["ADMIN", "SECRETARIA"]
  Entonces el sistema responde HTTP 201 y persiste ambos roles en UsuarioRol
    Y Marco puede operar con los permisos combinados de ambos roles

Escenario: Rechazo de SYSADMIN combinado con rol de tenant
  Dado un usuario de tenant existente con tenant_id no nulo
  Cuando se intenta agregarle el rol "SYSADMIN" vía PATCH /api/v1/usuarios/{id}/roles
  Entonces el sistema responde HTTP 422 con error E_ROL_INCOMPATIBLE
    Y no persiste el cambio
```

---

## 5. Reglas de negocio ⚡🔧

| ID | Regla | Tipo | Origen | Casos de uso afectados |
|----|-------|------|--------|------------------------|
| BR-001 | El Docente escribe únicamente en las materias que tiene asignadas. Verificación en cada request mediante `asignacion_docente`. | validación / política | `PRD-REQ-001`, DA-01 | FSD-UC-001, FSD-UC-005 |
| BR-002 | Ningún valor fuera del rango paramétrico de la dimensión puede persistirse. La validación ocurre antes del `INSERT`. | validación | `PRD-REQ-006`, DA-02 | FSD-UC-001 |
| BR-003 | El criterio de truncado de decimales es `floor` (piso), no redondeo estándar ni bancario. `floor(64.666) = 64`. | cálculo | `PRD-REQ-010`, DA-02 | FSD-UC-003 |
| BR-004 | La vinculación al SIE es exclusivamente por RUDE. Ningún payload puede usar nombre, apellido ni posición de lista. | política + normativa SIE | `PRD-REQ-012`, Ministerio de Educación Bolivia | FSD-UC-004 |
| BR-005 | Ningún registro cerrado puede modificarse sin autorización explícita del Director con ventana temporal definida. | política | `PRD-REQ-014`, BR-v2 BR-005 | FSD-UC-005 |
| BR-006 | La apertura de periodos trimestrales es estrictamente secuencial: T2 no puede abrirse sin T1 cerrado. | política | `PRD-REQ-005`, RB-05 | FSD-UC-009 |
| BR-007 | Los parámetros académicos del periodo (dimensiones, pesos, reglas) son inmutables desde el momento en que el periodo está en estado `ABIERTO`. | política | `PRD-REQ-003`, DA-02 | FSD-UC-001, FSD-UC-003, FSD-UC-009 |
| BR-008 | El cálculo de promedios y la conversión a escala SIE (`floor(nota/3)`) ocurren exclusivamente en el motor de dominio. Ningún cálculo puede ejecutarse en adaptadores, SQL ad-hoc ni en el frontend. | arquitectura | DA-02, Constitución §0.1 | FSD-UC-003, FSD-UC-004 |
| BR-009 | Toda autorización de modificación retroactiva incluye una ventana de expiración entre 1 y 72 horas (default: 24 h). No existe autorización indefinida. La revocación es automática sin intervención manual. | política | RB-07, `PRD-REQ-015` | FSD-UC-005 |
| BR-010 | El `audit_log` es inalterable: no se permiten `UPDATE` ni `DELETE` sobre ninguna fila de esta tabla. La escritura solo ocurre mediante el servicio de auditoría (AOP interceptor o Hibernate Envers). | política de datos | `PRD-REQ-018`, DA-03 | Todos |
| BR-011 | El promedio anual y el índice de reprobación anual solo se calculan y muestran cuando los 3 trimestres del año académico están en estado `CERRADO`. | cálculo | `PRD-REQ-011`, RB-11 | FSD-UC-003, FSD-UC-010 |
| BR-012 | La nómina de estudiantes no reasigna posiciones numéricas: el alta de un alumno crea un nuevo identificador; la baja marca el registro como `RETIRADO` sin alterar el orden de los demás. | política de integridad | RB-03, BRD BR-004 | FSD-UC-006 |

> **BR-001..BR-012 son del Perfil Bolivia SIE** y siguen vigentes sin cambios.

### 5.1 Reglas de negocio del modelo generalizado *(nuevas desde v2.0 — `ADR-0009`)*

| ID | Regla | Tipo | Origen | Casos de uso afectados |
|----|-------|------|--------|------------------------|
| BR-013 | El SysAdmin registra `Tenant` con fecha de inicio y vencimiento de suscripción, y administra su estado (`ACTIVO`/`SUSPENDIDO`/`VENCIDO`). | política | BRD BR-013 | FSD-UC-011 |
| BR-014 | Ningún usuario de un `Tenant` en estado `SUSPENDIDO` o `VENCIDO` puede autenticarse; sus datos académicos no se eliminan. | política + validación | BRD BR-014 | FSD-UC-011 |
| BR-015 | El SysAdmin administra usuarios `ADMIN` de cada tenant sin acceso a los datos académicos del tenant. | política de aislamiento | BRD BR-015 | FSD-UC-011 |
| BR-016 | Toda `GestionEscolar` requiere nombre, fecha de inicio, fecha de fin y estado (`PLANIFICACION`/`ACTIVA`/`CERRADA`) antes de pasar a `ACTIVA`. | validación | BRD BR-016 | FSD-UC-012 |
| BR-017 | El número de `PeriodoEvaluacion` de una `GestionEscolar` es configurable (N ≥ 1); el dominio no asume un valor fijo. | arquitectura | BRD BR-017 | FSD-UC-013 |
| BR-018 | Cada `SeccionEvaluacion` de un periodo define nombre, orden, nota máxima, peso porcentual y cantidad máxima de evaluaciones; el dominio no asume secciones fijas. | arquitectura | BRD BR-018 | FSD-UC-014 |
| BR-019 | Toda `Evaluacion` referencia un `TipoEvaluacion` del catálogo configurable del tenant; el dominio no codifica tipos de evaluación de forma fija. | arquitectura | BRD BR-019 | FSD-UC-015 |
| BR-020 | La nota final de un periodo se calcula como la suma ponderada de los promedios de cada `SeccionEvaluacion` según su peso porcentual configurado. | cálculo | BRD BR-020 | FSD-UC-016 |
| BR-021 | Un `Curso` puede tener uno o más `Paralelo`; `Materia` e `Inscripcion` referencian siempre un `Curso` y, cuando aplica, un `Paralelo` válidos. | validación | BRD BR-021 | FSD-UC-017 |
| BR-022 | Ninguna `Evaluacion` puede registrarse sobre una `Materia` sin `Profesor` asignado. | validación | BRD BR-022 | FSD-UC-018, FSD-UC-019 |
| BR-023 | `Estudiante` e `Inscripcion` son entidades independientes; el historial académico de un estudiante se reconstruye a través de todas sus `Inscripcion` en distintas `GestionEscolar`. | arquitectura | BRD BR-023 | FSD-UC-020 |
| BR-024 | Todo `Usuario` activo tiene **uno o más** roles vigentes (`SYSADMIN`/`ADMIN`/`SECRETARIA`/`ASESOR`/`PROFESOR`), modelados como relación N:M (`UsuarioRol`, §6.3.2). El rol `ASESOR` requiere `Curso`/`Paralelo` asignado. **Invariante:** `tenant_id IS NULL` ⟺ el conjunto de roles es exactamente `{SYSADMIN}`; `SYSADMIN` nunca se combina con un rol de tenant en el mismo usuario, de forma permanente (`ADR-0010`). | validación | BRD BR-024, `ADR-0010` | FSD-UC-021 |

> **Pendiente de definición (`ADR-0009` §3):** BR-013..BR-024 no incluyen todavía reglas de gobernanza (auditoría inalterable, inmutabilidad post-cierre, ventana de corrección retroactiva) equivalentes a BR-005/BR-009/BR-010/BR-011, ni la generalización de BR-006/BR-007 (secuencialidad y parámetros inmutables) a N periodos, ni la validación de suma de pesos de BR-018. No implementar ninguno de estos comportamientos en código sin una decisión explícita de seguimiento.
>
> **Nota (`ADR-0010`):** BR-024 refina la redacción original de `ADR-0009` ("exactamente un rol") a un modelo multi-rol. Ver §6.3.1 (diagrama ER con `USUARIO_ROL`), §6.3.2 (diccionario de datos) y §4.6.11 (`FSD-UC-021`).

---

## 6. Modelo de datos funcional ⚡🔧

### 6.1 Diagrama ER (Mermaid)

```mermaid
erDiagram
    TENANT ||--o{ USUARIO : tiene
    TENANT ||--o{ GESTION_ACADEMICA : posee
    TENANT ||--o{ ESTUDIANTE : matricula

    GESTION_ACADEMICA ||--o{ PERIODO : contiene
    PERIODO ||--o{ PARAMETRO_ACADEMICO : define
    PERIODO ||--o{ ASIGNACION_DOCENTE : registra
    PERIODO ||--o{ EXPORTACION_SIE : genera

    USUARIO ||--o{ ASIGNACION_DOCENTE : asignado
    ASIGNACION_DOCENTE }o--|| MATERIA : cubre

    MATERIA ||--o{ CALIFICACION : recibe
    ESTUDIANTE ||--o{ CALIFICACION : tiene

    CALIFICACION ||--o{ CALIFICACION : "versionada (padre)"
    CALIFICACION }o--|| SOLICITUD_CORRECCION : "origina"

    SOLICITUD_CORRECCION ||--o| AUTORIZACION_CORRECCION : resuelve

    PERIODO ||--o{ CENTRALIZADOR : produce
    ESTUDIANTE ||--o{ CENTRALIZADOR : incluye

    EXPORTACION_SIE ||--o{ EXPORTACION_REGISTRO : contiene

    TENANT ||--o{ AUDIT_LOG : registra
    USUARIO ||--o{ AUDIT_LOG : genera
```

### 6.2 Diccionario de datos

| Entidad | Atributo | Tipo | Obligatorio | Validaciones | Origen |
|---------|----------|------|-------------|--------------|--------|
| **Tenant** | `id` | UUID | sí | UUIDv4 | sistema |
| **Tenant** | `nombre` | VARCHAR(200) | sí | no nulo | operador |
| **Usuario** | `id` | UUID | sí | UUIDv4 | sistema |
| **Usuario** | `tenant_id` | UUID (FK) | sí | FK a Tenant | sistema |
| **Usuario** | `rol` | ENUM | sí | DIRECTOR / SECRETARIA / DOCENTE | admin |
| **Usuario** | `email` | VARCHAR(120) | sí | regex RFC 5322; único por tenant | usuario |
| **GestionAcademica** | `id` | UUID | sí | — | sistema |
| **GestionAcademica** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **GestionAcademica** | `anio` | INTEGER | sí | YYYY, único por tenant | director |
| **GestionAcademica** | `estado` | ENUM | sí | CONFIGURANDO / ACTIVA / CERRADA | sistema |
| **Periodo** | `id` | UUID | sí | — | sistema |
| **Periodo** | `gestion_id` | UUID (FK) | sí | FK a GestionAcademica | sistema |
| **Periodo** | `nombre` | VARCHAR(50) | sí | ej. "Trimestre 1" | director |
| **Periodo** | `estado` | ENUM | sí | PENDIENTE / ABIERTO / CERRADO | sistema |
| **Periodo** | `fecha_inicio` | DATE | sí | < fecha_fin | director |
| **Periodo** | `fecha_fin` | DATE | sí | > fecha_inicio | director |
| **ParametroAcademico** | `id` | UUID | sí | — | sistema |
| **ParametroAcademico** | `periodo_id` | UUID (FK) | sí | — | sistema |
| **ParametroAcademico** | `dimension` | ENUM | sí | SER / SABER / HACER / DECIDIR / AUTOEVALUACION | director |
| **ParametroAcademico** | `peso_max` | DECIMAL(5,2) | sí | > 0; suma de pesos ≤ 100 | director |
| **ParametroAcademico** | `rango_min` | DECIMAL(5,2) | sí | ≥ 0 | director |
| **ParametroAcademico** | `rango_max` | DECIMAL(5,2) | sí | > rango_min | director |
| **ParametroAcademico** | `regla_combinacion` | ENUM | sí | PROMEDIO_SIMPLE / SUMA / MEJOR_N | director |
| **Estudiante** | `id` | UUID | sí | — | sistema |
| **Estudiante** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **Estudiante** | `rude` | VARCHAR(20) | sí | único por tenant; no nulo | secretaría |
| **Estudiante** | `nombre_completo` | VARCHAR(200) | sí | — | secretaría |
| **Estudiante** | `estado` | ENUM | sí | ACTIVO / RETIRADO / TRANSFERIDO | secretaría |
| **Calificacion** | `id` | UUID | sí | — | sistema |
| **Calificacion** | `materia_id` | UUID (FK) | sí | — | sistema |
| **Calificacion** | `rude` | VARCHAR(20) | sí | FK a Estudiante.rude | docente |
| **Calificacion** | `dimension` | ENUM | sí | SER/SABER/HACER/DECIDIR/AUTOEVALUACION | docente |
| **Calificacion** | `indice_evaluacion` | INTEGER | sí | ≥ 1 | docente |
| **Calificacion** | `tipo` | ENUM | sí | REGULAR / AYUDA | docente |
| **Calificacion** | `valor` | DECIMAL(5,2) | sí | en [rango_min, rango_max] del parámetro | docente |
| **Calificacion** | `registro_padre_id` | UUID (FK, nullable) | no | si es modificación retroactiva: FK al original | sistema |
| **Calificacion** | `timestamp_utc` | TIMESTAMPTZ | sí | generado por sistema | sistema |
| **Centralizador** | `id` | UUID | sí | — | sistema |
| **Centralizador** | `curso_id` | UUID (FK) | sí | — | sistema |
| **Centralizador** | `periodo_id` | UUID (FK) | sí | — | sistema |
| **Centralizador** | `rude` | VARCHAR(20) | sí | — | sistema |
| **Centralizador** | `puntaje_total` | INTEGER | sí | resultado de `floor(suma de dimensiones)` | motor |
| **Centralizador** | `estado` | ENUM | sí | PROVISIONAL / OFICIAL | motor |
| **ExportacionSIE** | `id` | UUID | sí | — | sistema |
| **ExportacionSIE** | `periodo_id` | UUID (FK) | sí | — | secretaría |
| **ExportacionSIE** | `estado` | ENUM | sí | EN_PROGRESO / COMPLETA / PARCIAL / FALLIDA | motor |
| **ExportacionRegistro** | `id` | UUID | sí | — | sistema |
| **ExportacionRegistro** | `exportacion_id` | UUID (FK) | sí | — | sistema |
| **ExportacionRegistro** | `rude` | VARCHAR(20) | sí | clave de idempotencia con `exportacion.periodo_id` | motor |
| **ExportacionRegistro** | `estado` | ENUM | sí | PENDIENTE / ENVIADO / FALLIDO / EXCLUIDO_SIN_RUDE / EXCLUIDO_NOTA_INCOMPLETA | motor |
| **SolicitudCorreccion** | `id` | UUID | sí | — | sistema |
| **SolicitudCorreccion** | `docente_id` | UUID (FK) | sí | — | docente |
| **SolicitudCorreccion** | `materia_id` | UUID (FK) | sí | — | docente |
| **SolicitudCorreccion** | `rude` | VARCHAR(20) | sí | — | docente |
| **SolicitudCorreccion** | `justificacion` | TEXT | sí | ≥ 20 caracteres | docente |
| **SolicitudCorreccion** | `estado` | ENUM | sí | PENDIENTE / APROBADA / RECHAZADA | director |
| **AutorizacionCorreccion** | `id` | UUID | sí | — | sistema |
| **AutorizacionCorreccion** | `solicitud_id` | UUID (FK) | sí | — | director |
| **AutorizacionCorreccion** | `alcance` | ENUM | sí | ESTUDIANTE_ESPECIFICO / CURSO_COMPLETO | director |
| **AutorizacionCorreccion** | `ventana_inicio` | TIMESTAMPTZ | sí | = now() al aprobar | sistema |
| **AutorizacionCorreccion** | `ventana_fin` | TIMESTAMPTZ | sí | ventana_inicio + duracion_horas (1–72) | director |
| **AutorizacionCorreccion** | `estado` | ENUM | sí | ACTIVA / EXPIRADA / COMPLETADA | sistema |
| **AuditLog** | `id` | UUID | sí | — | sistema |
| **AuditLog** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **AuditLog** | `actor_id` | UUID (FK) | sí | — | sistema (AOP) |
| **AuditLog** | `accion` | VARCHAR(50) | sí | enum de acciones predefinidas | sistema (AOP) |
| **AuditLog** | `entidad_afectada` | VARCHAR(50) | sí | nombre de la entidad | sistema (AOP) |
| **AuditLog** | `entidad_id` | UUID | sí | ID del registro afectado | sistema (AOP) |
| **AuditLog** | `valor_anterior` | JSONB | no | snapshot antes del cambio | sistema (AOP) |
| **AuditLog** | `valor_nuevo` | JSONB | sí | snapshot después del cambio | sistema (AOP) |
| **AuditLog** | `timestamp_utc` | TIMESTAMPTZ | sí | generado por sistema (no modificable) | sistema |

> **§6.1 y §6.2 son del Perfil Bolivia SIE** y siguen vigentes sin cambios.

### 6.3 Modelo de datos del modelo generalizado *(nuevo desde v2.0 — `ADR-0009`)*

> **Pendiente de definición (`ADR-0009` §3):** la reconciliación entre este modelo y el de §6.1/§6.2 (`GestionAcademica`/`ParametroAcademico`) no está resuelta. Ambos conviven en este documento hasta que un Design Doc de seguimiento decida si el Perfil Bolivia SIE se migra a una instancia parametrizada de este modelo genérico o si permanecen como rutas de datos separadas.

#### 6.3.1 Diagrama ER (Mermaid)

```mermaid
erDiagram
    TENANT ||--o{ USUARIO : tiene
    TENANT ||--o{ GESTION_ESCOLAR : posee
    TENANT ||--o{ ESTUDIANTE : matricula
    TENANT ||--o{ TIPO_EVALUACION : define

    USUARIO ||--o{ USUARIO_ROL : tiene

    GESTION_ESCOLAR ||--o{ PERIODO_EVALUACION : contiene
    GESTION_ESCOLAR ||--o{ INSCRIPCION : agrupa

    PERIODO_EVALUACION ||--o{ SECCION_EVALUACION : define

    SECCION_EVALUACION ||--o{ EVALUACION : contiene
    TIPO_EVALUACION ||--o{ EVALUACION : clasifica

    CURSO ||--o{ PARALELO : tiene
    CURSO ||--o{ MATERIA : ofrece
    MATERIA }o--|| USUARIO : "asignada a (PROFESOR)"

    ESTUDIANTE ||--o{ INSCRIPCION : registra
    CURSO ||--o{ INSCRIPCION : recibe
    PARALELO ||--o{ INSCRIPCION : recibe

    USUARIO }o--o| CURSO : "ASESOR asignado a"
```

> **`USUARIO_ROL` (nuevo, `ADR-0010`):** relación N:M que reemplaza al atributo `Usuario.rol` de valor único (`ADR-0009`). Un `Usuario` puede tener uno o más roles; ver invariante de exclusión mutua `SYSADMIN`/tenant en §6.3.2 y `BR-024` (§5.1).

#### 6.3.2 Diccionario de datos

| Entidad | Atributo | Tipo | Obligatorio | Validaciones | Origen |
|---------|----------|------|-------------|---------------|--------|
| **Tenant** | `id` | UUID | sí | UUIDv4 | sistema |
| **Tenant** | `nombre` | VARCHAR(200) | sí | no nulo | SysAdmin |
| **Tenant** | `fecha_inicio_suscripcion` | DATE | sí | — | SysAdmin |
| **Tenant** | `fecha_vencimiento_suscripcion` | DATE | sí | > fecha_inicio_suscripcion | SysAdmin |
| **Tenant** | `estado` | ENUM | sí | ACTIVO / SUSPENDIDO / VENCIDO | sistema / SysAdmin |
| **Usuario** | `id` | UUID | sí | UUIDv4 | sistema |
| **Usuario** | `tenant_id` | UUID (FK, nullable) | no | nulo **de forma permanente** ⟺ el conjunto de roles del usuario es exactamente `{SYSADMIN}` (`ADR-0010`); no es una condición transitoria de *bootstrap* | sistema |
| **Usuario** | `email` | VARCHAR(120) | sí | regex RFC 5322; único por tenant | usuario |
| **Usuario** | `activo` | BOOLEAN | sí | default true | Admin |
| **Usuario** | `curso_asignado_id` | UUID (FK, nullable) | no | requerido si el usuario tiene el rol `ASESOR` | Admin |
| **UsuarioRol** *(nuevo, `ADR-0010`)* | `usuario_id` | UUID (FK) | sí | FK a Usuario | sistema |
| **UsuarioRol** *(nuevo, `ADR-0010`)* | `rol` | ENUM | sí | SYSADMIN / ADMIN / SECRETARIA / ASESOR / PROFESOR; par (`usuario_id`, `rol`) único | Admin / SysAdmin |

> **Invariante de exclusión mutua (`ADR-0010`):** `Usuario.tenant_id IS NULL` ⟺ el conjunto de `UsuarioRol.rol` de ese usuario es exactamente `{SYSADMIN}`. Ningún usuario puede tener `SYSADMIN` junto con un rol de tenant, ni un rol de tenant con `tenant_id` nulo. Reemplaza al atributo `Usuario.rol` (ENUM de valor único) definido originalmente en `ADR-0009`.
| **GestionEscolar** | `id` | UUID | sí | — | sistema |
| **GestionEscolar** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **GestionEscolar** | `nombre` | VARCHAR(100) | sí | — | Admin |
| **GestionEscolar** | `fecha_inicio` | DATE | sí | < fecha_fin | Admin |
| **GestionEscolar** | `fecha_fin` | DATE | sí | > fecha_inicio | Admin |
| **GestionEscolar** | `estado` | ENUM | sí | PLANIFICACION / ACTIVA / CERRADA | sistema |
| **PeriodoEvaluacion** | `id` | UUID | sí | — | sistema |
| **PeriodoEvaluacion** | `gestion_escolar_id` | UUID (FK) | sí | — | sistema |
| **PeriodoEvaluacion** | `nombre` | VARCHAR(50) | sí | ej. "Bimestre 1" | Admin |
| **PeriodoEvaluacion** | `fecha_inicio` / `fecha_fin` | DATE | sí | — | Admin |
| **PeriodoEvaluacion** | `estado` | ENUM | sí | PENDIENTE / ABIERTO / CERRADO *(transiciones pendientes de definición, `ADR-0009` §3)* | sistema |
| **SeccionEvaluacion** | `id` | UUID | sí | — | sistema |
| **SeccionEvaluacion** | `periodo_evaluacion_id` | UUID (FK) | sí | — | sistema |
| **SeccionEvaluacion** | `nombre` | VARCHAR(50) | sí | ej. "Saber" | Admin |
| **SeccionEvaluacion** | `orden` | INTEGER | sí | ≥ 1 | Admin |
| **SeccionEvaluacion** | `nota_maxima` | DECIMAL(5,2) | sí | > 0 | Admin |
| **SeccionEvaluacion** | `peso_porcentual` | DECIMAL(5,2) | sí | en [0, 100]; suma de pesos del periodo pendiente de validación (`ADR-0009` §3) | Admin |
| **SeccionEvaluacion** | `cantidad_maxima_evaluaciones` | INTEGER | sí | ≥ 1 | Admin |
| **SeccionEvaluacion** | `estado` | ENUM | sí | ACTIVA / INACTIVA | Admin |
| **TipoEvaluacion** | `id` | UUID | sí | — | sistema |
| **TipoEvaluacion** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **TipoEvaluacion** | `nombre` | VARCHAR(50) | sí | ej. "Examen", "Quiz"; único por tenant | Admin |
| **Evaluacion** | `id` | UUID | sí | — | sistema |
| **Evaluacion** | `seccion_evaluacion_id` | UUID (FK) | sí | — | sistema |
| **Evaluacion** | `tipo_evaluacion_id` | UUID (FK) | sí | — | Profesor |
| **Evaluacion** | `nombre` | VARCHAR(100) | sí | — | Profesor |
| **Evaluacion** | `fecha` | DATE | sí | — | Profesor |
| **Evaluacion** | `puntaje_maximo` | DECIMAL(5,2) | sí | > 0 | Profesor |
| **Evaluacion** | `descripcion` | TEXT | no | — | Profesor |
| **Evaluacion** | `estado` | ENUM | sí | ACTIVA / ANULADA | Profesor |
| **Curso** | `id` | UUID | sí | — | sistema |
| **Curso** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **Curso** | `nombre` | VARCHAR(100) | sí | ej. "Primero de Primaria" | Admin |
| **Paralelo** | `id` | UUID | sí | — | sistema |
| **Paralelo** | `curso_id` | UUID (FK) | sí | — | sistema |
| **Paralelo** | `nombre` | VARCHAR(10) | sí | ej. "A" | Admin |
| **Materia** | `id` | UUID | sí | — | sistema |
| **Materia** | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **Materia** | `nombre` | VARCHAR(100) | sí | — | Admin |
| **Materia** | `curso_id` / `paralelo_id` | UUID (FK) | sí | — | Admin |
| **Materia** | `profesor_id` | UUID (FK, nullable) | no | nulo hasta asignación | Admin / Secretaria |
| **Estudiante** *(genérico)* | `id` | UUID | sí | — | sistema |
| **Estudiante** *(genérico)* | `tenant_id` | UUID (FK) | sí | RLS | sistema |
| **Estudiante** *(genérico)* | `nombre_completo` | VARCHAR(200) | sí | — | Secretaria |
| **Estudiante** *(genérico)* | `datos_personales` | JSONB | no | — | Secretaria |
| **Estudiante** *(genérico)* | `estado` | ENUM | sí | ACTIVO / INACTIVO | Secretaria |
| **Inscripcion** | `id` | UUID | sí | — | sistema |
| **Inscripcion** | `estudiante_id` | UUID (FK) | sí | — | Secretaria |
| **Inscripcion** | `gestion_escolar_id` | UUID (FK) | sí | — | Secretaria |
| **Inscripcion** | `curso_id` / `paralelo_id` | UUID (FK) | sí | — | Secretaria |
| **Inscripcion** | `fecha_inscripcion` | DATE | sí | — | Secretaria |
| **Inscripcion** | `estado` | ENUM | sí | ACTIVA / RETIRADA / TRANSFERIDA | Secretaria |

> Nota: `Estudiante` (genérico, §6.3.2) coexiste con `Estudiante` (Perfil Bolivia SIE, §6.2, identificado por RUDE) hasta que se resuelva la reconciliación de `ADR-0009` §3.

---

## 7. Prompt como Contrato Funcional ⚡🔧

### 7.1 Prompt-contrato para FSD-UC-001 (Registro de Calificaciones)

```markdown
# Role
Eres el servicio de dominio CalificacionService de EduSync (Java 25, Spring Boot 4.1.0).
Tu responsabilidad es persistir calificaciones válidas y rechazar las inválidas
con el error preciso, respetando todas las invariantes de negocio.

# Task
Procesar la solicitud de registro de una calificación: validar RBAC, periodo,
rango paramétrico y tipo de nota; persistir el registro; disparar el evento de
recalculo del centralizador provisional; escribir en audit_log.

# Context
- Entrada: {materiaId, periodoId, rude, dimension, indiceEvaluacion, tipo (REGULAR|AYUDA), valor}
- JWT en SecurityContext: {tenantId, userId, rol=DOCENTE}
- Reglas aplicables: BR-001 (RBAC), BR-002 (rangos), BR-007 (parámetros inmutables),
  RB-01 (RUDE), Constitución P2 (RUDE único)
- Parámetros del periodo: obtenidos de ParametroAcademicoRepository por {periodoId, dimension}

# Reasoning
Pasos obligatorios:
1. Extraer {tenantId, userId} del SecurityContext; verificar rol = DOCENTE.
2. Verificar que materia.tenant_id == tenantId (RLS activo en BD; verificación adicional en app).
3. Verificar que existe AsignacionDocente(docente_id=userId, materia_id=materiaId, periodo_id=periodoId).
4. Verificar Periodo.estado == ABIERTO; si CERRADO o SOLO_LECTURA → E_PERIODO_NO_MODIFICABLE.
5. Recuperar ParametroAcademico(periodo_id, dimension); si no existe → E_DIMENSION_NO_ACTIVA.
6. Validar valor ∈ [rango_min, rango_max]; si no → E_RANGO_INVALIDO.
7. Si tipo == AYUDA y no existe REGULAR previa para {rude, dimension, indiceEvaluacion} y la regla
   exige REGULAR → E_REGULAR_REQUERIDA.
8. Persistir Calificacion con timestamp UTC. Nunca calcular promedio aquí.
9. Escribir AuditLog(actor=userId, accion=CALIFICACION_REGISTRADA, entidad_id=calificacion.id).
10. Publicar CalificacionRegistradaEvent para que ConsolidacionService recalcule PROVISIONAL.
11. Retornar {calificacionId, promedioProvisional (del ConsolidacionService), estado:"PROVISIONAL"}.

# Stop condition
Detente después de paso 11. Si cualquier validación (pasos 1–7) falla, retornar
el error específico sin ejecutar los pasos siguientes. No persistir nada en caso de error.

# Output
HTTP 201 Created:
{
  "calificacionId": "uuid",
  "promedioProvisional": { "valor": integer, "estado": "PROVISIONAL" },
  "timestamp": "ISO-8601"
}

Errores:
- HTTP 403 → E_RBAC_VIOLATION (pasos 2, 3)
- HTTP 409 → E_PERIODO_NO_MODIFICABLE (paso 4)
- HTTP 422 → E_RANGO_INVALIDO, E_DIMENSION_NO_ACTIVA, E_REGULAR_REQUERIDA (pasos 5, 6, 7)
- HTTP 400 → E_RUDE_INVALIDO (formato RUDE inválido)
```

**Invariants:**
- El cálculo de promedio NUNCA ocurre en este servicio; se delega a ConsolidacionService.
- El `audit_log` se escribe en la misma transacción que el INSERT de Calificacion.
- El `valor` persistido es siempre el valor crudo (escala docente), nunca convertido a SIE.

**Failure modes:**
- `E_RBAC_VIOLATION`: actor no autorizado → 403, entrada en audit_log.
- `E_PERIODO_NO_MODIFICABLE`: estado != ABIERTO → 409.
- `E_RANGO_INVALIDO`: valor fuera del rango paramétrico → 422 con rango esperado.
- `E_RUDE_INVALIDO`: RUDE nulo o formato incorrecto → 400.

---

### 7.2 Prompt-contrato para FSD-UC-003 (Motor de Consolidación)

```markdown
# Role
Eres el ConsolidacionService de EduSync (Spring Component).
Tu responsabilidad es el único punto de cálculo de promedios en el sistema.
Ningún otro componente puede calcular promedios o aplicar floor.

# Task
Calcular el centralizador (PROVISIONAL u OFICIAL) para un curso y periodo dados,
aplicando la regla de combinación paramétrica y el criterio floor.

# Context
- Disparador: CalificacionRegistradaEvent o MateriaCerradaEvent
- Parámetros: {cursoId, periodoId, tenantId}
- Reglas: BR-003 (floor), BR-008 (dominio exclusivo del cálculo), BR-011 (anual solo con 3 cerrados)
- ParametroAcademico: {dimension, peso_max, regla_combinacion} por periodo

# Reasoning
1. Recuperar todas las Calificacion(materia.curso_id=cursoId, periodo_id=periodoId).
2. Para cada estudiante (RUDE) y dimensión activa:
   a. Agrupar evaluaciones por dimension + tipo.
   b. Aplicar regla_combinacion (PROMEDIO_SIMPLE: sum/count; SUMA: sum; MEJOR_N: top N).
   c. Escalar al peso_max de la dimensión: puntaje_dim = floor(resultado * peso_max / rango_max).
   d. Nunca usar redondeo estándar: solo floor().
3. Sumar puntaje de todas las dimensiones activas: puntaje_total.
4. Determinar estado: si alguna materia del curso está ABIERTA → PROVISIONAL; si todas CERRADAS → OFICIAL.
5. Persistir Centralizador(rude, puntaje_total, estado, curso_id, periodo_id).
6. Si las 3 materias de los 3 trimestres anuales están CERRADAS:
   promedio_anual = floor((T1 + T2 + T3) / 3)
7. Si estado == OFICIAL: publicar CentralizadorOficialEvent.

# Stop condition
Detente al persistir el Centralizador. Si alguna evaluación en alguna dimensión
activa está vacía para un estudiante, calcular con los valores disponibles pero
marcar el registro del estudiante como INCOMPLETO en el centralizador provisional.

# Output
{
  "centralizadorId": "uuid",
  "estado": "PROVISIONAL|OFICIAL",
  "promedios": [ {"rude": "...", "puntaje": integer, "aprobado": boolean} ],
  "promedioAnual": integer | null
}
```

**Invariants:**
- `floor()` es la única función de truncado permitida.
- Si `estado == PROVISIONAL`, el centralizador puede sobreescribirse en el siguiente cálculo.
- Si `estado == OFICIAL`, el centralizador es inmutable.

**Failure modes:**
- `E_PARAMETRO_FALTANTE`: no existe ParametroAcademico para una dimensión activa → log error, estado OFICIAL bloqueado.
- `E_CALCULO_INCONSISTENTE`: resultado negativo o > suma de pesos → log crítico, alerta al sistema.

---

### 7.3 Prompt-contrato para FSD-UC-005 (Modificación Retroactiva)

```markdown
# Role
Eres el CorreccionRetroactivaService de EduSync.
Gestionas el ciclo completo: solicitud → autorización → ventana → append-only → cierre.

# Task
(1) Crear SolicitudCorreccion cuando el Docente lo solicita.
(2) Procesar la decisión del Director (aprobar/rechazar).
(3) Permitir modificaciones dentro de la ventana activa con modelo append-only.
(4) Revocar automáticamente la ventana al expirar y enviar alertas.

# Context
- Solicitud: {materiaId, rude, dimension, indiceEvaluacion, justificacion}
- Autorización: {solicitudId, alcance (ESTUDIANTE_ESPECIFICO|CURSO_COMPLETO), duracionHoras(1-72)}
- Reglas: BR-005, BR-009, RB-07, RB-10 (append-only)
- Default duracion: 24 h si el Director no especifica

# Reasoning
Fase 1 — Solicitud:
1. Verificar materia.estado == SOLO_LECTURA (si ABIERTO → redirigir a endpoint normal).
2. Crear SolicitudCorreccion(estado=PENDIENTE) sin alterar Calificacion original.
3. Notificar Director in-app.

Fase 2 — Decisión del Director:
4. Si RECHAZAR: actualizar estado = RECHAZADA. Notificar Docente. FIN.
5. Si APROBAR: crear AutorizacionCorreccion(ventana_fin = now() + duracionHoras).
   Si duracionHoras no llega: aplicar default 24 h y notificar explícitamente.
6. Notificar Docente con {alcance, ventana_fin}.

Fase 3 — Modificación durante la ventana:
7. En cada POST /calificaciones del Docente: verificar AutorizacionCorreccion.estado == ACTIVA
   Y now() < ventana_fin Y rude/materia dentro del alcance autorizado.
8. Si OK: crear nueva Calificacion(registro_padre_id = id_original). El original permanece.
9. Recalcular centralizador provisional (FSD-UC-003).
10. Escribir audit_log tipo CALIFICACION_MODIFICADA.

Fase 4 — Vencimiento:
11. Scheduler verifica cada minuto las AutorizacionCorreccion activas.
12. A ventana_fin - 30 min: enviar alerta in-app al Docente.
13. A ventana_fin: actualizar estado = EXPIRADA. Escribir audit_log tipo VENTANA_EXPIRADA.

# Stop condition
Detente después de paso 13. No existe prórroga automática.

# Output
Paso 5 → {autorizacionId, alcance, ventanaFin, estado: "ACTIVA"}
Paso 13 → audit_log entry + notificación
```

**Invariants:**
- El registro original es INMUTABLE. Solo se crea una versión nueva.
- Toda autorización tiene `ventana_fin` definido. Sin excepción.
- La cadena de audit_log tiene exactamente 3 entradas: solicitud, autorización, cierre.

---

### 7.4 Métricas de prompt-contrato *(opcional)* 🔧

| Métrica | Definición operativa | Umbral | Cómo se mide |
|---------|----------------------|--------|--------------|
| **Prompt coverage** | % de FSD-UC críticos con prompt-contrato vivo y testeado | ≥ 80 % | grep en `docs/PROMPT_MAPPING.md` + revisión por pares |
| **Spec fidelity** | % de outputs que respetan Invariants y Failure modes | ≥ 90 % | Suite de golden tests contra cada contrato (JUnit 5) |
| **Hallucination rate** | % de afirmaciones del agente sin trazabilidad a FSD/PRD/BRD | ≤ 5 % | Auditoría de 30 outputs de muestra por compliance-agent |
| **Reversion rate** | % de PRs derivados de prompts revertidos antes de release | ≤ 10 % | `git log --grep="revert-prompt"` |

---

## 8. Integraciones externas 🔧

| Sistema | Tipo | Protocolo | Operaciones | SLA esperado | Autenticación | Resiliencia |
|---------|------|-----------|-------------|--------------|---------------|-------------|
| **SIE (Ministerio de Educación Bolivia)** | Síncrono REST (con reintentos asíncronos) | HTTPS | `POST /sie/calificaciones` (payload por RUDE + periodo) | No garantizado; alta tasa de fallos en horario pico | API Key en header `X-SIE-TOKEN` (parametrizable) | Idempotencia por `rude+periodo_id`; reintentos cada 5 min; timeout 30 s; circuit breaker con umbral 50 % fallos en 10 min |
| **AWS RDS PostgreSQL 15** | Síncrono JDBC | TCP/5432 | SELECT / INSERT / UPDATE / DELETE (con RLS) | 99,95 % (SLA AWS) | IAM Auth + SSL | Connection pool HikariCP; read replicas para reportería |
| **Apache PDFBox** | Librería local (in-process) | — | `PDDocument.generate(template, data)` | N/A (in-process) | — | Timeout 10 s; fallback a error HTTP 503 si excede |
| **Proveedor Notificaciones In-App** | Asíncrono HTTP | HTTPS/WebSocket | `POST /notifications` | 99 % | Bearer token | Degradación graceful: si falla, la operación principal no se bloquea; cola de reintentos |

---

## 9. Interfaces de usuario (referencia) ⚡🔧

| Pantalla / Ruta | Caso de uso cubierto | Actor |
|-----------------|----------------------|-------|
| `/login` | FSD-UC-001 (autenticación) | Todos |
| `/docente/materias` | FSD-UC-001 — listado de materias asignadas | Docente |
| `/docente/materias/{id}/calificaciones` | FSD-UC-001 — ingreso de notas por dimensión + semáforo provisional | Docente |
| `/docente/materias/{id}/cierre` | FSD-UC-002 — solicitud de cierre con verificación de completitud | Docente |
| `/docente/correcciones` | FSD-UC-005 — formulario de solicitud retroactiva + estado de ventana activa | Docente |
| `/secretaria/dashboard` | FSD-UC-010 — avance de carga docente por curso (semáforo verde/amarillo/rojo) | Secretaría |
| `/secretaria/exportacion-sie` | FSD-UC-004 — botón de exportación one-click + reporte de resultado | Secretaría |
| `/secretaria/nominas` | FSD-UC-006 — alta/baja/transferencia de estudiantes | Secretaría |
| `/secretaria/boletines` | FSD-UC-007 — generación de boletines PDF por curso | Secretaría |
| `/director/gestion-academica` | FSD-UC-009 — creación de gestión, parámetros, asignaciones, apertura/cierre | Director |
| `/director/autorizaciones` | FSD-UC-005 — bandeja de solicitudes del Docente + formulario de autorización | Director |
| `/director/indicadores` | FSD-UC-010 — dashboard con vistas trimestral y anual diferenciadas | Director |
| `/director/centralizadores` | FSD-UC-003 — visualización del centralizador por curso (PROVISIONAL / OFICIAL) | Director |

### 9.1 Trazabilidad con M2 (UI/UX) ⚡🔧

| Wireframe / Artefacto M2 | Pantalla FSD | Caso de uso (FSD-UC) | Estado de traza |
|--------------------------|--------------|----------------------|-----------------|
| Sistema de diseño Atomic Design + Design Tokens | Todos los componentes Angular | Todos | ✅ Cubierto |
| Semáforos visuales de reprobación (rojo/amarillo/verde) | `/docente/materias/{id}/calificaciones` | FSD-UC-001 | ✅ Cubierto |
| Flujo de carga de notas — wireframe Docente | `/docente/materias/{id}/calificaciones` | FSD-UC-001 | ✅ Cubierto |
| WCAG 2.2 AA — contraste y etiquetas ARIA | Todas las pantallas | — | ✅ Cubierto en NFR-011 |
| Dashboard Director — wireframe indicadores | `/director/indicadores` | FSD-UC-010 | ⚠️ Parcial: separación trimestral/anual requiere refinamiento |
| Flujo exportación SIE one-click | `/secretaria/exportacion-sie` | FSD-UC-004 | ✅ Cubierto |
| Formulario de solicitud de corrección | `/docente/correcciones` | FSD-UC-005 | ✅ Cubierto |

---

## 10. Requerimientos No Funcionales (NFR) ⚡🔧

| ID | Categoría | Requisito | Métrica | Umbral | Cómo se verifica |
|----|-----------|-----------|---------|--------|------------------|
| NFR-001 | **Rendimiento** | Latencia POST /calificaciones | p95 | < 500 ms | k6 load test con 100 docentes concurrentes |
| NFR-002 | **Rendimiento** | Cálculo del centralizador por curso (50 estudiantes, 10 materias) | tiempo de procesamiento | < 3 s | JUnit integration test con datos reales |
| NFR-003 | **Rendimiento** | Generación de boletín PDF | tiempo por documento | < 5 s | test de performance con PDFBox |
| NFR-004 | **Rendimiento** | Tiempo de respuesta del dashboard Director (indicadores) | p95 | < 1 s | k6 con 50 directores concurrentes |
| NFR-005 | **Disponibilidad** | Uptime general del sistema | SLA mensual | ≥ 99,5 % | AWS CloudWatch + alertas PagerDuty |
| NFR-006 | **Disponibilidad** | Uptime en ventana crítica de cierre (72 h pre-plazo SIE) | SLA en ventana | ≥ 99,9 % | AWS CloudWatch en período configurado |
| NFR-007 | **Seguridad** | Ningún PII (RUDE, nombre, nota) en logs de aplicación | 0 ocurrencias en logs | 0 | Auditoría automatizada de logs con grep en CI |
| NFR-008 | **Seguridad** | Autenticación JWT con expiración de sesión | tiempo de sesión inactiva | 8 horas | Test de expiración de token |
| NFR-009 | **Seguridad** | Cifrado en tránsito | protocolo | HTTPS/TLS 1.3 | SSL Labs scan ≥ A |
| NFR-010 | **Seguridad** | Aislamiento multitenant — ninguna query sin `tenant_id` | 0 queries sin filtro RLS | 0 | multitenant-audit-agent en CI: grep de queries en tests |
| NFR-011 | **Usabilidad** | Flujo crítico completable en ≤ 3 pasos sin capacitación | prueba de usabilidad | ≥ 4/5 usuarios sin ayuda | Sesión de prueba con 5 docentes reales |
| NFR-012 | **Accesibilidad** | Contraste y etiquetas ARIA | WCAG 2.2 AA | nivel AA | Lighthouse CI en pipeline |
| NFR-013 | **Mantenibilidad** | Actualización formato SIE sin redespliegue | tiempo de respuesta a cambio ministerial | < 30 min | Test de configuración en BD sin deploy |
| NFR-014 | **Escalabilidad** | Incorporación de nuevo tenant (colegio) | tiempo de setup | < 5 min de configuración | Test de onboarding automatizado |
| NFR-015 | **Auditabilidad** | Cobertura del audit_log sobre operaciones de escritura | % operaciones cubiertas | 100 % | Revisión de cobertura con PR-AUD-001 |
| NFR-016 | **Cumplimiento** | Inmutabilidad del audit_log (sin UPDATE ni DELETE) | 0 modificaciones | 0 | DB constraint + test de intento de UPDATE → FAIL |

---

## 11. Trazabilidad MRD → PRD → FSD ⚡🔧

| MRD (necesidad) | PRD (requerimiento) | FSD (caso de uso) | NFR | Prueba de aceptación |
|-----------------|---------------------|-------------------|-----|----------------------|
| MRD-N-01 | PRD-REQ-012 | FSD-UC-004 | NFR-001, NFR-006 | Piloto real: exportación < 10 min, 0 errores |
| MRD-N-02 | PRD-REQ-019 | FSD-UC-006 | NFR-010 | Alta/baja con RUDE: no desfase de posiciones |
| MRD-N-03 | PRD-REQ-001, PRD-REQ-007 | FSD-UC-001, FSD-UC-009 | NFR-011 | Prueba usabilidad: 4/5 sin ayuda |
| MRD-N-04 | PRD-REQ-008, PRD-REQ-020 | FSD-UC-003, FSD-UC-010 | NFR-004 | Dashboard actualiza en < 5 s |
| MRD-N-05 | PRD-REQ-014, PRD-REQ-018 | FSD-UC-005 | NFR-015, NFR-016 | 100 % correcciones con audit_log |
| MRD-N-06 | PRD-REQ-016 | FSD-UC-007 | NFR-003 | PDF generado en < 5 s desde centralizador CERRADO |
| MRD-N-07 | PRD-REQ-001 | FSD-UC-001 | NFR-014 | Nuevo tenant en < 5 min |
| MRD-N-08 | PRD-REQ-013 | FSD-UC-004 | NFR-005 | Fallo parcial SIE: reanudación sin duplicados |
| MRD-N-09 | PRD-REQ-003 | FSD-UC-009 | NFR-013 | Cambio formato SIE: aplicado en < 30 min vía BD |
| MRD-N-10 | PRD-REQ-001 | FSD-UC-001 | NFR-010 | 0 queries cross-tenant en suite de tests |

---

## 12. Plan de pruebas funcionales 🔧

**Estrategia:**
- **Unitarias (JUnit 5 + Mockito):** Cobertura ≥ 80 % del dominio core (`domain/`). Foco en: motor de consolidación con `floor`, validaciones paramétricas, lógica de ventana temporal, generación de audit_log.
- **Integración (Spring Boot Test + Testcontainers PostgreSQL):** Cada FSD-UC con al menos 3 escenarios: flujo exitoso, error de validación, error de RBAC. Verificación de RLS multitenant.
- **End-to-End (Playwright):** Flujos críticos: registro de notas → cierre → exportación SIE (con mock del servidor SIE). Flujo de corrección retroactiva con timer simulado.
- **Contract Testing (prompt-contratos):** Golden tests para cada prompt-contrato del §7. Se ejecutan en CI contra los modelos de IA con muestras de 30 inputs reales.
- **Performance (k6):** 100 docentes concurrentes en carga de notas. 50 directores en dashboard. Exportación de 80 estudiantes al SIE.
- **Seguridad:** OWASP ZAP scan en endpoints públicos. Auditoría de logs (grep PII). SSL Labs scan.

**Herramientas:**
- Backend: JUnit 5, Mockito, Testcontainers, RestAssured.
- Frontend: Jest, Angular Testing Library, Playwright.
- Performance: k6.
- Seguridad: OWASP ZAP, SSL Labs.

**Cobertura mínima aceptada:** 80 % en `domain/`; 60 % en `application/`; E2E para los 5 FSD-UC críticos.

**Golden tests críticos (cero tolerancia):**
- `floor(64.666) == 64` — nunca 65.
- Exportación por RUDE: ningún payload con nombre o posición de lista.
- Ventana expirada: HTTP 403 en 100 % de intentos post-expiración.
- Cross-tenant: 0 registros visibles de otro tenant en cualquier endpoint.

---

## 13. Riesgos funcionales ⚡🔧

| Riesgo | Probabilidad | Impacto | Mitigación | Responsable |
|--------|--------------|---------|------------|-------------|
| RF-01: El servidor SIE falla durante el piloto real de cierre trimestral | Alta | Crítico | Motor de reintentos idempotentes (DA-05). UI muestra "Sistema seguro, reintentando". Demo interna antes del piloto. | Dev Lead |
| RF-02: Cambio en el formato SIE sin previo aviso del Ministerio | Media | Alto | Formato SIE almacenado en `parametro_sie` (BD sin redespliegue). Monitoreo activo de comunicados ministeriales. | PM + Dev Lead |
| RF-03: Docente cierra materia con evaluaciones incompletas por error de UI | Media | Medio | Verificación de completitud es back-end obligatoria (no solo UI). El sistema lista los faltantes antes de permitir el cierre. | Dev Lead + QA |
| RF-04: Alucinación de invariantes regulatorias por el dev-agent en código de exportación SIE | Media | Crítico | `compliance-agent` valida antes de cada merge. Golden tests de floor y RUDE antes de deploy. Revisión humana obligatoria. | Dev Lead |
| RF-05: Ventana de corrección no se revoca automáticamente por fallo del Scheduler | Baja | Alto | Scheduler con retry en fallo. Monitor de ventanas activas > 73h como alerta de anomalía. Test de Scheduler en CI. | Dev Lead |
| RF-06: Error de configuración RLS permite acceso cross-tenant | Baja | Crítico | `multitenant-audit-agent` en CI: rechaza cualquier migración de tabla sin política RLS y `tenant_id`. 0 excepciones. | Dev Lead |
| RF-07: El motor de consolidación calcula indicadores anuales con solo 2 trimestres cerrados | Baja | Alto | Test específico en golden tests: 3 escenarios con 1/2/3 trimestres cerrados. `BR-011` como invariante de prompt-contrato. | QA |

---

## 14. Glosario 🔧

| Término | Definición |
|---------|------------|
| **RUDE** | Registro Único de Estudiante. Identificador único emitido por el Ministerio de Educación de Bolivia. Es la única clave de identidad estudiantil en EduSync; nunca se usa nombre, apellido ni posición de lista. |
| **SIE** | Sistema de Información Educativa. Plataforma estatal del Ministerio de Educación Bolivia donde se reportan obligatoriamente las calificaciones trimestrales de todos los estudiantes. |
| **Triple digitación** | Proceso manual que genera el problema central de EduSync: (1) docente en Excel, (2) secretaría consolida en Excel, (3) secretaría transcribe al SIE nota por nota. |
| **Tenant** | Unidad educativa (colegio) que opera como cliente B2B en la plataforma. Sus datos están aislados de otros tenants mediante RLS en PostgreSQL. |
| **RLS** | Row-Level Security. Mecanismo de PostgreSQL que filtra automáticamente las filas visibles según políticas definidas por rol, usado en EduSync para garantizar el aislamiento multitenant. |
| **Floor** | Función matemática de piso (`⌊x⌋`). Criterio de truncado de decimales aplicado por el motor de consolidación. `floor(64.666) = 64`, no 65. Garantiza consistencia con la escala del SIE. |
| **PROVISIONAL** | Estado del centralizador mientras existen materias en estado ABIERTO en el curso y periodo. Los valores se calculan y muestran en tiempo real pero no tienen validez oficial y no pueden generar boletines. |
| **OFICIAL** | Estado del centralizador cuando el 100 % de las materias del curso y periodo están CERRADAS. El valor es inmutable y habilita la generación de boletines y la exportación al SIE. |
| **Append-only** | Modelo de persistencia en el que las modificaciones retroactivas no sobreescriben el registro original sino que crean un nuevo registro versionado con referencia al anterior (`registro_padre_id`). |
| **Audit Log** | Tabla `audit_log` inalterable que registra toda operación de escritura del sistema: actor, acción, entidad afectada, valor anterior, valor nuevo y timestamp UTC. Sin UPDATE ni DELETE. |
| **Ventana temporal** | Período de tiempo con fecha de inicio y fin dentro del cual el Docente tiene permiso de escritura retroactiva autorizado por el Director. Al vencer, el sistema revoca el permiso automáticamente. |
| **Dimension** | Componente de la calificación boliviana según la Ley 070: Ser, Saber, Hacer, Decidir (y opcionalmente Autoevaluación). Cada dimensión tiene un peso máximo en puntos configurado por periodo. |
| **RBAC** | Role-Based Access Control. Sistema de control de acceso que restringe las operaciones según el rol del usuario autenticado (DIRECTOR / SECRETARÍA / DOCENTE) y su `tenant_id`. |
| **Motor de Consolidación** | Componente de dominio de EduSync responsable exclusivo del cálculo de promedios, aplicación de `floor` y generación del centralizador. Ningún otro componente puede realizar estos cálculos. |
| **Gestión Académica** | Unidad organizativa de un año escolar completo en EduSync, compuesta por 3 periodos trimestrales y sus respectivos parámetros, asignaciones y nóminas. **(Perfil Bolivia SIE.)** |
| **SysAdmin** *(nuevo, `ADR-0009`, refinado por `ADR-0010`)* | Rol de plataforma (SaaS) que administra `Tenant`s (Unidades Educativas), su ciclo de suscripción y sus usuarios `ADMIN`. No pertenece a ningún tenant y no accede a sus datos académicos; su `tenant_id` es nulo de forma **permanente** y nunca se combina con un rol de tenant en el mismo usuario (`ADR-0010`). |
| **UsuarioRol** *(nuevo, `ADR-0010`)* | Relación N:M entre `Usuario` y `Rol` que permite que un mismo usuario tenga uno o más roles simultáneos. Reemplaza al atributo `Usuario.rol` de valor único definido originalmente en `ADR-0009`. |
| **Tenant** *(genérico, `ADR-0009`)* | Unidad Educativa con ciclo de vida propio a nivel plataforma: fecha de inicio/vencimiento de suscripción y estado (`ACTIVO`/`SUSPENDIDO`/`VENCIDO`). Extiende, sin reemplazar, el `tenant_id` de RLS (`ADR-0001`). |
| **Gestión Escolar** *(genérico, `ADR-0009`)* | Unidad organizativa de un ciclo escolar en el modelo generalizado, con un número **configurable** de `PeriodoEvaluacion` (no fijo en 3). |
| **Sección de Evaluación** *(genérico, `ADR-0009`)* | Componente configurable de la calificación de un periodo (ej. Ser/Saber/Hacer/Autoevaluación u otro esquema), con nombre, orden, nota máxima, peso porcentual y cantidad máxima de evaluaciones definidos por la institución. |
| **Tipo de Evaluación** *(genérico, `ADR-0009`)* | Categoría configurable de una `Evaluacion` (ej. Examen, Práctica, Quiz), definida en un catálogo propio de cada tenant, no codificada de forma fija en el dominio. |
| **Paralelo** *(genérico, `ADR-0009`)* | Subdivisión de un `Curso` (ej. "A", "B", "C"). Un curso puede tener uno o más paralelos. |
| **Inscripción** *(genérico, `ADR-0009`)* | Vínculo entre un `Estudiante` y una `GestionEscolar`/`Curso`/`Paralelo` concretos, con fecha y estado. El historial académico de un estudiante se reconstruye a través de todas sus inscripciones. |
| **Asesor** *(nuevo, `ADR-0009`)* | Rol de tenant con acceso de solo lectura al avance académico de un `Curso`/`Paralelo` asignado; sin permisos de escritura sobre calificaciones ni nóminas. |

---

## 15. Registro de cambios ⚡🔧

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v1.0 | 15/05/2026 | Equipo G-EduSync — Rodrigo Aspeti | Creación inicial del FSD en modo FSD Clásico. Basado en BRD v2.0, MRD v1.0, PRD v1.0, arquitectura_funcional_EduSync.md (10 UCs, 5 DAs) y diagramas de estado del Docente (18 estados) y Director (23 estados). 5 FSD-UC con flujos completos (UC-001, UC-003, UC-004, UC-005, UC-009), 12 reglas de negocio, modelo ER con 16 entidades, diccionario de datos completo, 3 prompt-contratos con invariantes y failure modes, 16 NFRs con umbrales y verificación, trazabilidad MRD→PRD→FSD, plan de pruebas y glosario. |
| v2.0 | 12/07/2026 | Rodrigo Aspeti | Generalización a plataforma SaaS multi-tenant configurable (`ADR-0009`), alineada con BRD v3.0 y PRD v2.0. §3 se amplía con actores `SYSADMIN`/`ADMIN`/`SECRETARIA`/`ASESOR`/`PROFESOR` (§3.1), manteniendo la tabla original del Perfil Bolivia SIE sin cambios. Se añade §4.6 con 11 nuevos casos de uso (`FSD-UC-011`..`FSD-UC-021`): Tenants/Suscripciones, Gestión Escolar, Periodos y Secciones de Evaluación configurables, Evaluaciones/Tipos configurables, Cálculo de Notas configurable, Cursos/Paralelos, Materias, Profesores, Estudiantes/Inscripciones, Usuarios y Roles. Se añaden BR-013..BR-024 (§5.1) y el modelo de datos genérico (§6.3, 13 entidades nuevas) con nota explícita de convivencia con el modelo Bolivia SIE (§6.1/§6.2). Nueva nota de nomenclatura de roles (§0.1). Nuevos términos de glosario. 5 puntos quedan explícitamente pendientes de definición en todo el documento (ver `ADR-0009` §3): no implementar en código sin resolverlos primero. |
| v2.1 | 12/07/2026 | Rodrigo Aspeti | Corrección de consistencia de stack: §1 (resumen ejecutivo), §2.4 (tabla "Stack tecnológico") y §7.1 (prompt-contrato de `CalificacionService`) citaban todavía el stack del baseline de M4 (Java 21 / Spring Boot 3.3 / Angular 17) en vez del stack vivo fijado por `ADR-0008` (Java 25 LTS / Spring Boot 4.1.0 / Angular 21 LTS) desde la apertura de `release/3.0.0`. Actualizados los 3 puntos con referencia explícita a `ADR-0008`, sin afectar `docs/baseline/DTI.md` (que documenta correctamente el stack histórico de M4 sin cambios). |
| v2.2 | 14/07/2026 | Rodrigo Aspeti | Refinamiento del modelo de roles (`ADR-0010`): `BR-024` (§5.1) pasa de "exactamente un rol" a **multi-rol** vía nueva entidad `UsuarioRol` (N:M), con la invariante permanente `tenant_id IS NULL ⟺ roles = {SYSADMIN}`. Actualizados §3.1 (nota en actor `SYSADMIN`), §6.3.1 (diagrama ER con `USUARIO_ROL`), §6.3.2 (diccionario de datos: `Usuario.rol` reemplazado por `UsuarioRol`), §4.6.1 (`FSD-UC-011`, nota no bloqueante sobre el *bootstrap* del primer SysAdmin y el tenant demo pendiente de diseño) y §4.6.11 (`FSD-UC-021`, endpoint `roles: [...]` y nuevos escenarios Gherkin). Nuevos términos de glosario (§14). Sin cambios en `ADR-0009` ni en el resto de BR-013..BR-023. |
| v2.3 | 14/07/2026 | Rodrigo Aspeti | `FSD-UC-011` (§4.6.1) implementado en `docs/design/DD-UC-003.md` (módulo `plataforma`: alta y gestión de Tenants, scheduler de vencimiento, `TenantConsultaPort`, enforcement de `BR-014`). Corrección de referencia: la nota `ADR-0010` sobre el tenant "demo" ya no cita el ID inexistente `DD-UC-011`; ahora referencia explícitamente `docs/design/DD-UC-003.md` para el bootstrap del `SYSADMIN`/alta de tenant ya implementada, y deja claro que el diseño del tenant demo queda diferido a un Design Doc de seguimiento aún sin crear (distinto de `DD-UC-003`). Sin cambios de requisito, solo corrección de trazabilidad. |
| v2.4 | 19/07/2026 | Rodrigo Aspeti | `FSD-UC-011` (§4.6.1): se añade al flujo principal el paso de lectura `GET /api/v1/plataforma/tenants` (lista para consola SysAdmin, `DD-UC-004` / `PR-IMPL-004`). Sin cambio de reglas de negocio; solo documenta el endpoint de consulta necesario para la UI. |
| v2.5 | 04/08/2026 | Rodrigo Aspeti | `FSD-UC-021` (§4.6.11) implementado (backend) en `docs/design/DD-UC-005.md`/`PR-IMPL-005`: se añade al flujo principal el paso de lectura `GET /api/v1/usuarios` (mismo precedente que `GET /tenants` en v2.4); el flujo alternativo **A1** (`E_ASESOR_SIN_CURSO`) se marca explícitamente **diferido** — el rol `ASESOR` ya es asignable, pero la validación de la referencia a `Curso`/`Paralelo` depende del módulo `academico`, bloqueado por `ADR-0009` §3. Sin cambio de reglas de negocio; solo documenta el endpoint de consulta y el alcance real de A1. |
| v2.6 | 21/08/2026 | Rodrigo Aspeti | `FSD-UC-017` (§4.6.7) cierra implementación **completa** (backend + UI): se documentan los pasos de lectura `GET /api/v1/cursos` (filtro `q` + paginación) y `GET /api/v1/cursos/{id}/paralelos` (lista simple), la excepción **A1** `404 E_CURSO_NO_ENCONTRADO`, y la trazabilidad a `DD-UC-010`/`PR-IMPL-010` (backend, 20/08/2026) y `DD-UC-011`/`PR-IMPL-011` (UI, 21/08/2026). Sin cambio de reglas de negocio (`BR-021` vigente); `PATCH`/`DELETE` de `Curso`/`Paralelo` permanecen fuera de este slice. Propagado vía `sync-doc-chain` a `docs/product/DTP.md` y `docs/PROMPT_MAPPING.md`. |
| v2.7 | 21/08/2026 | Rodrigo Aspeti | `FSD-UC-018` (§4.6.8) cierra implementación **completa** (backend + UI fullstack, `DD-UC-012`/`PR-IMPL-012`): se documentan `GET /materias` (filtro `q` + paginación), `GET /materias/{id}`, `GET /materias/profesores-disponibles`, `GET` de asignaciones, A2/A3 404, y la nota de RBAC `SECRETARIA` en los GET de Cursos. Sin cambio de `BR-022`; `PATCH`/`DELETE` y `FSD-UC-019` permanecen fuera. |
| v2.8 | 21/08/2026 | Rodrigo Aspeti | `FSD-UC-020` (§4.6.10) cierra implementación **completa** (backend + UI fullstack, `DD-UC-013`/`PR-IMPL-013`): se documentan `rude` obligatorio en el POST, `GET /estudiantes` (`q`/`estado` + paginación), `GET /estudiantes/{id}`, historial `GET .../inscripciones`, A2 404 y A3 `409 E_RUDE_DUPLICADO`. Sin cambio de `BR-023`; incluir `rude` aplica `BR-004` vigente (no es la reconciliación de `ADR-0009` §3 punto 1). `PATCH`/`DELETE` permanecen fuera. |

---

## Checklist de entrega — modo FSD Clásico 🔧

- [x] §0 Metadatos completos, modo declarado como FSD Clásico 🔧.
- [x] §1 Resumen ejecutivo (150–250 palabras).
- [x] §2 Alcance y fuera de alcance + §2.4 Plan técnico detallado + §2.5 Tasks (14 tasks).
- [x] §3 Actores y permisos (7 actores: 3 humanos + 4 sistemas/agentes).
- [x] ≥ 3 casos de uso críticos (5 FSD-UC con flujos principal, alternativos, excepciones, datos y Gherkin).
- [x] §5 Reglas de negocio con tipo y origen (12 reglas BR-001..BR-012).
- [x] §6 Modelo de datos completo (diagrama ER Mermaid + diccionario de 16 entidades).
- [x] Un prompt-contrato por caso de uso crítico (3 contratos para UC-001, UC-003, UC-005 + métricas).
- [x] §8 Integraciones externas con SLA y autenticación (SIE, AWS RDS, PDFBox, Notificaciones).
- [x] §9 + §9.1 Trazabilidad con M2 (13 pantallas mapeadas a FSD-UC).
- [x] §10 NFRs con métrica, umbral y forma de verificación (16 NFRs).
- [x] §11 Matriz de trazabilidad MRD → PRD → FSD → NFR → prueba (10 filas).
- [x] §12 Plan de pruebas detallado (estrategia + herramientas + cobertura + golden tests).
- [x] §13 Riesgos funcionales (7 riesgos con mitigación).
- [x] §14 Glosario (15 términos de dominio).
- [x] §15 Registro de cambios.
- [ ] Revisión por pares (otro grupo) — pendiente de asignación.
