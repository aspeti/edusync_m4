# PR-ADR-012 — El SIE recibe la nota final de periodo y se retira floor()

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-012` |
| Título | ADR-0025, un solo motor `round` y exportación de la nota final de periodo |
| Artefacto origen | `docs/product/FSD.md` §4.1–§4.5, `docs/adr/0013-modelo-generico-periodos-secciones-calculo.md`, `docs/adr/0005-resiliencia-integracion-sie-resilience4j.md` |
| ID origen | `DD-UC-031`, `ADR-0025` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado** |

## 1. Anatomía

### Role

Eres el arquitecto de EduSync. Registras una decisión costosa de revertir sin tocar el baseline.

### Task

Redactar `ADR-0025`: se retira `floor()` del producto vivo, el motor genérico (`round`) es el único, y el SIE recibe una nota final entera por estudiante, materia y periodo, solo si todas las secciones están completas.

### Context

- El Perfil Bolivia SIE (`FSD-UC-001`/`003`/`004`, `BR-003`, `BR-008`, `RB-08`) contradice el modelo genérico ya implementado (`ADR-0013`, `FSD-UC-016`).
- `ADR-0005` fija la resiliencia con la clave `rude + periodo_id`. Con una nota por materia, la clave cambia.
- Decisiones del responsable (10/11/2026): secciones configurables, todo `round`, el SIE solo recibe la nota final de cada trimestre, nota incompleta excluida, inmutabilidad diferida, sin cierre de materia.

### Reasoning

1. Comparar mantener dos motores, enviar también el detalle por sección, y enviar solo la nota final.
2. Declarar explícitamente qué queda diferido y qué riesgo se acepta (periodo reabrible por `ADR-0014`).
3. Enmendar la clave de idempotencia de `ADR-0005` sin reescribir su cuerpo.

### Stop condition

El ADR tiene contexto, alternativas, decisión, consecuencias, impacto, reversión, validación y referencias. Estado **Propuesta** hasta la aprobación humana. No edita `docs/baseline/**`.

### Output

`docs/adr/0025-sie-recibe-nota-final-de-periodo-y-retiro-de-floor.md`.

### Invariants

- No supersede a ningún ADR. Modifica `ADR-0005` §3 y `ADR-0013` §3.4/§3.5 solo mediante nota.
- `BR-004` (RUDE como única clave hacia el SIE) se mantiene.
- Al menos tres alternativas, una de ellas la elegida.

### Failure modes

- `E_FLOOR_REINTRODUCIDO`: la decisión conserva `floor()` en algún punto. Reescribir.
- `E_DIFERIDO_IMPLICITO`: algo diferido no aparece listado en §3.7. Reescribir.
- `E_BASELINE_TOCADO`: revertir.
