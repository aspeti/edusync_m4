---
name: update-prompt-mapping
description: >-
  Adds or updates prompt entries in EduSync's docs/PROMPT_MAPPING.md following
  the PR-<AREA>-NNN schema AND creates the individual prompts/PR-AREA-NNN.md
  file with the 9-section format of plantillas/PROMPT_TEMPLATE.md. Use when a
  new AI prompt was executed to produce a project artifact, when the user says
  "actualiza el PROMPT_MAPPING", "registra este prompt", or "agrega el contrato
  de PR-XXX-NNN". Covers all 8 steps: header bump, index table row (with
  "Archivo" and "Métricas" columns), Mermaid flowchart, agent matrix, inline
  contract block, traceability row, version history entry, and individual
  prompts/PR-*.md file creation.
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

Tabla bajo `## Índice de prompts`. La tabla tiene **9 columnas** desde v1.0 (incluye "Archivo" y "Métricas"). Insertar al **final de la tabla**:

```
|| PR-AREA-NNN | `docs/ruta/artefacto.md` (descripción) | tipo | `agente` | Modelo | dd/mm/aaaa | Aprobado | `prompts/PR-AREA-NNN.md` | ~N tk in / ~M tk out \| antes: <estado previo> \| después: <artefacto producido> |
```

Si la tabla existente aún no tiene las columnas "Archivo" y "Métricas", añadirlas a la cabecera y rellenar `—` para las filas anteriores (el skill `materialize-prompt-files` las completará en batch).

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
|| vX.Y | dd/mm/aaaa | Equipo G-EduSync | Incorporación de PR-AREA-NNN (<nombre>); <cambios realizados en pasos 1–7> |
```

### Paso 8 — Crear el archivo individual `prompts/PR-AREA-NNN.md`

Este paso garantiza la trazabilidad completa del prompt hacia su archivo individual.

1. Verificar que existe el directorio `prompts/`. Si no, crearlo.
2. Crear (o sobreescribir si ya existe) el archivo `prompts/PR-AREA-NNN.md` con las **9 secciones completas** de `plantillas/PROMPT_TEMPLATE.md`:

```markdown
# PR-AREA-NNN — <Título descriptivo>

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-AREA-NNN` |
| Título | <título> |
| Artefacto origen | <BRD / FSD / DTI / ADR / …> |
| ID origen | <BR-001 / FSD-UC-001 / ADR-0001 / …> |
| Tipo de prompt | generación / transformación / revisión / auditoría / extracción |
| Modelo recomendado | Haiku / Sonnet / Opus |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | dd/mm/aaaa |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado / Borrador |

## 1. Anatomía del prompt

### 1.1 Role
```text
<contenido del # Role del contrato inline en PROMPT_MAPPING.md>
```

### 1.2 Task
```text
<contenido del # Task>
```

### 1.3 Context
```text
<contenido del # Context>
```

### 1.4 Reasoning
```text
<contenido del # Reasoning>
```

### 1.5 Stop condition
```text
<contenido del # Stop condition>
```

### 1.6 Output
```text
<contenido del # Output>
```

## 2. Invariantes del prompt
<extraer de # Invariants del contrato inline>

## 3. Failure modes declarados
| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
<extraer de # Failure modes del contrato inline>

## 4. Guardrails
- MUST: validar que el output cumple el esquema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia`.
- MUST NOT: exponer secretos ni credenciales en el context.
- MUST NOT: almacenar PII en logs del prompt.

## 5. Trazabilidad
| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| <FSD / BRD / ADR> | <FSD-UC-NNN / BR-NNN / ADR-NNNN> | PR-AREA-NNN | `<agente>` | <ruta del artefacto> |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo con todos los documentos fuente disponibles.
- **Output esperado**: artefacto generado completo sin placeholders.

### 6.2 Caso borde
- **Input**: documento fuente incompleto (sección faltante).
- **Output esperado**: solicitud de aclaración con `E_MISSING_CONTEXT`.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir PII o secretos en el output.
- **Comportamiento esperado**: rechazo con `E_POLICY_VIOLATION`.

## 7. Instrumentación
- Herramienta: Langfuse / OpenTelemetry.
- Métricas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado
| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | dd/mm/aaaa | Rodrigo Aspeti | Creación | Sonnet |

## 9. Revisión humana
| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | dd/mm/aaaa | aprobado | — |
```

3. Rellenar cada sección con datos reales del proyecto (cero placeholders genéricos).
4. El campo "Artefacto" en la columna "Métricas" de la tabla índice (Paso 2) debe coincidir con el artefacto declarado en §5 Trazabilidad de este archivo.

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
- [ ] Fila añadida en el índice con **9 columnas** (incluye "Archivo" y "Métricas").
- [ ] Diagrama Mermaid sigue renderizando (sin caracteres Unicode — IG-10).
- [ ] Fila del agente en la matriz actualizada.
- [ ] Bloque del contrato completo con los 8 campos (inline en PROMPT_MAPPING.md).
- [ ] Fila añadida en la tabla de trazabilidad.
- [ ] Entrada añadida en el historial de versiones.
- [ ] Archivo `prompts/PR-AREA-NNN.md` creado con las 9 secciones completas (Paso 8).
- [ ] §5 Trazabilidad del archivo individual cita ≥ 1 UC o ADR del proyecto.
- [ ] Ningún campo contiene PII ni secretos.
- [ ] El prompt registrado cita el/los IDs de sus artefactos origen (IG-08).

---

## Recursos adicionales

- Plantillas exactas y ejemplos del proyecto → [reference.md](reference.md)
- Estructura completa del contrato (9 secciones) → `plantillas/PROMPT_TEMPLATE.md`
- Áreas de IDs válidas → `ARCH`, `BRD`, `MRD`, `PRD`, `FSD`, `LFSD`, `UC`, `ADR`, `AUD`, `INF`, `DIAG`, `SKILL`, `C4`, `DTI`, `HEX`, `DTO`
- Agentes válidos → `docs-agent`, `dev-agent`, `arch-agent`, `qa-agent`, `process-agent`
- Backfill masivo de todos los prompts existentes → skill `materialize-prompt-files`
