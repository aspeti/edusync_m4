---
name: feature-design-doc
description: Genera un documento de diseño (design doc) estandarizado para un feature de la fase de implementación, a partir de uno o más casos de uso del FSD (FSD-UC), con trazabilidad explícita al FSD y registro del prompt asociado en PROMPT_MAPPING. Úsalo cuando el docente o un grupo pida crear/actualizar el design doc de un feature, diseñar la implementación de un FSD-UC, o documentar el "cómo" de algo que se va a construir en el código.
disable-model-invocation: true
---

# Feature Design Doc

Crea un **design doc por feature** (`DD-UC-NNN`) trazable al FSD, siguiendo el modelo documental de implementación de M4. Un FSD puede tener **uno o más** design docs. El design doc describe el *cómo*; complementa (no reemplaza) a los ADR.

## Invocación

```
@feature-design-doc <FSD-UC-NNN[,FSD-UC-MMM]> [titulo="<título>"] [release=release/3.0.0]
```

- `<FSD-UC-...>`: caso(s) de uso del FSD vivo que el feature implementa. **Obligatorio** (sin FSD-UC no hay design doc válido).
- `titulo`: nombre corto del feature. Si se omite, se deriva del nombre del FSD-UC.
- `release`: release vivo objetivo (por defecto el vigente del repo).

## Archivos de referencia
- Plantilla del design doc: [`m4/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md`](../../m4/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md)
- Plantilla de prompt: [`m4/plantillas/PROMPT_TEMPLATE.md`](../../m4/plantillas/PROMPT_TEMPLATE.md)
- Modelo documental y reglas: [`m4/docs/MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../../m4/docs/MODELO_DOCUMENTAL_IMPLEMENTACION.md)
- ADR: [`m4/plantillas/ADR_TEMPLATE.md`](../../m4/plantillas/ADR_TEMPLATE.md)

## Principios
- **Trazabilidad obligatoria al FSD**: `fsd_uc` siempre poblado y enlazado.
- **No tocar el baseline congelado** (`docs/baseline/`). Todo cambio de spec va a `docs/product/`.
- **Diseño ≠ decisión**: el *cómo* va aquí; una decisión significativa/costosa de revertir va a un ADR enlazado.
- **No inventar**: si falta info del FSD-UC, pregúntala al docente/grupo antes de redactar.

## Flujo

```
- [ ] Paso 1: Resolver el/los FSD-UC y ubicar el FSD vivo
- [ ] Paso 2: Asignar el ID DD-UC-NNN (siguiente correlativo en docs/design/)
- [ ] Paso 3: Redactar el design doc desde la plantilla
- [ ] Paso 4: Registrar el/los prompt(s) PR-IMPL-NNN
- [ ] Paso 5: Enlazar en PROMPT_MAPPING.md y validar trazabilidad
```

### Paso 1 — Resolver FSD-UC
- Localizar `docs/product/FSD.md` (vivo, modo LFSD). Si solo existe el FSD en `docs/baseline/`, avisar: hay que crear primero la copia viva en `docs/product/` (no editar el baseline).
- Confirmar que cada `FSD-UC` existe; extraer nombre, flujo principal y criterios de aceptación (Gherkin) como insumo.

### Paso 2 — ID correlativo
- Listar `docs/design/DD-UC-*.md` y asignar el siguiente número libre. Un mismo FSD-UC puede tener varios DD si cubre piezas distintas.

### Paso 3 — Redactar
- Copiar [`FEATURE_DESIGN_DOC_TEMPLATE.md`](../../m4/plantillas/FEATURE_DESIGN_DOC_TEMPLATE.md) a `docs/design/DD-UC-NNN.md`.
- Completar frontmatter (`id`, `fsd_uc`, `prd_refs`, `adrs`, `prompts`, `release`, `status`) y las secciones 1–7.
- §4 (impacto en specs vivas): listar qué se actualizará en `docs/product/PRD.md` / `FSD.md` / `DTP.md`. Marcar si algún cambio es **delta vs DTI vFinal** (entonces requiere ADR).
- Si hay decisión significativa, crear/enlazar `ADR-NNNN` con [`ADR_TEMPLATE.md`](../../m4/plantillas/ADR_TEMPLATE.md).

### Paso 4 — Prompt(s)
- Por cada generación asistida por IA del feature, crear `docs/prompts/impl/PR-IMPL-NNN.md` con [`PROMPT_TEMPLATE.md`](../../m4/plantillas/PROMPT_TEMPLATE.md) (6 elementos + invariantes + trazabilidad al `FSD-UC`).

### Paso 5 — Enlazar y validar
- Añadir el prompt a `docs/PROMPT_MAPPING.md` (sección de trazabilidad requerimiento → prompt → artefacto).
- Validar la cadena: `FSD-UC → DD-UC → PR-IMPL → (artefacto)`. Reportar gaps.

## Salida (reporte en el chat)
- Ruta del design doc creado y su `DD-UC-NNN`.
- FSD-UC cubiertos y enlaces.
- Prompts registrados (`PR-IMPL-*`) y si se creó/enlazó algún ADR.
- Deltas vs DTI vFinal detectados (si los hay) y recordatorio de correr `@dtp-sync` tras implementar.
