---
name: feature-design-doc
description: Genera un documento de diseño (design doc) estandarizado para un feature de la fase de implementación, a partir de uno o más casos de uso del FSD (FSD-UC), con trazabilidad explícita al FSD y registro del prompt asociado en PROMPT_MAPPING. Úsalo cuando se pida crear/actualizar el design doc de un feature de EduSync, diseñar la implementación de un FSD-UC, o documentar el "cómo" de algo que se va a construir en el código durante release/3.0.0 en adelante.
disable-model-invocation: true
---

# Feature Design Doc (EduSync)

Crea un **design doc por feature** (`DD-UC-NNN`) trazable al FSD vivo, siguiendo el modelo documental de implementación de EduSync. Un FSD-UC puede tener **uno o más** design docs. El design doc describe el *cómo*; complementa (no reemplaza) a los ADR.

## Invocación

```
@feature-design-doc <FSD-UC-NNN[,FSD-UC-MMM]> [titulo="<título>"] [release=release/3.0.0]
```

- `<FSD-UC-...>`: caso(s) de uso del FSD vivo que el feature implementa. **Obligatorio** (sin FSD-UC no hay design doc válido).
- `titulo`: nombre corto del feature. Si se omite, se deriva del nombre del FSD-UC.
- `release`: release vivo objetivo (por defecto `release/3.0.0`, el vigente del repo).

## Archivos de referencia

- Plantilla del design doc: [`plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md`](../../plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md)
- Plantilla de prompt: [`plantillas/PROMPT_TEMPLATE.md`](../../plantillas/PROMPT_TEMPLATE.md)
- Modelo documental y reglas: [`plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md`](../../plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md)
- ADR: [`plantillas/ADR_TEMPLATE.md`](../../plantillas/ADR_TEMPLATE.md)
- Stack vivo vigente: [`docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md`](../../docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md) (Java 25 LTS + Spring Boot 4.1.0 + Angular 21 LTS)

## Principios

- **Trazabilidad obligatoria al FSD**: `fsd_uc` siempre poblado y enlazado al FSD vivo (`docs/product/FSD.md`).
- **No tocar el baseline congelado** (`docs/baseline/**`, ver `.cursor/rules/baseline-congelado.mdc`). Todo cambio de spec va a `docs/product/`.
- **Diseño ≠ decisión**: el *cómo* va aquí; una decisión significativa/costosa de revertir va a un ADR enlazado (siguiente número libre en `docs/adr/`, después de `ADR-0008`).
- **No inventar**: si falta info del FSD-UC, pregúntala antes de redactar.

## Flujo

```
- [ ] Paso 1: Resolver el/los FSD-UC y ubicar el FSD vivo
- [ ] Paso 2: Asignar el ID DD-UC-NNN (siguiente correlativo en docs/design/)
- [ ] Paso 3: Redactar el design doc desde la plantilla
- [ ] Paso 4: Registrar el/los prompt(s) PR-IMPL-NNN
- [ ] Paso 5: Enlazar en PROMPT_MAPPING.md y validar trazabilidad
```

### Paso 1 — Resolver FSD-UC

- Localizar `docs/product/FSD.md` (vivo, modo LFSD ⚡). Si solo existe el FSD en `docs/baseline/FSD_EduSync_vFinal.md`, avisar: hay que crear primero la copia viva en `docs/product/FSD.md` (no editar el baseline).
- Confirmar que cada `FSD-UC` existe; extraer nombre, flujo principal y criterios de aceptación (Gherkin) como insumo.

### Paso 2 — ID correlativo

- Listar `docs/design/DD-UC-*.md` y asignar el siguiente número libre. Un mismo FSD-UC puede tener varios DD si cubre piezas distintas.

### Paso 3 — Redactar

- Copiar [`FEATURE_DESIGN_DOC_TEMPLATE.md`](../../plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md) a `docs/design/DD-UC-NNN.md`.
- Completar frontmatter (`id`, `fsd_uc`, `prd_refs`, `adrs`, `prompts`, `release`, `status`) y las secciones 1–7.
- §4 (impacto en specs vivas): listar qué se actualizará en `docs/product/PRD.md` / `FSD.md` / `DTP.md`. Marcar si algún cambio es **delta vs DTI vFinal** (entonces requiere ADR).
- Si hay decisión significativa, crear/enlazar `ADR-NNNN` (siguiente libre tras `ADR-0008`) con [`ADR_TEMPLATE.md`](../../plantillas/ADR_TEMPLATE.md).

### Paso 4 — Prompt(s)

- Por cada generación asistida por IA del feature, crear `docs/prompts/impl/PR-IMPL-NNN.md` con [`PROMPT_TEMPLATE.md`](../../plantillas/plantillas1/PROMPT_TEMPLATE.md) (6 elementos + invariantes + trazabilidad al `FSD-UC`). **A diferencia** de los `PR-<AREA>-NNN.md` existentes en `prompts/` (convención plana de M4), el área `IMPL` es la única que vive en `docs/prompts/impl/`, siguiendo [`FEATURE_DESIGN_DOC_TEMPLATE.md`](../../plantillas/plantillas3/FEATURE_DESIGN_DOC_TEMPLATE.md) §5.

### Paso 5 — Enlazar y validar

- Añadir el prompt al área `IMPL` de `docs/PROMPT_MAPPING.md` (índice + trazabilidad requerimiento → prompt → artefacto).
- Validar la cadena: `FSD-UC → DD-UC → PR-IMPL → (artefacto)`. Reportar gaps.

## Salida (reporte en el chat)

- Ruta del design doc creado y su `DD-UC-NNN`.
- FSD-UC cubiertos y enlaces.
- Prompts registrados (`PR-IMPL-*`) y si se creó/enlazó algún ADR.
- Deltas vs DTI vFinal detectados (si los hay) y recordatorio de correr `@dtp-sync` tras implementar.
