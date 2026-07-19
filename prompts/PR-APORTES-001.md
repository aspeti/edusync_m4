# PR-APORTES-001 — Generación de `docs/aportes/release-2.0.0.md` (grupo unipersonal)

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-APORTES-001` |
| Título | Generación del informe de aportes individuales del release `release/2.0.0` de EduSync para grupo unipersonal (n = 1) aplicando la fórmula `clamp(tareas_i / promedio, 0.5, 1.1)` |
| Artefacto origen | `plantillas/APORTES_TEMPLATE.md` + auditoría del repo EduSync (39 prompts materializados, 6 ADRs, 10 diagramas, 2 POCs documentadas, documentos canónicos BRD/MRD/PRD/FSD/LFSD/DTI/AGENTS/roadmap, skills propios EduSync, rule `seguridad.mdc`) + identidad del único integrante |
| ID origen | `APORTES-TEMPLATE`, `AGENTS.md §16 L489`, `docs/roadmap.md §5 L-09`, `docs/PROMPT_MAPPING.md` v1.7, `docs/DTI.md` v0.7 |
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
Eres un Documentation Auditor + Project Manager académico del grupo
unipersonal G-EduSync (n = 1, integrante: Rodrigo Aspeti). Tienes
acceso de solo lectura al repositorio EduSync (Java 21, Spring Boot 3.3,
PostgreSQL 15 RLS, Angular 17, AWS ECS Fargate) y conoces:
- El catálogo de prompt-contratos materializados en `prompts/PR-*.md`
  (PROMPT_MAPPING v1.7+).
- Los 6 ADRs aprobados, las 2 POCs documentadas, los 10 diagramas
  Mermaid versionados, los documentos canónicos (BRD v1/v2, MRD, PRD,
  FSD, LFSD, ARQ funcional, ARQ hexagonal, DTOs, DTI v0.7,
  roadmap v0.1, AGENTS.md v0.9).
- La granularidad estándar de tareas declarada en
  `plantillas/APORTES_TEMPLATE.md §4`.
- La fórmula del módulo: `factor = clamp(tareas_i / aporte_promedio, 0.5, 1.1)`.
- El caso degenerado n = 1: el factor es trivialmente 1.0 y el archivo
  funciona como inventario auditable, no como ajuste relativo.
Tu objetivo es producir un informe auditable que documente todo el
trabajo individual sin inventar referencias.
```

### 1.2 Task
```text
Genera el archivo `docs/aportes/release-2.0.0.md` siguiendo exactamente
la estructura de `plantillas/APORTES_TEMPLATE.md` (6 secciones), con:
1. §0 Metadatos del release `release/2.0.0` y del único integrante.
2. §1 Tabla exhaustiva de TODAS las tareas auditables del repo,
   atribuidas al único integrante (Rodrigo Aspeti). Cada fila debe ser
   verificable contra un archivo+sección o commit del repo.
3. §2 Resumen: una sola fila con `total tareas == count(§1)`.
4. §3 Aplicación literal de la fórmula con nota explícita del caso
   degenerado n = 1 (`factor = 1.0`).
5. §4 Reglas de granularidad copiadas literalmente de la plantilla.
6. §5 Auditoría del docente vacía pero presente.
7. §6 Checklist con `[x]` en §0..§4 y `[ ]` en el ítem del commit final.
No se modifica ningún otro archivo del repo.
```

