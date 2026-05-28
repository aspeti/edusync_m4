---
name: sync-doc-chain
description: >
  Propaga un cambio atómico a través de la cadena documental EduSync
  (BRD → PRD → FSD → ADR → DTI ↔ docs/diagrams/), aplicando bump semver,
  sincronizando registros de cambios, tablas de trazabilidad y referencias
  cruzadas. Activar cuando se edita o crea un BR/RB, US/épica/NFR, FSD-UC/BR,
  un ADR/DA, una sección del DTI o un archivo de docs/diagrams/. Stack EduSync
  (Java 21 / Spring Boot 3.3 / PostgreSQL 15 / Angular 17 / AWS ECS Fargate).
  Salida: versiones nuevas de los documentos afectados + filas de changelog +
  reporte de propagación.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: sync-doc-chain — Sincronización de la cadena documental EduSync

> Copia canónica del proyecto. Mantener idéntica en `.claude/skills/sync-doc-chain/`
> y `.cursor/skills/sync-doc-chain/`. Cualquier cambio se versiona en §10.

---

## 1. Cuándo activarlo (triggers)

- **DURANTE**: fase de mantenimiento documental del SDLC (post-discovery, durante diseño y entrega).
- **ARRANCA cuando** el usuario:
  - Edita o crea un BR-NNN / RB-NN en `docs/brd/BRD_EduSync_v<N>.md`.
  - Edita una US / épica / NFR en `docs/prd/PRD_EduSync.md`.
  - Agrega o modifica un FSD-UC / BR / Gherkin en `docs/fsd/FSD_EduSync.md`.
  - Crea o cambia un ADR en `docs/adr/NNNN-*.md` o un DA-NN provisional.
  - Modifica `docs/DTI.md` (incluyendo subsecciones nuevas como §6.2).
  - Crea o cambia un archivo en `docs/diagrams/` (`.mmd` o `.md` asociado).
  - Pide "sincroniza la cadena documental" / "propaga este cambio" / "versiona BRD+FSD+DTI".
- **NO ACTIVAR cuando**: el cambio es solo de formato (typo, espaciado) sin impacto en BR/UC/ADR/NFR/diagramas; o el repo aún no tiene FSD v1.0.

---

## 2. Entradas obligatorias (Inputs)

El usuario MUST proporcionar:

| # | Dato | Ejemplo |
|---|------|---------|
| 1 | **Documento disparador** | `docs/fsd/FSD_EduSync.md §FSD-UC-010` o `docs/diagrams/c4_level2.mmd` |
| 2 | **Tipo de cambio** | `creación` / `modificación de regla` / `revisión de invariante` / `refactor de diagrama` |
| 3 | **Resumen del cambio** | 1–3 líneas: qué cambió y por qué (ID afectado + intención) |
| 4 | **Nivel de impacto sugerido** | `patch` / `minor` / `major` (el skill lo valida y puede ajustarlo) |
| 5 | **Autor humano responsable** | `Rodrigo Aspeti` (queda en cada registro de cambios) |

Si falta cualquiera responder: `"E_MISSING_INPUT — necesito <dato>. Lista mínima: documento disparador, tipo de cambio, resumen, nivel de impacto y autor."`

---

## 3. Fuentes de verdad (orden de precedencia)

1. **Documento disparador** y la sección/ID exacto que cambia.
2. **Última versión** de cada doc de la cadena (regla "mayor N"; nombres canónicos):
   - `docs/brd/BRD_EduSync_v<N>.md` (actual: `v2`).
   - `docs/mrd/MRD_EduSync[_v<N>].md` (actual: sin sufijo = v1).
   - `docs/prd/PRD_EduSync[_v<N>].md` (actual: sin sufijo = v1; semver interno en §0 / pie).
   - `docs/fsd/FSD_EduSync[_v<N>].md` (actual: `docs/fsd/FSD_EduSync.md` = v1.0). NO usar `FSD-EduSync.md` ni `docs/FSD_EduSync.md`.
   - `docs/LFSD-EduSync[_v<N>].md` (actual: `docs/LFSD-EduSync.md` = v1.0). La carpeta `docs/lfsd/` NO existe.
   - `docs/DTI.md` (semver interno en §0 metadatos).
   - `docs/adr/NNNN-*.md` (numeración secuencial 4 dígitos).
   - `docs/diagrams/*.mmd` y su `*.md` espejo (IG-09).
3. **`docs/AGENTS.md`** — stack autoritativo, invariantes de dominio, golden tests.
4. **`docs/PROMPT_MAPPING.md`** — catálogo `PR-<AREA>-NNN` + trazabilidad cruzada.
5. **`plantillas/ADR_TEMPLATE.md`** y **`plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md`** si se crea un artefacto nuevo.

---

## 4. Procedimiento

