---
name: update-prompt-mapping
description: >-
  Adds or updates prompt entries in EduSync's docs/PROMPT_MAPPING.md following
  the PR-<AREA>-NNN schema. Use when a new AI prompt was executed to produce a
  project artifact, when the user says "actualiza el PROMPT_MAPPING", "registra
  este prompt", or "agrega el contrato de PR-XXX-NNN". Covers all 7 required
  sections: header version bump, index table, Mermaid flowchart, agent matrix,
  prompt contract block, traceability row, and version history entry.
status: stable
owner: G-EduSync
---

# Actualizar PROMPT_MAPPING — EduSync

## Cuándo activar este skill

- El usuario acaba de ejecutar un prompt y quiere registrarlo.
- El usuario cita explícitamente `docs/PROMPT_MAPPING.md` o pide "actualizar el mapping".
- Se acaba de generar un nuevo artefacto documental (BRD, MRD, PRD, FSD, LFSD, diagrama, ADR…).

**NO activar** cuando: el usuario está discutiendo negocio, diseñando prompts, o revisando el documento sin intención de modificarlo.

---

## Entradas obligatorias

Antes de modificar el archivo, confirmar que el usuario ha proporcionado:

| Campo | Descripción | Ejemplo |
|-------|-------------|---------|
| `ID` | `PR-<AREA>-NNN` | `PR-MRD-001` |
| `Artefacto producido` | ruta + descripción del archivo generado | `docs/mrd/MRD_EduSync.md` |
| `Tipo` | generación / transformación / auditoría / extracción / revisión | `generación` |
| `Agente` | docs-agent / dev-agent / arch-agent / qa-agent / process-agent | `docs-agent` |
| `Modelo` | Haiku / Sonnet / Opus | `Sonnet` |
| `Fecha` | dd/mm/aaaa | `17/05/2026` |
| `Contenido del prompt` | Role + Task + Context + Reasoning + Stop + Output + Invariants + Failure modes | *(ver §Formato del contrato)* |
| `Artefacto(s) origen` | qué documentos sirvieron de entrada | `BRD v2.0, arquitectura_funcional_EduSync.md` |

Si falta algún campo, **responder**: "Necesito [campo faltante] antes de actualizar el PROMPT_MAPPING."

---

## Procedimiento — 7 pasos en orden

Lee `docs/PROMPT_MAPPING.md` completo antes de editar. Los 7 pasos son secuenciales.

### Paso 1 — Actualizar la línea de cabecera

Localizar la segunda línea del documento:

```
> IDs: `ARCH` / `BRD` / ... Versión activa: `vX.Y`.
```

- Si el AREA del nuevo prompt **ya existe** en la lista → solo incrementar `vX.Y` → `vX.(Y+1)`.
- Si el AREA es **nueva** → añadir `` `AREA` / `` antes del punto final + incrementar versión.

### Paso 2 — Añadir fila en el índice de prompts

Tabla bajo `## Índice de prompts`. Insertar al **final de la tabla**, una fila con formato:

```
|| PR-AREA-NNN | `docs/ruta/artefacto.md` (descripción breve) | tipo | `agente` | Modelo | dd/mm/aaaa | Aprobado |
```

### Paso 3 — Actualizar el diagrama Mermaid

Sección `## Flujo general de información entre prompts`.

- Si el nuevo prompt produce un **nodo ya representado** (ej. docs-agent → MRD) → solo añadir arista si hay nueva conexión de datos.
- Si es un **nodo completamente nuevo** → agregar nodo con alias + texto + la(s) arista(s) de entrada.
- Mantener los subgraphs existentes (`DOMINIO`, `SOPORTE`, `PROCESOS`). Añadir nuevo subgraph solo si el agente es nuevo.

### Paso 4 — Actualizar la matriz de responsabilidades

Tabla bajo `## Matriz de responsabilidades por agente`.