### 1.3 Context
```text
- Documento plantilla:
  `plantillas/APORTES_TEMPLATE.md` (no modificar; copiar estructura 1:1).
- Documentos auditables (cada uno potencialmente = 1 tarea según §4):
  - `AGENTS.md` v0.9 (raíz)
  - `docs/DTI.md` v0.7 (23 secciones + 7 bumps con changelog)
  - `docs/PROMPT_MAPPING.md` v1.7 (35+ contratos catalogados)
  - `docs/roadmap.md` v0.1
  - `docs/brd/BRD_EduSync_v1.md`, `docs/brd/BRD_EduSync_v2.md`
  - `docs/mrd/MRD_EduSync.md`
  - `docs/prd/PRD_EduSync.md`
  - `docs/fsd/FSD_EduSync.md` (FSD-UC-001..010)
  - `docs/LFSD-EduSync.md` v1.0.1
  - `docs/arquitectura_funcional_EduSync.md`
  - `docs/arquitectura_hexagonal_EduSync.md` v0.1
  - `docs/dtos_EduSync.md` v0.1
  - `docs/APORTES_EduSync.md` (release 1.0.0/1.0.1 previos)
  - `docs/adr/0001..0006-*.md` (6 ADRs)
  - `docs/pocs/POC-01-rls-multitenancy/` (README + runbook)
  - `docs/pocs/POC-02-circuit-breaker-sie/` (README + runbook)
  - `docs/diagrams/*.mmd` (10 diagramas)
  - `prompts/PR-*.md` (39 contratos materializados al 28/05/2026)
  - `.cursor/skills/<slug>/SKILL.md` (skills PROPIOS de EduSync:
    `c4-edusync`, `dti-edusync`, `adr-edusync`, `poc-runner-edusync`,
    `sync-doc-chain`, `edusync-skill-creator`,
    `materialize-prompt-files`, `update-prompt-mapping`,
    `distributed-architecture-reviewer`). NO contar los 19 skills
    canónicos importados desde `plantillas2/`.
  - `.cursor/rules/seguridad.mdc` (rule de dominio EduSync)
- Categorías admitidas (lista cerrada literal):
  `BRD` · `MRD` · `PRD` · `FSD` · `UC` · `NFR` · `Gherkin` · `Diagrama`
  · `ADR` · `AGENTS` · `Skill` · `Rule` · `POC` · `Código` · `Test`
  · `Presentación` · `Bitácora` · `Prompt` · `Otro`.
- Granularidad: literal de `APORTES_TEMPLATE.md §4`.
- Privacidad: solo nombre del único integrante. Cero correos, teléfonos
  o IDs académicos.
- INPUTS parametrizables (bloque YAML del invocador; si la
  configuración n_integrantes ≠ 1 el prompt aborta):

  release: "release/2.0.0"
  sesion: "S12"
  fecha_cierre: "<dd/mm/aaaa>"
  branch: "release/2.0.0"
  commit_head: "<hash corto>"
  integrantes:
    - nombre: "Rodrigo Aspeti"
      seudonimo: null
  n_integrantes: 1
  nota_grupal: null

- Cumplimiento: Ley 164 datos personales (Bolivia).
- Formato de salida: Markdown plano, encabezados `##` y `###` como en la
  plantilla.
```

### 1.4 Reasoning
```text
1. Validar el bloque INPUTS:
   - Si `n_integrantes != 1`, abortar con `E_N_INTEGRANTES_NO_VALIDO`.
   - Si la lista `integrantes` está vacía o tiene > 1 entrada, abortar
     con `E_N_INTEGRANTES_NO_VALIDO`.
   - Si `release != release/2.0.0`, abortar con `E_RELEASE_TAG_MISMATCH`.
2. Construir el inventario exhaustivo recorriendo los documentos de
   §1.3 y aplicando la granularidad de `APORTES_TEMPLATE.md §4`:
   - Cada `prompts/PR-*.md` materializado = 1 tarea (Categoría `Prompt`).
   - Cada `docs/adr/0001..0006-*.md` aprobado = 1 tarea (Categoría `ADR`).
   - Cada `docs/diagrams/*.mmd` versionado = 1 tarea (Categoría `Diagrama`).
   - Cada POC con README + runbook = 2 tareas (Categoría `POC`); si
     `docs/pocs/POC-NN/evidencia/` tiene métricas reales, +1 tarea por
     POC ejecutada.
   - Cada skill PROPIO bajo `.cursor/skills/<slug>/SKILL.md` (lista
     declarada en §1.3) = 1 tarea (Categoría `Skill`).
   - `.cursor/rules/seguridad.mdc` = 1 tarea (Categoría `Rule`).
   - Cada documento canónico estructural (BRD v1, BRD v2, MRD, PRD,
     FSD, LFSD, ARQ funcional, ARQ hexagonal, DTOs, APORTES previo,
     roadmap, DTI, AGENTS, PROMPT_MAPPING) = 1 tarea (Categoría
     correspondiente).
   - Cada bump de versión documental con changelog razonado en
     `Registro de cambios` (DTI v0.2..v0.7, AGENTS v0.7..v0.9,
     PROMPT_MAPPING v1.5..v1.7) = 1 tarea (Categoría `Bitácora`).
3. Imputar TODAS las filas al único integrante: `Rodrigo Aspeti`. No es
   necesario un mapping `atribuciones` porque n = 1.
4. Construir §1 con orden cronológico ascendente (campo Fecha); columnas
   exactas: `# | Integrante | Tarea concreta | Categoría | Referencia
   | Fecha`. La Referencia debe ser `archivo §sección` o `commit <hash>`
   verificable.
5. Construir §2 con UNA fila para Rodrigo Aspeti: `total = count(§1)`,
   `categorías_cubiertas = #distinct(Categoría)`, observación libre. La
   fila "Total grupo" repite el mismo número (n = 1).
6. Construir §3 con la nota degenerada n = 1:
   - `aporte_promedio = T / 1 = T`.
   - `factor_raw = T / T = 1.00`.
   - `factor = clamp(1.00, 0.5, 1.1) = 1.00` (siempre).
   - Si `nota_grupal != null`: `nota_individual = nota_grupal × 1.00
     = nota_grupal`. Si es null, omitir la columna.
   - Agregar nota explícita: "Caso degenerado (n = 1): el factor es
     trivialmente 1.0; este archivo funciona como inventario auditable
     del trabajo individual, no como ajuste de nota relativo."
7. Copiar §4 literalmente desde `APORTES_TEMPLATE.md §4` (no modificar).
8. Insertar §5 vacío con tabla cabecera y nota "Si está vacío, se aplica
   el cálculo automático de §3".
9. §6 Checklist: marcar `[x]` los ítems §0..§4 y dejar `[ ]` solo el
   ítem del commit final.
10. No tocar ningún archivo fuera de `docs/aportes/release-2.0.0.md`.
```

### 1.5 Stop condition
```text
Detente cuando:
- `docs/aportes/release-2.0.0.md` exista con las 6 secciones de la
  plantilla.
- §1 tenga >= 50 filas auditables (cota mínima realista para un release
  que cierra el Módulo 4 de EduSync); cada fila con los 6 campos llenos
  y una Categoría dentro de la lista cerrada.
- §2 cuadra con §1 (`tareas Rodrigo == count(§1)`) y la fila total
  coincide.
- §3 declara `aporte_promedio = T`, `factor_raw = 1.00`, `factor = 1.00`
  y la nota explicativa del caso degenerado n = 1.
- §4 es copia literal de `APORTES_TEMPLATE.md §4`.
- §6 checklist con `[x]` en §0..§4 y `[ ]` en el ítem del commit final.
- No se modificó ningún otro archivo del repo (la propagación a
  PROMPT_MAPPING/AGENTS corre por una tarea posterior).
- Cero PII fuera del nombre del único integrante en §0 y §1.
```

### 1.6 Output
```text
Archivo `docs/aportes/release-2.0.0.md` con la estructura literal de
`plantillas/APORTES_TEMPLATE.md`:

# Aportes Individuales — release/2.0.0 EduSync

## 0. Metadatos
| Producto | EduSync |
| Grupo | G-EduSync |
| Release evaluable | release/2.0.0 |
| Sesión asociada | S12 |
| Fecha de cierre | <dd/mm/aaaa> |
| Integrantes (n) | Rodrigo Aspeti (n = 1) |
| Branch | release/2.0.0 |
| Commit HEAD | <hash | pendiente> |

## 1. Tabla de tareas atribuidas
(>= 50 filas, todas con Integrante = Rodrigo Aspeti)

## 2. Resumen por integrante
| Rodrigo Aspeti | T | M | obs |
| Total grupo    | T | — | — |

## 3. Cálculo del factor (caso degenerado n = 1)
- aporte_promedio = T / 1 = T
- factor_raw      = T / T = 1.00
- factor          = clamp(1.00, 0.5, 1.1) = 1.00

## 4. Reglas del grupo sobre qué cuenta como tarea
<copia literal de APORTES_TEMPLATE.md §4>

## 5. Auditoría del docente
(tabla vacía con placeholder)

## 6. Checklist de cierre
- [x] §0 metadatos completos
- [x] §1 referencias verificables
- [x] §2 suma cuadra con §1
- [x] §3 fórmula aplicada (caso n = 1 documentado)
- [x] §4 granularidad respetada (texto literal)
- [ ] Archivo commiteado en release/2.0.0 antes del cierre
```

## 2. Invariantes del prompt

- El archivo generado **debe** tener exactamente las 6 secciones de `APORTES_TEMPLATE.md` (§0..§6) en el mismo orden y con los mismos títulos.
- `n_integrantes == 1` **siempre**; si no, abortar.
- El único integrante **debe** ser `Rodrigo Aspeti` (configurable en INPUTS, pero rechaza nombres adicionales).
- Cada fila de §1 **debe** tener `Integrante = "Rodrigo Aspeti"` y Categoría ∈ {`BRD`, `MRD`, `PRD`, `FSD`, `UC`, `NFR`, `Gherkin`, `Diagrama`, `ADR`, `AGENTS`, `Skill`, `Rule`, `POC`, `Código`, `Test`, `Presentación`, `Bitácora`, `Prompt`, `Otro`}.
- `Σ tareas §2 == count(filas §1)` (igualdad estricta).
- `factor == 1.00` **siempre** (consecuencia matemática de n = 1); cualquier desvío es bug.
- §3 **debe** incluir la nota explícita del caso degenerado n = 1.
- El archivo **no debe** contener PII fuera del nombre del único integrante.
- El prompt **no debe** modificar ningún archivo fuera de `docs/aportes/release-2.0.0.md`.
- §4 **debe** ser copia literal byte-a-byte de `APORTES_TEMPLATE.md §4`.
- Si `nota_grupal` es `null`, la columna `nota_individual` de §3 **debe** estar ausente.
- No contar los 19 skills canónicos importados desde `plantillas2/` como aporte del grupo.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_N_INTEGRANTES_NO_VALIDO` | `INPUTS.n_integrantes != 1` o la lista `integrantes` no tiene exactamente 1 entrada | STOP; verificar identidad del grupo antes de reintentar |
| `E_RELEASE_TAG_MISMATCH` | `INPUTS.release` ≠ `release/2.0.0` | Corregir el tag o crear `docs/aportes/release-<otro>.md` |
| `E_CATEGORIA_INVALIDA` | Una fila usa una Categoría fuera de la lista cerrada | Reemplazar por una válida o usar `Otro` con justificación |
| `E_REFERENCIA_NO_VERIFICABLE` | Una fila apunta a un archivo/sección que no existe en el repo | Corregir la referencia antes de guardar |
| `E_GRANULARIDAD_INFLADA` | Una fila no cumple §4 (commit cosmético, typo aislado, copy-paste sin adaptación) | Eliminar la fila |
| `E_TOTAL_DESCUADRADO` | `Σ §2 ≠ count(§1)` | Recontar antes de guardar |
| `E_FACTOR_NO_UNITARIO_N1` | Con `n = 1`, el `factor` reportado en §3 ≠ 1.00 | Bug del prompt; forzar a 1.00 y registrar incidente |
| `E_INVENTARIO_INCOMPLETO` | §1 tiene menos de 50 filas | Recorrer nuevamente el repo (ADRs, diagramas, prompts, secciones DTI/FSD) hasta cubrirlo todo |
| `E_PII_FILTRADA` | El archivo contiene correo, teléfono, RUDE, ID académico u otro PII | Revertir; sanear el campo afectado |
| `E_OUT_OF_SCOPE_EDIT` | El prompt editó algún archivo fuera de `docs/aportes/release-2.0.0.md` | Revertir; el alcance es estricto |

## 4. Guardrails

- **MUST**: validar el INPUT YAML antes de generar (schema mínimo: `release`, `n_integrantes == 1`, `integrantes[0].nombre`).
- **MUST**: registrar `promptId`, `version`, `modelo`, `tokens`, `latencia`, `n_filas_§1` en telemetría.
- **MUST**: cada fila de §1 cita archivo+sección o commit del repo (referencia verificable).
- **MUST**: respetar la granularidad de `APORTES_TEMPLATE.md §4` literalmente.
- **MUST**: incluir la nota explícita en §3 del caso degenerado n = 1 (para que el docente lo entienda al auditar).
- **MUST NOT**: imputar tareas a personas distintas del único integrante.
- **MUST NOT**: incluir PII más allá del nombre del único integrante.
- **MUST NOT**: modificar otros archivos del repo en esta tarea.
- **MUST NOT**: alterar §4 de la plantilla.
- **MUST**: revisión humana antes de pushear al branch `release/2.0.0`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Plantilla | `APORTES_TEMPLATE.md` | PR-APORTES-001 | `docs-agent` | `docs/aportes/release-2.0.0.md` v1.0 |
| AGENTS | `AGENTS.md §16 L489` | PR-APORTES-001 | `docs-agent` | checklist `[x]` post-generación |
| Roadmap | `docs/roadmap.md §5 L-09` | PR-APORTES-001 | `docs-agent` | Cumplimiento de la regla de aporte individual |
| Rúbrica del Módulo 4 | §Ajuste por aporte individual | PR-APORTES-001 | `docs-agent` | Aplicación trivial de la fórmula con n = 1 |
| Repo EduSync | 39 prompts + 6 ADRs + 10 diagramas + DTI + BRD v1/v2 + MRD + PRD + FSD + LFSD + ARQ hex + DTOs + roadmap + 2 POCs + 9 skills propios + 1 rule | PR-APORTES-001 | `docs-agent` | §1 Tabla de tareas (>= 50 filas) |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: `INPUTS` con `n_integrantes: 1`, `integrantes: [{nombre: Rodrigo Aspeti}]`, `release: release/2.0.0`, `fecha_cierre: 28/05/2026`, `nota_grupal: null`.
- **Output esperado**: `docs/aportes/release-2.0.0.md` con §1 de ≥ 50 filas (todas con `Integrante = Rodrigo Aspeti`), §2 con una sola fila más total, §3 con `factor = 1.00` y nota del caso n = 1, §4 literal, §6 con 5 `[x]` y 1 `[ ]`.

### 6.2 Caso borde
- **Input**: `nota_grupal: 95`.
- **Output esperado**: §3 incluye la columna `nota_individual` con el valor `95.0` para Rodrigo Aspeti (95 × 1.00); todas las demás invariantes idénticas al caso feliz.

### 6.3 Caso adversarial
- **Input**: `INPUTS.n_integrantes: 1` pero `integrantes: [{nombre: Rodrigo Aspeti}, {nombre: <Otro>}]` (inconsistencia entre conteo y lista).
- **Comportamiento esperado**: rechazo con `E_N_INTEGRANTES_NO_VALIDO`; no se genera el archivo.

### 6.4 Caso adversarial bis
- **Input**: el modelo intenta reportar `factor = 1.10` "por excelencia individual".
- **Comportamiento esperado**: rechazo con `E_FACTOR_NO_UNITARIO_N1`; el modelo debe corregir a 1.00 (la fórmula no admite atajos).

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`, `hallucination_rate`, `n_filas_§1`, `n_categorias_cubiertas`.
- Eventos mínimos: `prompt.started`, `inputs.validated`, `inventory.built`, `formula.applied`, `aportes.generated`, `prompt.completed`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 28/05/2026 | Rodrigo Aspeti | Creación desde contrato para grupo unipersonal (n = 1) | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado y ejecutado en el mismo flujo; `docs/aportes/release-2.0.0.md` v1.0 generado |