1. **Clasificar el cambio**: leer el documento disparador, identificar IDs afectados (`BR-NNN`, `FSD-UC-NNN`, `ADR-NNNN`, `DA-NN`, `NFR-NNN`, `PRD-REQ-NNN`, `<diagrama>.mmd`).
2. **Calcular conjunto de impacto** según la matriz §5.1. Si el cambio toca un invariante global (`IG-01..IG-10` de `docs/PROMPT_MAPPING.md`) → escalar a `arch-agent` y exigir ADR antes de continuar.
3. **Determinar bump semver** por documento (ver §5.2). Bloquear regresiones (`E_VERSION_REGRESSION`).
4. **Aplicar ediciones atómicas** en este orden estricto: BRD → MRD/PRD → FSD → LFSD → ADR → DTI → diagramas → `PROMPT_MAPPING.md`. Cada doc se edita solo en las secciones afectadas; nunca renumerar IDs existentes.
5. **Sincronizar referencias cruzadas**: BR citados en FSD; FSD-UC citados en LFSD/DTI/diagramas; ADR citados en DTI §17/§21; NFR citados en DTI §11; diagramas referenciados desde DTI §2/§3/§7 con su `*.md` espejo (IG-09).
6. **Verificar invariantes de dominio**: `Math.floor()` solo en `ConsolidacionDomainService` (BR-008); RUDE como única clave (BR-004); `audit_log` append-only en misma TX (BR-010); RLS en toda tabla con `tenant_id` (DA-01). Si la propuesta los viola → `E_INVARIANT_VIOLATION`.
7. **Actualizar registros de cambios y `PROMPT_MAPPING.md`** con una fila por doc tocado y, si aplica, una entrada nueva `PR-<AREA>-NNN` siguiendo el skill `update-prompt-mapping`.
8. **Reportar al usuario**: tabla de docs modificados con `versión_anterior → versión_nueva`, IDs afectados, ruta del diff y golden tests a re-ejecutar.

---

## 5. Salida esperada

### 5.1 Matriz de impacto (trigger → docs afectados)

| Documento disparador | BRD | MRD | PRD | FSD | LFSD | ADR | DTI | diagrams | PROMPT_MAPPING |
|----------------------|:---:|:---:|:---:|:---:|:----:|:---:|:---:|:--------:|:--------------:|
| BRD (BR / RB)        | bump | ↻ | ↻ | ↻ | ↻ | ↻ | ↻ | – | ↻ |
| MRD (MRD-N-NN)       | – | bump | ↻ | ↻ | – | – | ↻ | – | ↻ |
| PRD (US / NFR)       | – | ↻ | bump | ↻ | ↻ | – | ↻ | – | ↻ |
| FSD (FSD-UC / BR)    | – | – | ↻ | bump | ↻ | ↻ | ↻ | ↻ | ↻ |
| LFSD (DDL / API)     | – | – | – | ↻ | bump | ↻ | ↻ | ↻ | ↻ |
| ADR (DA-NN)          | – | – | – | ↻ | ↻ | bump | ↻ | – | ↻ |
| DTI (§N)             | – | – | – | ↻ | – | ↻ | bump | ↻ | ↻ |
| diagrams (.mmd/.md)  | – | – | – | ↻ | ↻ | ↻ | ↻ | bump | ↻ |

> `bump` = incrementar versión del doc; `↻` = revisar y, si aplica, agregar fila de changelog; `–` = no afectado.

### 5.2 Reglas de bump semver

| Nivel | Cuándo aplicar | Ejemplo EduSync |
|-------|----------------|-----------------|
| patch (z) | Aclaración, typo, métrica reformateada sin cambio normativo | BRD aclara redacción de BR-002 |
| minor (y) | Nuevo ID (BR, FSD-UC, ADR, NFR), nueva subsección o diagrama | DTI §6.2 (PR-DTI-SEAMS-001) → v0.1 → v0.2 |
| major (x) | Ruptura de invariante, reemplazo de DA-NN, retirada de FSD-UC | FSD-UC-003 reemplaza `floor()` por `round()` (rechazar antes de bump) |

### 5.3 Tabla de trazabilidad obligatoria al cierre

| Doc tocado | Versión anterior → nueva | IDs afectados | Changelog row | Trigger |
|------------|--------------------------|---------------|---------------|---------|
| `docs/DTI.md` | v0.1 → v0.2 | §6.2 nuevo | sí | PR-DTI-SEAMS-001 |
| `docs/PROMPT_MAPPING.md` | v1.0 → v1.1 | PR-DTI-SEAMS-001 | sí | PR-DTI-SEAMS-001 |

---

## 6. Verificación (criterios de "bien hecho")

