---
name: materialize-prompt-files
description: >-
  Materializes individual prompts/PR-*.md files from contracts already
  registered in docs/PROMPT_MAPPING.md and enriches the index table with
  "Archivo" and "Métricas" columns to fully document the prompt ecosystem.
  Use when the user says "materializar prompts", "crear archivos
  PR-*.md", "backfill prompt files", or "completar prompts/".
status: stable
owner: G-EduSync
---

# Materializar archivos `prompts/PR-*.md`

El estándar de calidad del mapeo de prompts del proyecto exige:
> "tabla completa símbolo → archivo/sección → métricas antes/después"

Esto requiere dos entregables que este skill produce en una sola pasada:

| Entregable | Ruta | Estado objetivo |
|------------|------|-----------------|
| Archivos individuales de cada prompt | `prompts/PR-AREA-NNN.md` (uno por entrada del índice) | Todos con las 9 secciones completas |
| Tabla índice enriquecida | `docs/PROMPT_MAPPING.md` §"Índice de prompts" | Columnas "Archivo" y "Métricas" con valores reales |

---

## Cuándo activar este skill

- El usuario menciona "materializar prompts", "prompts/PR-*.md".
- El directorio `prompts/` no existe o tiene menos archivos que filas en el índice.

**NO activar** para registrar un prompt nuevo — usar `update-prompt-mapping` para eso.

---

## Fuentes de verdad (leer antes de ejecutar)

1. `plantillas/PROMPT_TEMPLATE.md` — estructura exacta de las 9 secciones.
2. `docs/PROMPT_MAPPING.md` — contratos inline existentes (fuente del §1 Anatomía).
3. `AGENTS.md` — roles de agente para §5 Trazabilidad.
4. `docs/fsd/FSD_EduSync.md` — UCs para referencias en §5.
5. `docs/adr/*.md` — ADRs para referencias en prompts de arquitectura.

---

## Procedimiento — 4 fases en orden

### Fase 1 — Auditar el estado actual

1. Listar todas las filas del `## Índice de prompts` de `docs/PROMPT_MAPPING.md`.
2. Listar los archivos existentes en `prompts/` (si el directorio ya existe).
3. Calcular la diferencia: prompts sin archivo individual → lista de trabajo.
4. Verificar si la tabla índice ya tiene columnas "Archivo" y "Métricas".

Reportar al usuario el resumen antes de continuar:
```
Prompts en índice: N
Archivos en prompts/: M
Pendientes de crear: N - M
Columnas "Archivo"/"Métricas" en índice: SÍ / NO
```

### Fase 2 — Crear directorio y archivos individuales

Para cada prompt ID pendiente, en el orden del índice (prioridad: UC > ADR > HEX > DTI > FSD > ARCH > DTO > resto):

1. Localizar el bloque `### PR-AREA-NNN —` en `docs/PROMPT_MAPPING.md`.
2. Extraer el contenido del bloque de código ````markdown` … ` ``` `` como §1 Anatomía.
3. Crear `prompts/PR-AREA-NNN.md` con las 9 secciones completas (ver §Plantilla de archivo).
4. Rellenar cada sección con datos reales del proyecto (cero placeholders genéricos `<…>`).

**Regla de extracción**: el contrato inline en PROMPT_MAPPING.md ya tiene los 6 elementos
(`# Role`, `# Task`, `# Context`, `# Reasoning`, `# Stop condition`, `# Output`, `# Invariants`,
`# Failure modes`). Mapearlos directamente a §1.1–1.6 + §2 + §3 del archivo individual.

### Fase 3 — Enriquecer la tabla índice de PROMPT_MAPPING.md

Si la tabla no tiene las columnas "Archivo" y "Métricas":

1. Añadir a la línea de cabecera de la tabla: `| Archivo | Métricas |`
2. Para cada fila existente, añadir al final:
   - **Archivo**: `prompts/PR-AREA-NNN.md`
   - **Métricas**: `~N tk in / ~M tk out | antes: <estado previo> | después: <artefacto>`

**Guía de estimación de tokens**:

| Tipo de prompt | Tokens in (aprox.) | Tokens out (aprox.) |
|---|---|---|
| Generación de UC (contrato) | ~800–1 200 | ~2 000–4 000 |
| Generación de documento complejo (FSD, LFSD, DTI) | ~3 000–6 000 | ~15 000–25 000 |
| Generación de arquitectura (HEX, C4) | ~2 000–4 000 | ~8 000–15 000 |
| Generación de ADR (uno) | ~1 500–2 500 | ~3 000–5 000 |
| Generación de BRD/MRD/PRD | ~1 500–3 000 | ~8 000–15 000 |
| Skill / diagrama / extracto | ~500–1 000 | ~1 500–4 000 |

