---
name: distributed-architecture-reviewer-edusync
description: >
  Audita la sección §6 del DTI de EduSync (Arquitectura Distribuida) y los
  artefactos derivados (seams §6.2, tabla de resiliencia, catálogo de eventos,
  ADR-0001..0016, diagramas C4) contra un checklist de 30 criterios derivado
  de las mejores prácticas de microservicios y DDD. Aplica la regla
  "monolito modular v1.x vs microservicios v2.0 (ADR-0007)": criterios que
  asumen N microservicios desplegados independientemente se marcan
  "no auditable" cuando no aplican al monolito. Produce reporte con score
  0-100, hallazgos críticos must-fix antes de `release/2.0.0`, hallazgos
  menores, criterios no auditables, fortalezas y conflictos de coherencia
  DTI↔ADR↔AGENTS↔FSD. Skill de verificación: NO crea ni edita artefactos.
allowed-tools:
  - read
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: distributed-architecture-reviewer-edusync — Auditor de DTI §6 de EduSync

> Skill canónica del proyecto EduSync. Copiar a `.claude/skills/distributed-architecture-reviewer-edusync/`
> y a `.cursor/skills/distributed-architecture-reviewer-edusync/` para activarla en Claude Code,
> Claude Desktop y Cursor respectivamente.

---

## 1. Cuándo activarlo (triggers)

- **DURANTE**: cierre de Módulo 4 / pre-defensa final, revisión de `docs/DTI.md` antes de merge a `release/*`, autoevaluación previa a la entrega grupal, code review arquitectónico, validación pre-Strangler Fig (extracción de `edusync-sie-exporter` o `edusync-consolidacion` en v2.0).
- **ARRANCA cuando** el usuario dice:
  - `"@distributed-architecture-reviewer-edusync"`
  - "audita mi arquitectura distribuida" / "audita el §6 del DTI" / "audita los seams"
  - "score de calidad arquitectónica antes del release"
  - "validar Strangler Fig" / "validar resiliencia SIE"
- **NO ACTIVAR cuando**:
  - `docs/DTI.md §6` aún no está poblado (usar primero `dti-edusync`).
  - No existen los ADRs base (correr antes `adr-edusync` para ADR-0001..0016).
  - El usuario quiere generar artefactos (este skill es solo lectura/auditoría).

---

## 2. Entradas obligatorias

El usuario DEBE garantizar acceso de lectura a los siguientes archivos. Si falta alguno crítico,
responder: `"E_MISSING_INPUT: Necesito al menos <archivo> para auditar. Ausentes: <lista>."`

| # | Tema | Ruta canónica EduSync | Crítico |
|---|------|-----------------------|---------|
| 1 | DTI §6 (Arq. Distribuida) y §6.2 (Seams T1.8) | `docs/DTI.md` | Sí |
| 2 | Arquitectura funcional (DA-01..DA-05) | `docs/arquitectura_funcional_EduSync.md` | Sí |
| 3 | Arquitectura hexagonal (Aggregates, ports, adapters) | `docs/arquitectura_hexagonal_EduSync.md` | Sí |
| 4 | DTOs hexagonales | `docs/dtos_EduSync.md` | No |
| 5 | FSD (UC-001..UC-010, BR-001..BR-012, NFR) | `docs/fsd/FSD_EduSync.md` | Sí |
| 6 | LFSD (DDL, paquetes, contratos) | `docs/LFSD-EduSync.md` | No |
| 7 | AGENTS (stack autoritativo, guardrails) | `AGENTS.md` | Sí |
| 8 | PROMPT_MAPPING | `docs/PROMPT_MAPPING.md` | No |
| 9 | ADRs vigentes | `docs/adr/0001..0016-*.md` | Sí |
| 10 | C4 nivel 1 y 2 | `docs/diagrams/c4_level1.mmd`, `docs/diagrams/c4_level2.mmd` | Sí |
| 11 | Estados de UC críticos | `docs/diagrams/estados_*.mmd` | No |
| 12 | AI-SDLC | `docs/diagrams/ai-sdlc.mmd` | No |

Si falta `docs/diagrams/services_map.mmd` o un diagrama explícito de Aggregates, NO inventar:
marcar los criterios afectados como **no auditable** citando la ausencia.

---

## 3. Fuentes de verdad (orden de precedencia)