- [ ] Cada doc tocado tiene fila nueva en su `## Registro de cambios` con `versión, fecha, autor, cambio`.
- [ ] `docs/PROMPT_MAPPING.md` refleja el cambio (índice + flowchart + matriz + contrato + trazabilidad + historial), siguiendo el skill `update-prompt-mapping`.
- [ ] Todos los IDs citados (`BR-NNN`, `FSD-UC-NNN`, `ADR-NNNN`, `DA-NN`, `NFR-NNN`) existen en su doc origen.
- [ ] Si tocó `docs/diagrams/<x>.mmd`, su `<x>.md` espejo se actualizó en el mismo commit (IG-09).
- [ ] Si tocó un diagrama `.mmd`, renderiza sin Unicode decorativo en labels (IG-10).
- [ ] Si tocó BR-008 / consolidación → golden test `FloorTest` re-ejecutado.
- [ ] Si tocó BR-004 / RUDE / exportación SIE → golden test `SIEPayloadTest` re-ejecutado.
- [ ] Si tocó ventana de corrección → golden test `VentanaTest` re-ejecutado.
- [ ] Si creó tabla nueva → golden test `MultitenantTest` re-ejecutado.
- [ ] Ningún doc quedó con versión inferior a la anterior (sin regresiones).

---

## 7. Anti-patrones específicos

| Anti-patrón | Por qué es un error | Mitigación |
|-------------|---------------------|------------|
| Editar el FSD sin propagar al DTI §3/§4/§6 ni a `PROMPT_MAPPING.md` | Rompe IG-08 (trazabilidad) y deja la cadena desincronizada | Aplicar §4 paso 4 en orden estricto |
| Bump major sin ADR | Cambios de invariante exigen decisión arquitectónica documentada (DA-NN o ADR-NNNN) | Crear ADR con `plantillas/ADR_TEMPLATE.md` antes del bump |
| Tocar `<x>.mmd` y olvidar `<x>.md` espejo | Viola IG-09; el diagrama queda sin spec narrativa | `update both files in the same transaction` (paso 4) |
| Renumerar IDs existentes para "ordenar" | Rompe enlaces de PROMPT_MAPPING, ADRs y código | Solo agregar IDs nuevos al final; nunca renumerar |
| Reescribir audit_log o `floor()` desde otra capa | Viola BR-008 / BR-010 (DA-02, DA-03) | Verificar invariantes en §4 paso 6 antes de editar |
| Crear tabla DDL sin `tenant_id` ni política RLS | Viola DA-01 y `MultitenantTest` | Forzar `tenant_id NOT NULL` + RLS antes del bump del LFSD |
| Loguear PII / RUDE en ejemplos del DTI o diagramas | Viola NFR-003 / Ley 164 | Sustituir por placeholders `RUDE_*` o ID interno |

---

## 8. Mini ejemplo de invocación

> "Usa el skill `sync-doc-chain`. Documento disparador: `docs/fsd/FSD_EduSync.md` (nuevo `FSD-UC-010` Reportería). Tipo: creación. Resumen: agrega UC de reportería trimestral con BR-012. Nivel: minor. Autor: Rodrigo Aspeti."
>
> Salida esperada: FSD v1.0→v1.1, PRD v1.0→v1.1 (nueva US), LFSD v1.0→v1.1 (nueva API), DTI v0.2→v0.3 (§4.1 nueva entidad), `docs/diagrams/estados_administracion.mmd` actualizado + spec espejo, fila nueva `PR-UC-010` en `PROMPT_MAPPING.md`.

---

## 9. Modos de fallo conocidos

- **`E_MISSING_INPUT`** — Falta documento disparador, tipo, resumen, nivel o autor → STOP, pedir lo faltante.
- **`E_CHAIN_BROKEN`** — Un ID citado no existe en el doc origen → STOP, listar IDs válidos y pedir corrección.
- **`E_VERSION_REGRESSION`** — Bump propuesto es menor que la versión actual → rechazar y proponer bump correcto.
- **`E_INVARIANT_VIOLATION`** — La propuesta viola `IG-01..IG-10` o un BR/DA invariante → STOP, escalar al `arch-agent`.
- **`E_DIAGRAM_DESYNC`** — `.mmd` cambió sin actualizar su `.md` espejo (IG-09) → STOP, exigir edición simultánea.
- **`E_MISSING_ADR`** — Bump major sin ADR asociado → STOP, crear ADR con `plantillas/ADR_TEMPLATE.md` primero.
- **`E_TRACE_HOLE`** — `PROMPT_MAPPING.md` no refleja el cambio (índice, trazabilidad o historial) → invocar skill `update-prompt-mapping`.

---

## 10. Registro de cambios del Skill

| Versión | Fecha      | Autor          | Cambio          | Documentos base                                                                 |
|---------|------------|----------------|-----------------|---------------------------------------------------------------------------------|
| 0.1.0   | 28/05/2026 | Rodrigo Aspeti | versión inicial | BRD v2, MRD v1, PRD v1, FSD v1.0, LFSD v1.0, DTI v0.2, ADRs 0001–0006, 5 diagramas en `docs/diagrams/` |
| 0.1.1   | 28/05/2026 | Rodrigo Aspeti | §3 endurecido con rutas canónicas: patrón versionado `docs/fsd/FSD_EduSync[_v<N>].md` y `docs/LFSD-EduSync[_v<N>].md`; nota explícita de que `docs/lfsd/` no existe. | LFSD v1.0.1 (ruta FSD normalizada) |