**Formato de la celda "Métricas"**:
```
~1 200 tk in / ~3 500 tk out | antes: UC-01 sin contrato | después: contrato PR-UC-001 en PROMPT_MAPPING.md
```

### Fase 4 — Actualizar cabecera de PROMPT_MAPPING.md

1. Cambiar `Versión activa: v0.9` → `Versión activa: v1.0`.
2. Añadir nota bajo el encabezado: `> Archivos individuales en \`prompts/PR-*.md\`.`
3. Añadir entrada en `## Historial de versiones`:
   ```
   | v1.0 | 28/05/2026 | Rodrigo Aspeti | Actualización: columnas "Archivo" y "Métricas" en índice; N archivos prompts/PR-*.md creados |
   ```

---

## Plantilla de archivo individual

Usar esta estructura exacta para cada `prompts/PR-AREA-NNN.md`:

```markdown
# PR-AREA-NNN — <Título descriptivo>

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-AREA-NNN` |
| Título | <título exacto del índice> |
| Artefacto origen | <BRD / FSD / DTI / ADR / arquitectura_funcional> |
| ID origen | <BR-NNN / FSD-UC-NNN / ADR-NNNN / DA-NN> |
| Tipo de prompt | generación / transformación / revisión / auditoría / extracción |
| Modelo recomendado | Haiku / Sonnet / Opus |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | <fecha del índice> |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado / Borrador |

## 1. Anatomía del prompt

### 1.1 Role
[contenido de # Role del contrato inline]

### 1.2 Task
[contenido de # Task]

### 1.3 Context
[contenido de # Context]

### 1.4 Reasoning
[contenido de # Reasoning]

### 1.5 Stop condition
[contenido de # Stop condition]

### 1.6 Output
[contenido de # Output]

## 2. Invariantes del prompt
[extraer de # Invariants del contrato inline; si no existe, derivar de los UCs afectados]

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
[extraer de # Failure modes; si no existe, añadir E_MISSING_CONTEXT y E_POLICY_VIOLATION]

## 4. Guardrails
- MUST: validar que el output cumple el esquema antes de consumirlo.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia` en telemetría.
- MUST NOT: exponer secretos ni credenciales en el context.
- MUST NOT: almacenar PII en logs del prompt.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| <FSD / BRD / ADR / LFSD> | <FSD-UC-NNN / BR-NNN / ADR-NNNN> | PR-AREA-NNN | `<agente>` | <ruta del artefacto> |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: contexto completo con todos los documentos fuente disponibles.
- **Output esperado**: artefacto generado completo sin placeholders ni texto de plantilla.

### 6.2 Caso borde
- **Input**: documento fuente con sección faltante o campo nulo.
- **Output esperado**: solicitud de aclaración al usuario con código `E_MISSING_CONTEXT`.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir PII (nombre, DNI, RUDE en claro) en el output.
- **Comportamiento esperado**: rechazo con `E_POLICY_VIOLATION`; output no generado.

## 7. Instrumentación
- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`, `hallucination_rate`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | <fecha del índice> | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
```

---

## Invariantes de este skill

- Cada archivo `prompts/PR-AREA-NNN.md` tiene las 9 secciones completas sin placeholders `<…>`.
- §5 Trazabilidad de cada archivo cita ≥ 1 UC (`FSD-UC-NNN`) o ADR (`ADR-NNNN`) cuando aplique.
- El ID en §0 Metadatos coincide exactamente con el ID en la tabla índice.
- La columna "Archivo" usa rutas relativas desde la raíz: `prompts/PR-AREA-NNN.md`.
- La columna "Métricas" tiene valores estimados reales, no celdas vacías ni `—`.
- `docs/PROMPT_MAPPING.md` no pierde ningún contenido pre-existente (solo se añaden columnas).
- El directorio `prompts/` contiene exactamente tantos archivos como filas tiene el índice.

---

## Checklist de verificación final

- [ ] `prompts/` existe y tiene N archivos (N = filas del índice).
- [ ] Cada archivo tiene las 9 secciones sin texto de plantilla sin reemplazar.
- [ ] Tabla índice de PROMPT_MAPPING.md tiene columnas "Archivo" y "Métricas" completas.
- [ ] `docs/PROMPT_MAPPING.md` versión bumpeada a v1.0.
- [ ] Entrada v1.0 añadida en `## Historial de versiones`.
- [ ] Ningún archivo contiene PII ni secretos de producción.
- [ ] §5 Trazabilidad de cada archivo cita al menos 1 artefacto origen del proyecto.

---

## Recursos adicionales

- Estructura de 9 secciones → `plantillas/PROMPT_TEMPLATE.md`
- Registro incremental de un nuevo prompt → skill `update-prompt-mapping`
- Contratos inline existentes → `docs/PROMPT_MAPPING.md` §Prompts
