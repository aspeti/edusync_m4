---
name: github-issues-edusync
description: >
  Inventariar trabajo pendiente de EduSync (FSD-UC vivos, DTP §A.3, PROMPT_MAPPING,
  ADRs en Propuesta, DoD abiertos de DD-UC) y crear issues/tareas en GitHub con `gh`,
  o un lote Markdown listo para pegar si no hay autenticación. Activar cuando el usuario
  pide "crea issues de lo pendiente", "sube tareas a GitHub", "issues desde el DTP/FSD"
  o "backlog de PR-IMPL pendientes".
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
disable-model-invocation: true
---

# Skill: github-issues-edusync — Issues GitHub desde el backlog vivo

> Paridad: `.cursor/skills/github-issues-edusync/` y `.claude/skills/github-issues-edusync/`.
> No toca `docs/baseline/**`. No inventa alcance fuera de las specs vivas.

## 1. Cuándo activarlo (triggers)

- **DURANTE**: planificación de sprint, triage tras un Design Doc/`ADR`, cierre de `sync-doc-chain` o cuando hay prompts `Aprobado` sin ejecutar.
- **ARRANCA cuando** el usuario pide: "crea issues de lo pendiente", "sube tareas a GitHub", "issues desde FSD/DTP", "backlog de PR-IMPL", "genera issues para `<FSD-UC>`".
- **NO ACTIVAR cuando**: solo quiere implementar código (`feature-design-doc` / `PR-IMPL`); solo quiere un ADR (`adr-edusync`); o pide issues sobre `docs/baseline/**` (rechazar).

## 2. Entradas obligatorias

| Campo | Obligatorio | Default |
|-------|-------------|---------|
| `modo` | sí | `dry-run` (lista Markdown) o `crear` (`gh issue create`) |
| `alcance` | no | `pendiente` = FSD-UC no completos + prompts no ejecutados + ADRs *Propuesta* + diferidos anotados |
| `labels` | no | ver §4.5 |
| `assignee` | no | vacío |
| `repo` | no | remoto `origin` (`gh repo view`) |

Si falta `modo`, preguntar: dry-run o crear. Si `crear` y `gh auth status` falla → STOP con `E_GH_NO_AUTH` y entregar el lote Markdown.

## 3. Fuentes de verdad (orden)

1. `docs/product/DTP.md` §A.3 (estado por FSD-UC) y §A.1 (filas recientes).
2. `docs/product/FSD.md` (UCs vivos; notas *diferido* / *absorbido* / *diseñado*).
3. `docs/PROMPT_MAPPING.md` (filas `Aprobado (prompt)` / ejecución pendiente).
4. `docs/design/DD-UC-*.md` DoD con casillas abiertas.
5. `docs/adr/*.md` con estado **Propuesta**.
6. `docs/roadmap.md` (horizonte, sin inventar hitos).
7. `AGENTS.md` §6/§8 (invariantes; qué no pedir en un issue).
8. Issues abiertos: `gh issue list --state open` (evitar duplicados).

**MUST NOT** leer `docs/baseline/**` como fuente de tareas nuevas.

## 4. Procedimiento

### 4.1 Inventario

Construir una tabla interna con una fila por ítem:

| id | tipo | título corto | origen | bloqueo | prioridad |
|----|------|--------------|--------|---------|-----------|

Tipos: `impl` (PR-IMPL pendiente), `adr` (Propuesta), `fsd-gap` (UC no completo), `deferred` (explícitamente diferido), `chore` (DoD/manual residual, p. ej. prueba ≥ 45 alumnos).

### 4.2 Deduplicar

- Omitir FSD-UC en estado **completo** sin mejora pendiente documentada.
- Omitir ítems ya cubiertos por un issue abierto (match por `FSD-UC-NNN`, `PR-IMPL-NNN`, `ADR-NNNN` o `DD-UC-NNN` en título/body).
- Agrupar "diferidos" (`ADR-0025` §3.7, etc.) en issues de tipo `deferred`, nunca como "implementar ya".

### 4.3 Prioridad sugerida

1. ADR *Propuesta* que bloquea un `PR-IMPL` (p. ej. `ADR-0025` → `PR-IMPL-031`).
2. `PR-IMPL` aprobado pendiente de ejecución.
3. FSD-UC diseñado / parcial sin prompt.
4. Chores de DoD / prueba manual.
5. Diferidos (cola baja).

### 4.4 Cuerpo del issue (plantilla)