- Localizar la fila del agente responsable.
- Añadir el nuevo ID (`PR-AREA-NNN`) a la columna "Prompts asignados".
- Actualizar "Artefactos generados" si el tipo de artefacto es nuevo para ese agente.
- Si el agente es **nuevo**, insertar fila completa.

### Paso 5 — Insertar el bloque del contrato

Sección `## Prompts`. Insertar **al final del último bloque existente**, con separador `---`:

```markdown
---

### PR-AREA-NNN — <Título descriptivo>

```markdown
# Role
<rol con nivel de senioridad y dominio>

# Task
<acción atómica y verificable>

# Context
- Documentos fuente: <lista>
- Entradas esperadas: <estructura>
- Restricciones de dominio: <BR-NNN / invariantes>
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 15

# Reasoning
1. <Paso 1>
2. <Paso 2>
3. <Paso 3>

# Stop condition
Detente cuando <condición objetiva>.

# Output
<formato + ejemplo>

# Invariants
- <Invariante 1 verificable>
- <Invariante 2 verificable>

# Failure modes
- E_MISSING_CONTEXT: falta documento fuente — STOP, solicitar.
- E_<OTRO>: <descripción> — <acción>.
` `` `
```

> Usar los mismos nombres de invariantes globales (`IG-01..IG-10`) cuando apliquen.

### Paso 6 — Añadir fila en la tabla de trazabilidad

Tabla bajo `## Trazabilidad completa`. Insertar al final:

```
|| <Artefacto origen> | `<ID origen>` | PR-AREA-NNN | `agente` | <Artefacto generado> | `docs/ruta/archivo.md` |
```

### Paso 7 — Añadir entrada en el historial de versiones

Tabla bajo `## Historial de versiones`. Insertar **una fila nueva al final**:

```
|| vX.Y | dd/mm/aaaa | Equipo G-EduSync | Incorporación de PR-AREA-NNN (<nombre>); <cambios realizados en pasos 1–6> |
```

---

## Formato del contrato (anatomía completa)

Ver `plantillas/PROMPT_TEMPLATE.md` para la referencia completa.
Campos mínimos obligatorios para marcar un prompt como `Aprobado`:

| # | Campo | Regla |
|---|-------|-------|
| 1 | `Role` | Nivel de senioridad + dominio específico |
| 2 | `Task` | Una sola acción atómica y verificable |
| 3 | `Context` | Documentos fuente + restricciones de dominio |
| 4 | `Reasoning` | ≥ 3 pasos numerados |
| 5 | `Stop condition` | Criterio objetivo de finalización |
| 6 | `Output` | Formato + ejemplo mínimo |
| + | `Invariants` | ≥ 1 regla verificable (puede citar IG-01..IG-10) |
| + | `Failure modes` | ≥ 1 código `E_` con acción concreta |

---

## Validación antes de entregar

- [ ] Versión en cabecera incrementada.
- [ ] Fila añadida en el índice de prompts con los 7 campos completos.
- [ ] Diagrama Mermaid sigue renderizando (sin caracteres Unicode decorativos en labels — IG-10).
- [ ] Fila del agente en la matriz actualizada.
- [ ] Bloque del contrato completo con los 8 campos.
- [ ] Fila añadida en la tabla de trazabilidad.
- [ ] Entrada añadida en el historial de versiones.
- [ ] Ningún campo contiene PII ni secretos.
- [ ] El prompt registrado cita el/los IDs de sus artefactos origen (IG-08).

---

## Recursos adicionales

- Plantillas exactas y ejemplos del proyecto → [reference.md](reference.md)
- Estructura completa del contrato → `plantillas/PROMPT_TEMPLATE.md`
- Áreas de IDs válidas → `ARCH`, `BRD`, `MRD`, `PRD`, `FSD`, `LFSD`, `UC`, `ADR`, `AUD`, `INF`, `DIAG`
- Agentes válidos → `docs-agent`, `dev-agent`, `arch-agent`, `qa-agent`, `process-agent`
