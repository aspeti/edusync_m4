# Architecture Decision Record (ADR)

## ADR-0027: Rangos de calificación como propiedades de `SeccionEvaluacion`

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0027` |
| Título | Rangos de calificación como propiedades de `SeccionEvaluacion` |
| Fecha | 16/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aceptada** |
| Alcance | Plantilla de secciones de la gestión; UI de Periodos/Secciones; gates al abrir periodo; validación de `CalificacionEvaluacion` |
| Stakeholders consultados | Responsable de producto G-EduSync |

### 1. Contexto

`ADR-0002` decidió parametrizar rangos sin redespliegue con alcance `tenant + periodo`. `DD-UC-029` / `PR-IMPL-029` lo materializaron como aggregate `ParametroPeriodo` (una fila por periodo × sección) y un panel de mín/máx **bajo cada periodo** en la UI.

Eso chocaba con el modelo genérico ya aceptado (`ADR-0013`): la plantilla de `SeccionEvaluacion` es **única por gestión** y aplica a todos los periodos. El Admin veía “configuración de secciones” repetida por periodo, y al crear un periodo nuevo debía volver a guardar parámetros aunque la plantilla ya existiera.

Fuerzas: fidelidad a `ADR-0002` (rangos configurables) vs. plantilla única por gestión (`ADR-0013`) vs. UX clara (Periodos ≠ Secciones).

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo |
|-------------|------|---------|-------|
| A. Mantener `ParametroPeriodo` + panel por periodo | Sin migración; cumple `DD-UC-029` al pie | UX confusa; config duplicada; sin seed al crear periodo | Bajo (status quo) |
| B. Mantener tabla `parametro_periodo` pero sembrarla/sincronizarla desde Secciones (sin UI por periodo) | Conserva snapshot por periodo | Dos fuentes de verdad; desalineamiento con `ADR-0014` (secciones editables siempre) | Medio |
| C. `rangoMin` / `rangoMax` como campos de `SeccionEvaluacion`; retirar `ParametroPeriodo` y el panel de Periodos | Una sola fuente; UI alineada; mismos rangos para todos los periodos | Enmienda la lectura “por periodo” de `ADR-0002`; migración Flyway + delta de validación en calificaciones | Medio |

### 3. Decisión

> **Elegimos la Alternativa C.**

1. `SeccionEvaluacion` expone `nota` (peso/escala del motor), `rangoMin` y `rangoMax` (rango permitido al cargar calificaciones).
2. Invariantes: `rangoMin ≥ 0`, `rangoMax > rangoMin`, `rangoMax ≤ nota`. Seed: `rangoMin = 0`, `rangoMax = nota`.
3. UI Periodos: solo nombre, inicio, fin, estado, acciones.
4. UI Secciones: edita `nombre`, `nota`, `rangoMin`, `rangoMax`.
5. Se retiran `ParametroPeriodo`, `GET`/`PUT .../periodos-evaluacion/{id}/parametros` y los códigos `E_PARAMETROS_INCOMPLETOS` / `E_PARAMETROS_INMUTABLES` ligados a ese aggregate.
6. Al abrir un periodo se exige Σ `nota` = 100, rangos válidos en cada sección, y cobertura docente (`E_MATERIA_SIN_DOCENTE`) — sin gate de parámetros por periodo.
7. `CalificacionEvaluacion.valor` se valida en `[rangoMin, rangoMax]` (`E_RANGO_INVALIDO`). `puntajeMaximo` de la evaluación sigue siendo `seccion.nota`.
8. La regla de combinación permanece fija en `PROMEDIO_SIMPLE` (`CalculoNotas`); no se modela por sección en este ADR.

**Relación con ADRs previos:** no supersede `ADR-0002` (sigue vigente la idea de parametrizar sin redespliegue); **enmienda su alcance** de `tenant + periodo` a **plantilla de sección por gestión**. No contradice `ADR-0013`/`ADR-0014`. Supersede la realización de `DD-UC-029` respecto a rangos (la cobertura docente al abrir **se conserva**).

### 4. Consecuencias

- **Positivas:** UX alineada al dominio; un solo lugar para configurar rangos; crear periodo reutiliza la plantilla sin paso extra.
- **Negativas:** migración de datos desde `parametro_periodo` (si hay filas) hacia columnas de sección o defaults; tests de `DD-UC-029` a retirar/adaptar.
- **Seguimiento:** `DD-UC-034` / `PR-IMPL-034`; actualizar `FSD-UC-009`/`014`, `BR-018`/`019`.

### 5. Cumplimiento

| Invariante | Cumple |
|------------|--------|
| Sin tocar `docs/baseline/**` | sí |
| Multitenancy / RLS en tablas tocadas | sí |
| Sin `floor()` en cálculo de notas | sí |
| `CalculoNotas` único motor | sí |

### 6. Referencias

- `docs/product/FSD.md` §4.5 (`FSD-UC-009`), §4.6.4 (`FSD-UC-014`) v2.20
- `docs/design/DD-UC-034.md`, `docs/prompts/impl/PR-IMPL-034.md`
- `docs/design/DD-UC-029.md` (realización previa de rangos por periodo)
- `docs/adr/0002-parametrizacion-reglas-normativas.md`, `0013-*.md`, `0014-*.md`