```markdown
## Resumen
<1-2 frases del gap real>

## Trazabilidad
- FSD: `FSD-UC-NNN` (`docs/product/FSD.md`)
- Design Doc: `DD-UC-NNN` (si existe)
- Prompt: `PR-IMPL-NNN` / `PR-ADR-NNN` (si existe)
- ADR: `ADR-NNNN` (estado)

## Alcance
- [ ] <bullet concreto>
- Fuera: <qué NO hacer>

## Criterio de hecho
- [ ] <verificación: mvn test / ng build / DoD del DD>
- [ ] Sin tocar `docs/baseline/**`

## Notas
Bloqueado por: <ADR/PR-IMPL o "ninguno">
```

### 4.5 Labels

| Label | Uso |
|-------|-----|
| `type:impl` | Ejecutar PR-IMPL / código |
| `type:adr` | Aprobar o cerrar ADR |
| `type:docs` | Solo documentación |
| `type:deferred` | Trabajo diferido a propósito |
| `area:backend` / `area:frontend` / `area:notassie` | Según módulo |
| `blocked` | Espera ADR u otro issue |

Crear labels faltantes solo con confirmación del usuario (`gh label create`).

### 4.6 Publicar

**dry-run:** mostrar la tabla + bodies completos; no llamar a `gh issue create`.

**crear:** por cada ítem aprobado por el usuario (o el lote completo si dijo "crea todos"):

```bash
gh issue create --title "<tipo>: <título>" --body "$(cat <<'EOF'
…
EOF
)" --label "type:impl" --label "area:backend"
```

Límite: máx. **15 issues por corrida** salvo que el usuario pida más. Si el inventario supera 15, priorizar §4.3 y listar el resto como backlog Markdown.

### 4.7 Reporte final

Tabla: `#issue | título | labels | origen`. Enlace al filtro de issues creados.

## 5. Salida esperada

- Tabla de inventario (siempre).
- Issues creados **o** archivo/lote Markdown (si dry-run o sin auth).
- Trazabilidad por issue:

| Origen | Issue | Verificación al cerrar |
|--------|-------|------------------------|
| `FSD-UC-004` + `PR-IMPL-031` | `#N Exportación SIE…` | DoD `DD-UC-031` + `mvn test` |
| `ADR-0025` Propuesta | `#N Aprobar ADR-0025` | Estado Aceptada en el ADR |

## 6. Verificación

- [ ] Ningún issue pide editar `docs/baseline/**`.
- [ ] Ningún issue pide `floor()`, secretos en código, o cálculo de notas fuera de `CalculoNotas`.
- [ ] Cada issue de impl cita `FSD-UC`/`DD-UC`/`PR-IMPL` reales.
- [ ] Diferidos marcados `type:deferred`, no mezclados con el sprint activo.
- [ ] Sin duplicar issues abiertos existentes.
- [ ] `modo=crear` solo tras `gh auth status` OK.

## 7. Anti-patrones

| Anti-patrón | Por qué | Mitigación |
|-------------|---------|------------|
| Issue "implementar todo el SIE" sin `DD-UC-031` | Alcance inventado | Exigir trazabilidad §4.4 |
| Issue que reintroduce `floor()` / Perfil Bolivia fijas | Viola `ADR-0025`/`ADR-0013` | Citar ADR vigente en el body |
| Crear issues de ítems ya **completos** en DTP §A.3 | Ruido | Filtrar en §4.2 |
| `gh issue create` sin auth / sin dry-run previo | Fallos y spam | Default dry-run; `E_GH_NO_AUTH` |
| Incluir PII/RUDE de ejemplos reales en el body | `AGENTS.md` §7 | Solo IDs sintéticos o ninguno |

## 8. Mini ejemplo

> "Usa `github-issues-edusync` en modo dry-run: issues para lo pendiente de exportación SIE (`FSD-UC-004` / `PR-IMPL-031`) y para `ADR-0025`."

## 9. Modos de fallo

- **`E_GH_NO_AUTH`** — `gh auth status` falla → entregar Markdown; indicar `gh auth login`.
- **`E_NO_PENDING`** — inventario vacío tras deduplicar → informar; no crear issues vacíos.
- **`E_BASELINE_SCOPE`** — el usuario pide issues desde `docs/baseline/**` → STOP.
- **`E_TOO_MANY`** — >15 sin confirmación → crear el top 15 y dejar el resto en Markdown.
- **`E_DUPLICATE`** — match con issue abierto → omitir y citar el `#` existente.

## 10. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 10/11/2026 | G-EduSync | versión inicial: inventario DTP/FSD/PROMPT_MAPPING/ADR → dry-run o `gh issue create` | `docs/product/FSD.md` v2.17, `DTP.md` v1.58, `PROMPT_MAPPING` v2.56, `AGENTS.md` v0.54 |
