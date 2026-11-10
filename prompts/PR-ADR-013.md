# PR-ADR-013 — PrimeNG como única librería de componentes del frontend

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-013` |
| Título | ADR-0026, PrimeNG (línea 21.x) como única librería de UI |
| Artefacto origen | `docs/adr/0015-adopcion-design-system-visual-edusync.md`, `docs/design/DD-UC-018.md`, `docs/adr/0008-actualizacion-stack-java25-springboot4-angular21.md` |
| ID origen | `DD-UC-032`, `ADR-0026` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.2 |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado** |

## 1. Anatomía

### Role

Eres el arquitecto de EduSync. Registras una decisión de presentación costosa de revertir, sin tocar el baseline ni el dominio.

### Task

Redactar `ADR-0026`: adoptar PrimeNG (línea 21.x) como única librería de UI de terceros, incluida `p-table` para la matriz de registro de notas y los futuros centralizadores. Enmendar `ADR-0015` (deja de aplicar la restricción "sin PrimeNG"). Descartar AG Grid Community.

### Context

- `ADR-0015` eligió tokens propios + `shared/ui/` sin librería. Dejó abierta la Alternativa D si aparecía un componente complejo.
- La matriz de notas (`DD-UC-018`) es una `<table>` con `<input>` por fila: sin teclado entre celdas, sin estado por celda.
- Angular 21 LTS (`ADR-0008`): `primeng` 22.x exige Angular 22 y queda excluido.
- Decisión del responsable (10/11/2026): solo PrimeNG. Una primera propuesta con AG Grid se descartó el mismo día.

### Reasoning

1. Comparar mantener el status quo, solo PrimeNG, PrimeNG + AG Grid y Angular Material.
2. Fijar el reparto: `p-table` para notas/centralizadores; resto de componentes también PrimeNG.
3. Conservar los tokens de `ADR-0015` como fuente de verdad visual (`definePreset`).
4. Declarar invariantes: sin cálculo en el frontend, validación de celda = UX, sin Enterprise/bloques de pago.

### Stop condition

El ADR tiene contexto, alternativas, decisión, consecuencias, impacto, reversión, validación y referencias. Estado **Propuesta** hasta la aprobación humana. No edita `docs/baseline/**`. El archivo se llama `0026-primeng-libreria-de-componentes-frontend.md`.

### Output

`docs/adr/0026-primeng-libreria-de-componentes-frontend.md` + nota de modificación en `docs/adr/0015-*.md`.

### Invariants

- No supersede a `ADR-0015` por completo: los tokens siguen vigentes.
- `primeng` 22.x está prohibido mientras el stack sea Angular 21.
- Sin AG Grid ni otra librería de grillas sin un ADR nuevo.
- Al menos tres alternativas, una de ellas la elegida.

### Failure modes

- `E_AG_GRID_CONSERVADO`: la decisión final mantiene AG Grid. Reescribir.
- `E_PRIMENG_22`: se fija la línea 22.x. Reescribir.
- `E_TOKENS_DESCARTADOS`: se abandonan los tokens de `ADR-0015`. Reescribir.
- `E_BASELINE_TOCADO`: revertir.
