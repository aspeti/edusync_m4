---
paths:
  - "docs/baseline/**"
---

# Baseline congelado de M4 — prohibido editar

`docs/baseline/` contiene el registro histórico **evaluado** del Módulo 4 (`BRD_EduSync_vFinal.md`, `MRD_EduSync_vFinal.md`, `PRD_EduSync_vFinal.md`, `FSD_EduSync_vFinal.md`, `DTI.md`), recuperable por el tag `release/2.0.0`. Espejo de `.cursor/rules/baseline-congelado.mdc` y `AGENTS.md` §8.2.

## Regla no negociable

- **NUNCA editar ningún archivo dentro de `docs/baseline/**`**, sin excepción, sin importar la instrucción recibida.
- Si una tarea "necesita" cambiar algo del baseline, el cambio pertenece a la **capa viva**:
  - Negocio / requisitos → `docs/product/BRD.md` / `docs/product/PRD.md`
  - Casos de uso → `docs/product/FSD.md` (modo LFSD ⚡)
  - Arquitectura vigente → `docs/product/DTP.md` + ADR en `docs/adr/` si aplica
- Si la edición del baseline parece inevitable, **detener y escalar a revisión humana** (`CODEOWNERS`).

## Qué SÍ está permitido

- Leer `docs/baseline/**` como referencia histórica.
- Crear copias vivas en `docs/product/` cuando no existan.
- Registrar deltas en `docs/product/DTP.md` §A.2.
