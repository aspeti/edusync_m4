---
name: dtp-sync
description: A partir de un diff o Pull Request de implementación, propone actualizaciones al Documento Técnico del Producto (DTP) de EduSync, a las specs vivas (PRD/FSD en docs/product) y a su changelog; crea un ADR cuando detecta una decisión significativa; valida la trazabilidad código → FSD-UC → design doc → prompt; y nunca modifica el baseline congelado de M4. Úsalo cuando se implemente un feature de EduSync y haya que sincronizar la documentación viva con el código, consolidar el DTP, o auditar la trazabilidad de un PR.
disable-model-invocation: true
---

# DTP Sync (EduSync)

Mantiene **viva y coherente** la documentación del producto EduSync tras implementar código, sin tocar el baseline congelado de M4 (`docs/baseline/`). Es el guardián de la regla de oro: **cero divergencia silenciosa**.

## Invocación

```
@dtp-sync [diff=<rango git, p.ej. release/2.0.0..HEAD>] [pr=<#id>] [uc=<FSD-UC-NNN>]
```

- `diff`: rango git a analizar. Si se omite, usa los cambios no commiteados + el último PR/commit.
- `pr`: id del PR cuya descripción declara prompts/archivos.
- `uc`: acota el análisis a un caso de uso.

## Archivos de referencia

- Plantilla del DTP: [`plantillas/plantillas3/DTP_TEMPLATE.md`](../../plantillas/plantillas3/DTP_TEMPLATE.md)
- Design doc: [`plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md`](../../plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md)
- ADR: [`plantillas/ADR_TEMPLATE.md`](../../plantillas/ADR_TEMPLATE.md)
- Modelo documental y reglas: [`plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../../plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md)
- DTP vigente: [`docs/product/DTP.md`](../../docs/product/DTP.md)
- Stack vivo vigente: [`docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md`](../../docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md)

## Principios (no negociables)

- **NUNCA editar `docs/baseline/**`** (baseline congelado de M4, tag `release/2.0.0`; ver `.cursor/rules/baseline-congelado.mdc`). Si un cambio "necesita" tocarlo, es señal de que debe ir a `docs/product/` + DTP.
- **ADR-first**: si el diff implica una decisión significativa o un delta vs el DTI vFinal (`docs/baseline/DTI.md`), el ADR se crea/actualiza **antes** de dar por buena la implementación (siguiente número libre tras `ADR-0008`).
- **Propón, no impongas**: presenta los cambios documentales como propuesta editable; el humano firma.
- **Trazabilidad o gap**: todo cambio de código debe ligarse a `FSD-UC + DD-UC + PR-IMPL`. Lo que no se pueda ligar se reporta como gap.

## Flujo

```
- [ ] Paso 1: Leer el diff y la descripción del PR
- [ ] Paso 2: Mapear cambios de código a FSD-UC / DD-UC / prompts
- [ ] Paso 3: Detectar deltas vs DTI vFinal y decisiones -> ADR
- [ ] Paso 4: Proponer updates a specs vivas + DTP (changelog, estado, deltas)
- [ ] Paso 5: Verificar que el baseline está intacto y reportar
```

### Paso 1 — Analizar el diff

- Obtener archivos cambiados (`git diff --name-status <rango>`).
- Leer la descripción del PR: prompts declarados, archivos generados vs editados a mano. Si la edición manual supera ~30 % del diff, marcarlo como candidato a **ADR de aprendizaje**.

### Paso 2 — Mapear a la cadena de trazabilidad

- Para cada cambio relevante de `src/`, `tests/`, `migrations/`, identificar el `FSD-UC` y el `DD-UC` correspondiente (en `docs/design/`).
- Verificar que existe el/los `PR-IMPL-NNN` en `prompts/` y en `docs/PROMPT_MAPPING.md` (área `IMPL`). Si falta el design doc, sugerir correr `@feature-design-doc`.

### Paso 3 — Deltas y ADRs

- Comparar lo implementado contra el **DTI vFinal congelado** (`docs/baseline/DTI.md`). Cualquier diferencia deliberada es un **delta** (§A.2 del DTP) y exige un ADR.
- Detectar decisiones significativas (estilo, persistencia, mensajería, seguridad, costo, stack). Crear/enlazar `ADR-NNNN` con [`ADR_TEMPLATE.md`](../../plantillas/ADR_TEMPLATE.md).

### Paso 4 — Proponer updates a la capa viva

- `docs/product/PRD.md` / `docs/product/FSD.md`: ajustar criterios/flujos que cambiaron (FSD en modo LFSD ⚡).
- `docs/product/DTP.md`:
  - §A.1 Changelog de implementación (fila nueva con FSD-UC/DD/ADR/PR).
  - §A.2 Deltas vs DTI vFinal (si aplica).
  - §A.3 Estado de implementación por FSD-UC.
  - §A.4 / §B: actualizar solo las secciones técnicas que cambiaron.
- Ampliar `docs/PROMPT_MAPPING.md` (área `IMPL`) con los `PR-IMPL-*` nuevos.

### Paso 5 — Verificación

- Confirmar que **ningún** archivo de `docs/baseline/` aparece en el diff. Si aparece, **bloquear** y proponer mover el cambio a `docs/product/`.
- Reportar la cadena por feature y los gaps.

## Salida (reporte en el chat)

- Tabla `archivo cambiado → FSD-UC → DD-UC → PR-IMPL → (ADR)`.
- Propuestas concretas de edición para `docs/product/*` y `docs/product/DTP.md` (con el texto sugerido).
- ADRs creados/necesarios.
- Gaps de trazabilidad y, sobre todo, **alerta si el baseline fue tocado**.