1. ADRs vigentes en `docs/adr/0001..0016-*.md` (estado `Aceptada` > `Propuesta`).
2. NFR-001..NFR-014 del FSD y constitution del PRD.
3. `AGENTS.md` (stack autoritativo, guardrails, agentes).
4. `docs/DTI.md` (especialmente §6.1 resiliencia, §6.2 seams, §7 eventos, §13 STRIDE).
5. Diagramas C4 y de estados; en caso de conflicto entre diagrama y ADR, ganan los ADRs.

> **Conflicto DTI ↔ ADR**: reportar siempre como **hallazgo crítico de coherencia**; no asumir
> cuál tiene razón.

---

## 4. Procedimiento

### Regla de contexto: monolito modular v1.x

EduSync **no es microservicios** en v1.x. Aplicar esta regla a cada criterio del checklist:

- Si el criterio sigue aplicando al monolito modular (módulo = bounded context), **evaluar la
  versión modular**.
- Si el criterio solo aplica a microservicios desplegados independientemente, marcar
  **no auditable** y citar `docs/adr/0007-estrategia-migracion-microservicios-strangler-fig.md`
  como justificación.
- Si la documentación describe la decisión futura (v2.0) pero el criterio audita el estado
  actual, separar explícitamente "estado actual" vs "estado objetivo".

### Paso 1 — Leer los archivos de §2

Si falta alguno crítico → STOP con `E_MISSING_INPUT`.

### Paso 2 — Recorrer el checklist (30 criterios, 3.33 pts c/u)

| # | Bloque | Criterio | Referencia EduSync |
|---|--------|----------|---------------------|
| 1 | A. Descomposición | Cada BC mapea a 1 responsabilidad clara | DTI §4.1 (5 BCs: calificaciones, periodos, consolidacion, exportacion, auditoria) |
| 2 | A | Datos propios o compartición controlada por RLS | DA-01 / ADR-0001 |
| 3 | A | Cero `JOIN` cross-módulo no documentado | LFSD §6 (DDL por BC); en monolito = cero acceso a tabla de otro BC fuera del puerto |
| 4 | A | Cada módulo tiene dueño asignado | DTI §4.1 columna Owner / AGENTS §0.1 |
| 5 | A | ADR-0007 evalúa ≥ 3 opciones | ADR-0007 §2 |
| 6 | A | ADR-0007 usa árbol T1.8 (equipos, tráfico, aislamiento, costo) | DTI §6.2 + ADR-0007 §1 |
| 7 | B. Resiliencia | Tabla cubre 3 puntos críticos: registro, consolidación, exportación SIE | DTI §6.1 |
| 8 | B | ≥ 4 parámetros numéricos por fila (timeout, retry, backoff, `failureRate`, `slidingWindow`) | ADR-0005 §5 |
| 9 | B | Retry NUNCA en 4xx (solo 5xx/timeouts/IOException) | ADR-0005 §3 + `application.yml` |
| 10 | B | Métrica observable por fila (CloudWatch metric name) | DTI §14 |
| 11 | B | Dimensión CAP sacrificada (CP vs AP) declarada por fila | DTI §6.1 |
| 12 | B | Fallback explícito (cero "500 al usuario" sin alternativa) | DTI §6.1 + ADR-0005 §4 |
| 13 | C. IPC | Mapa de IPC por flujo (sync REST / Spring Events / SQS v2.0 / scheduler) | DTI §6.1 + §7.1 |
| 14 | C | Cada flujo justifica ≥ 2 dimensiones (latencia, ordering, fan-out, acoplamiento, replay) | ADR-0004 §2 |
| 15 | C | Cero cadena sync > 3 saltos sin Circuit Breaker | DTI §6.1 (Resilience4j SIE) |
| 16 | C | Cada flujo sync declara timeout HTTP, timeout JDBC y retry | ADR-0005 §5 |
| 17 | C | Cada flujo async declara topic/cola, garantía, particionamiento, DLQ | DTI §7.1 + ADR-0004 |
| 18 | D. API externa | ADR-0014 evalúa ≥ 3 opciones (path/header/sin versionado) × ≥ 3 dimensiones | ADR-0014 §2 |
| 19 | D | Cada endpoint público declara auth (JWT/RBAC) y rate limit | ADR-0008 + ADR-0016 |
| 20 | D | Cero gRPC expuesto al browser/cliente público (todo REST `/api/v1/*`) | ADR-0014 §3 |
| 21 | D | Query cross-módulo: API Composition (timeout/fallback) o CQRS | En monolito puede ser "no auditable hasta v2.0" |
| 22 | E. DDD | Aggregates con Root + entities + VOs identificados | `arquitectura_hexagonal_EduSync.md §5` (8 Roots) |
| 23 | E | Cero setters públicos en Roots; mutación vía verbo de negocio | `registrarCalificacion`, `oficializar`, `cerrarMateria` |
| 24 | E | Cada método del Root valida ≥ 1 invariante (BR-001..BR-012, RUDE, ventana 1–72 h, `Math.floor()` BR-008) | `arquitectura_hexagonal_EduSync.md §5` |
| 25 | E | Cada cambio relevante emite Domain Event en pasado (`CalificacionRegistradaEvent`, `MateriaCerradaEvent`, `CentralizadorOficialEvent`, `VentanaExpiradaEvent`) | DTI §7.1 |
| 26 | E | 3 reglas Aggregate: ref. solo al Root / 1 TX = 1 Aggregate / consistencia eventual via eventos | DA-04 / ADR-0004 |
| 27 | F. Anti-patrones | **Distributed Monolith** no detectado (monolito intencional, no distributed) | Estado v1.x |
| 28 | F | **God microservice/módulo** no detectado (ningún módulo absorbe 3+ BCs) | DTI §4.1 |
| 29 | F | **Anemic Domain Model** no detectado (Roots con lógica, no solo getters + ServiceClass) | `arquitectura_hexagonal_EduSync.md §5` |
| 30 | F | **Dual-write directo** no detectado: `audit_log` en misma TX (DA-03/ADR-0003) + `@TransactionalEventListener(AFTER_COMMIT)` (DA-04/ADR-0004); v2.0 SQS exigirá Outbox | DA-03 + DA-04 |

