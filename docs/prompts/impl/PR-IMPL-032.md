# PR-IMPL-032 — Frontend: base de PrimeNG y matriz de calificaciones con `p-table`

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-IMPL-032` |
| Título | Instalar PrimeNG 21.x, tema EduSync y migrar la matriz de calificaciones a `p-table` |
| Artefacto origen | `docs/design/DD-UC-032.md`, `docs/adr/0026-primeng-libreria-de-componentes-frontend.md` |
| ID origen | `DD-UC-032`, `ADR-0026`, `ADR-0015`, `ADR-0008`, `FSD-UC-016`, `DD-UC-018` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aprobado (prompt)** — ejecutar solo cuando `ADR-0026` esté *Aceptada* |

> No ejecutar este prompt como parte del diseño. La implementación es un turno aparte. Preferible ejecutarlo **antes** del paso de frontend de `PR-IMPL-031` (`DD-UC-032` §8 punto 5).

## 1. Task

Implementar `docs/design/DD-UC-032.md` §2: instalar PrimeNG (línea 21.x) + `@angular/cdk` + `@primeuix/themes`, crear el tema EduSync a partir de los tokens de `ADR-0015`, migrar `evaluacion-calificaciones.page.ts` a `p-table` y añadir `calificaciones-grid.util.ts` con tests.

## 2. Stop condition

`ng build` en verde. Tests de `calificaciones-grid.util` en verde (o limitación de entorno Vitest sin navegador documentada, igual que `DD-UC-004`). Ningún hexadecimales de color fuera de `_tokens.scss`. Ningún `rude` ni nota en `console.log`. Sin cambios en `backend/`, `core/auth/`, `shared/ui/` ni contratos REST. La prueba manual con ≥ 45 estudiantes (§6 del design doc) debe quedar anotada como pendiente del humano si no se puede hacer en el turno.

## 3. Anatomía

### Role

Eres un Senior Frontend Engineer con experiencia en Angular 21 (standalone, signals) y en tematizar PrimeNG 21.x con `@primeuix/themes` (`definePreset`) sobre design tokens CSS. No inventas librerías de grillas adicionales.

### Task

Implementa en este orden:

1. Instalar `primeng` línea **21.x** (`^21.1`), `@angular/cdk` `^21` y `@primeuix/themes` (verificar con `npm ls` si viene transitivo; si no, instalarlo). **MUST NOT** instalar `primeng` 22.x ni AG Grid. Anotar las versiones exactas en `package.json`.
2. Crear `frontend/src/app/core/ui/edusync-theme.ts` con `definePreset` que mapea los tokens semánticos a `var(--…)` de `_tokens.scss`. Si hace falta una escala `50…950` del primario, declarar esas variables en `_tokens.scss` y referenciarlas; nunca pegar hexadecimales en el TypeScript.
3. Registrar `providePrimeNG({ theme: … })` en `app.config.ts`. Modo oscuro desactivado.
4. Crear `calificaciones-grid.util.ts` con `estadoDeCelda` y `calcularCambios` (firmas de `DD-UC-032` §2.4) y su `*.spec.ts` cubriendo los casos de §6.
5. Reescribir `evaluacion-calificaciones.page.ts` según `DD-UC-032` §2.3: `p-table` + `p-inputnumber` (campo siempre visible), estados por celda, `Tab`/`Enter` entre filas, `Guardar` solo con celdas `MODIFICADA` y sin `INVALIDA`, envío solo de lo modificado, `p-message` para errores del servidor, resumen provisional con `p-table` simple. Misma ruta, mismos endpoints.
6. Actualizar `AGENTS.md` §4: la fila de PrimeNG deja de decir «aún no está en el `package.json`» y refleja las versiones fijadas.
7. Al cerrar: `dtp-sync`, DoD de `DD-UC-032` y este prompt a «Ejecutado».

### Context

- Documentos: `DD-UC-032`, `ADR-0026` (única librería de UI), `ADR-0015` (tokens), `ADR-0008` (Angular 21), `DD-UC-018` (contrato actual).
- Stack: Angular 21.2.x LTS, standalone components. Sin backend.
- Restricciones:
  - Ningún cálculo de notas en el frontend. La provisional viene de `GET .../nota-provisional`.
  - Validación de rango en celda = ayuda de UX; el servidor es la autoridad (`E_RANGO_INVALIDO`, `E_PERIODO_NO_ABIERTO`).
  - Identificación por `estudianteId`. El `rude` se muestra y no se loguea.
  - Contrato `PUT /evaluaciones/{id}/calificaciones` con `items[]` sin cambios.
  - `login.page.ts` y `shared/ui/` **no se tocan**.
  - Sin `primeicons` salvo que un componente lo exija de forma dura; si se añaden, documentarlo.
  - Sin tocar `docs/baseline/**`.

### Reasoning

1. Verificar peerDependencies de `primeng@21` contra el Angular del `package.json` antes de fijar versiones.
2. Escribir primero los tests de `calificaciones-grid.util`.
3. Confirmar que `providePrimeNG` no altera el aspecto de pantallas no migradas.
4. Comprobar en la pestaña de red (prueba manual) que Guardar con 3 cambios envía exactamente 3 ítems.
5. Si `p-inputnumber` con locale boliviano no interpreta `57,5` como `57.5`, fijar el formato explícitamente (`DD-UC-032` §8).

### Output

`frontend/package.json` (+ lockfile), `core/ui/edusync-theme.ts`, delta `app.config.ts`, `calificaciones-grid.util.ts` + spec, `evaluacion-calificaciones.page.ts` reescrito, `AGENTS.md` §4 actualizado. Sin cambios de backend.

### Invariants

- PrimeNG es la única librería de UI de terceros. Sin AG Grid ni otra grilla.
- `primeng` 22.x está prohibido mientras el stack sea Angular 21.
- Ningún color/tamaño hardcodeado fuera de `_tokens.scss`.
- Sin cálculo de notas ni PII/`rude`/notas en consola.
- Sin cambios de contrato REST, RBAC ni dominio.

### Failure modes

- `E_PRIMENG_22`: se instaló `primeng` 22.x. Rechazar y fijar 21.x.
- `E_AG_GRID`: aparece `ag-grid-*` en `package.json`. Rechazar.
- `E_CALCULO_EN_FRONTEND`: la tabla calcula promedios o notas. Rechazar.
- `E_COLOR_HARDCODEADO`: hexadecimales de marca fuera de `_tokens.scss`. Rechazar.
- `E_CONTRATO_CAMBIADO`: cambia el body del `PUT` o los endpoints. Rechazar.
- `E_LOGIN_TOCADO`: se modificó `login.page.ts` o `shared/ui/`. Rechazar.
- `E_BASELINE_TOCADO`: revertir.

## 4. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 10/11/2026 | G-EduSync | Prompt aprobado. Ejecución pendiente de `ADR-0026` *Aceptada* |