### Paso 3 — Calcular score

- `score = aciertos × 3.33`, redondeado al entero más cercano.
- Criterios "no auditable" no suman ni restan; reportar aparte.
- Reportar también `score normalizado = aciertos / (30 − no_auditables) × 100`.

### Paso 4 — Citar evidencia

Cada hallazgo debe citar archivo + sección/línea aproximada:
`docs/DTI.md §6.2`, `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md §3`,
`docs/arquitectura_hexagonal_EduSync.md §5`, etc. No inventar.

### Paso 5 — Separar hallazgos

- **Críticos**: must-fix antes de `release/2.0.0` (bloquean defensa final).
- **Menores**: recomendados, no bloquean release.
- **No auditables**: explicar qué documento faltaría para auditarlos.
- **Fortalezas**: aciertos relevantes documentables en la defensa.
- **Conflictos de coherencia**: DTI ↔ ADR ↔ AGENTS ↔ FSD/LFSD.

---

## 5. Salida esperada

Un único reporte Markdown en chat con esta estructura exacta:

```markdown
# Auditoría arquitectura distribuida — EduSync — <fecha>

**Score global**: NN / 100
**Score normalizado**: NN / (30 − no_auditables) → NN %
**Estado**: monolito modular hexagonal v1.x con seams identificados para v2.0 (ADR-0007).

| Bloque | Aciertos | Total | % | No auditable |
|--------|----------|-------|---|--------------|
| A. Descomposición | x | 6 |  |  |
| B. Resiliencia | x | 6 |  |  |
| C. IPC | x | 5 |  |  |
| D. API externa | x | 4 |  |  |
| E. DDD | x | 5 |  |  |
| F. Anti-patrones | x | 4 |  |  |

## Hallazgos críticos (must-fix antes de release/2.0.0)
- **<código>**: descripción concreta. Evidencia: `docs/<ruta>:<sección>`. Acción: <qué hacer>.

## Hallazgos menores (recomendados)
- **<código>**: descripción. Evidencia. Acción.

## Criterios no auditables
- **<código>**: por qué no se puede auditar y qué documento faltaría.

## Fortalezas detectadas
- ...

## Conflictos de coherencia
- DTI vs ADR vs AGENTS vs FSD/LFSD si los hay.
```

NO se generan ni editan archivos. NO se ejecutan tests.

---

## 6. Verificación (criterios de "bien hecho")

- [ ] El reporte cubre los 30 criterios en orden, sin saltar ninguno.
- [ ] Cada hallazgo cita archivo + sección concreta del repo EduSync.
- [ ] Cada hallazgo tiene una **acción accionable** ("agregar columna CAP a §6.1"), no
      "mejorar X".
- [ ] El score se calcula `aciertos × 3.33`; criterios "no auditable" reportados aparte.
- [ ] Hallazgos críticos vs menores vs no auditables están separados.
- [ ] Se reporta al menos un conflicto de coherencia o se declara explícitamente "sin
      conflictos".
- [ ] El reporte NO inventa: si un criterio no puede verificarse por falta de archivo,
      se marca como "no auditable" citando el archivo faltante.
- [ ] NO se crean ni editan archivos (skill de auditoría pura).

---

## 7. Anti-patrones específicos del skill

| Anti-patrón | Por qué es un error | Mitigación |
|-------------|---------------------|------------|
| Aplicar criterio de microservicios al monolito modular como si fuera falla | Genera falsos positivos y degrada el score injustamente | Regla §4: marcar "no auditable" citando ADR-0007 Strangler Fig |
| Inventar parámetros numéricos para "rellenar" criterio B8 | Auditoría pierde credibilidad | Si no hay número, marcar falla con evidencia exacta de la ausencia |
| Exponer RUDE, nombre o PII en ejemplos del reporte (viola `AGENTS.md §7`) | Filtra dato sensible al log de chat | Citar tablas/secciones; nunca pegar datos reales |
| Editar `docs/DTI.md` durante la auditoría | El skill es solo lectura por contrato | `allowed-tools: [read]` en frontmatter; si el usuario pide editar, redirigir a `dti-edusync` |
| Confundir `Math.floor()` con `round`/`HALF_UP` al auditar BR-008 / BR-003 | Reporta falsamente que se viola la invariante de consolidación | Verificar literal en `docs/arquitectura_hexagonal_EduSync.md §5` (Centralizador) |
| Aceptar `Distributed Monolith` como "está bien porque es monolito modular" | EduSync v1.x es monolito modular **intencional**; el anti-patrón solo aplica en v2.0 cuando se extraigan servicios y compartan BD | Distinguir estado actual vs estado objetivo en criterio 27 |

---

## 8. Mini ejemplo de invocación

> "Usa el skill `distributed-architecture-reviewer-edusync` para auditar EduSync antes de
> abrir `release/2.0.0`. Lee `docs/DTI.md`, `AGENTS.md`, ADR-0001..0016 y los diagramas
> C4. Quiero score 0-100 + hallazgos críticos must-fix + fortalezas para citar en la
> defensa final."

---

## 9. Modos de fallo conocidos

- **`E_MISSING_INPUT`** — Falta uno de los archivos críticos de §2 →
  STOP, listar los archivos ausentes y esperar.
- **`E_EMPTY_DTI_S6`** — `docs/DTI.md §6` existe pero está vacío o solo con plantilla →
  STOP, indicar "ejecutar primero `dti-edusync` para poblar §6".
- **`E_NO_ADRS`** — `docs/adr/` está vacío o no contiene ADR-0007 (Strangler Fig) →
  STOP, indicar "ejecutar primero `adr-edusync` para crear ADR-0001..0007".
- **`E_DTI_ADR_CONFLICT`** — DTI dice X y ADR dice ¬X (ej. DTI declara microservicios y
  ADR-0007 dice monolito modular) → NO asumir cuál tiene razón; reportar como hallazgo
  crítico de coherencia y continuar.
- **`E_USER_OVERRIDE`** — Usuario justifica un anti-patrón como aceptable (ej. dual-write
  OK en flujo no crítico) → aceptar la justificación, bajar el hallazgo a "informativo" y
  registrar la justificación en el reporte.
- **`E_OUT_OF_SCOPE`** — Usuario pide proponer cambios de UX o redacción → recordar que
  el scope es arquitectónico; rechazar y continuar con la auditoría.

---

## 10. Registro de cambios del Skill

| Versión | Fecha      | Autor          | Cambio                                          | Documentos base                                                                 |
|---------|------------|----------------|-------------------------------------------------|---------------------------------------------------------------------------------|
| 0.1.0   | 28/05/2026 | Rodrigo Aspeti | Versión inicial — adaptación EduSync del skill genérico `distributed_architecure_reviewer_SKILL.md` de `plantillas2/`. Incorpora regla "monolito modular v1.x vs microservicios v2.0" referenciando ADR-0007. Inputs canónicos del repo EduSync. Anti-patrones específicos del proyecto (PII, `Math.floor()`, DA-04 Outbox). | DTI v0.3, FSD v1.0, AGENTS v0.4, PROMPT_MAPPING v1.2, ADR-0001..0016, arquitectura_hexagonal v0.1 |
