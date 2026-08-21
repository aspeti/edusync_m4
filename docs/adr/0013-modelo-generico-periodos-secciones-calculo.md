# Architecture Decision Record (ADR)

## ADR-0013: Modelo genérico SaaS de periodos, secciones de evaluación y cálculo de notas

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0013` |
| Título | Modelo genérico SaaS de periodos, secciones de evaluación y cálculo de notas |
| Fecha | 21/08/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Capa viva (`docs/product/BRD.md`, `PRD.md`, `FSD.md`, `DTP.md`). Resuelve los puntos 1–4 de `ADR-0009` §3. No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`). No supersede a `ADR-0001`..`0012`. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0009` (generalización aditiva; dejó pendientes de definición los 5 puntos de §3) |

### 1. Contexto

`ADR-0009` introdujo `GestionEscolar`, `PeriodoEvaluacion`, `SeccionEvaluacion` y `Evaluacion` como extensión aditiva sobre el Perfil Bolivia SIE, y dejó **cinco puntos fuera de alcance** antes de implementar `FSD-UC-013`..`FSD-UC-016`:

1. Si Bolivia (`GestionAcademica` / `ParametroAcademico`) es una instancia del genérico o una ruta de código paralela.
2. Secuencialidad de apertura (`RB-05`) y visibilidad del promedio de gestión (`RB-11`) con N periodos.
3. Redondeo del motor genérico (`floor()` vs. otra estrategia).
4. Si la suma de pesos/notas de las secciones debe ser 100.
5. Gobernanza (`audit_log`, inmutabilidad post-cierre, ventana 1–72 h) de los módulos nuevos.

El catálogo SaaS de `academico` (`FSD-UC-012`/`017`/`018`/`019`/`020`) ya está implementado. El siguiente eslabón (`FSD-UC-013`..`016`) no puede diseñarse sin cerrar 1–4. El negocio confirmó el **modelo genérico como único motor** de la capa viva, con Bolivia como **defaults** (3 periodos + 4 secciones Ser/Saber/Hacer/Autoevaluación). El punto 5 permanece pendiente (mismo criterio que `DD-UC-008`..`014`).

Fuerzas: un solo motor vs. duplicar Bolivia; promedio simple por sección vs. ponderar evaluaciones; mostrar el promedio de gestión con datos parciales vs. ocultarlo (`RB-11`); `floor()` SIE vs. `round` pedagógico.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Dos motores: genérico y Bolivia en paralelo (`GestionAcademica` + `ParametroAcademico` vs. `GestionEscolar`) | Cero riesgo sobre golden tests `floor`/SIE | Duplica dominio, APIs y UI; contradice el pedido de un modelo SaaS único | Alto — dos caminos de cálculo hasta `release/3.0.0` |
| B. Un motor genérico; Bolivia = seed/defaults (3 periodos, 4 secciones 5/45/40/10). Fórmula: promedio simple de evaluaciones en la escala `[0, nota]` de la sección; nota de periodo = suma de secciones; `round` HALF_UP a entero en periodo y gestión | Un modelo, implementable en `academico`; SIE/`floor()` queda en `notassie` | El centralizador SIE (5 dimensiones, `floor`) se implementa después como perfil de exportación, no como segundo cálculo cotidiano | Medio — un Design Doc cluster `FSD-UC-013`..`016` |
| C. Motor genérico con promedio ponderado de evaluaciones (cupos 30+15=45) y `floor()` por defecto | Cercano a T-007 Bolivia | El negocio rechazó cupos por evaluación y `floor()` en el genérico | Medio — fórmula que el negocio no quiere |

### 3. Decisión

> **Elegimos la Alternativa B: un único modelo genérico SaaS.** El Perfil Bolivia SIE es una **instancia parametrizada** (defaults), no una ruta de código paralela. `ADR-0009` permanece `Aceptada`; este ADR **resuelve** sus puntos 1–4. El punto 5 (gobernanza de módulos nuevos) sigue pendiente.

#### 3.1 Estructura (`FSD-UC-012`/`013`/`014`)

1. Cada `GestionEscolar` tiene N `PeriodoEvaluacion` (N ≥ 1) y una **plantilla** de M `SeccionEvaluacion` **a nivel de gestión** (las mismas en T1, T2, T3).
2. Al **crear** la gestión, el sistema **siembra** 3 periodos (`Trimestre 1`..`3`, fechas a completar/ajustar) y 4 secciones:

   | Orden | Nombre | `nota` (puntos) |
   |-------|--------|-----------------|
   | 1 | Ser | 5 |
   | 2 | Saber | 45 |
   | 3 | Hacer | 40 |
   | 4 | Autoevaluación | 10 |
   | | **Suma** | **100** |

3. `nota` de la sección **es** su peso sobre 100. No hay campo separado `peso_porcentual`. Suma de `nota` de las secciones de la gestión **= 100** (`422 E_SUMA_SECCIONES_INVALIDA` si no).
4. Mientras **ningún** periodo está `ABIERTO`, el Admin puede editar nombres, fechas, N y las `nota` de sección (manteniendo suma 100).
5. Desde que **el primer** periodo pasa a `ABIERTO`, la plantilla de secciones (nombres y `nota`) es **inmutable** (`422 E_SECCIONES_INMUTABLES`), incluso si ese periodo luego se cierra. Equivalente genérico de `RB-06`.

#### 3.2 Periodos (`FSD-UC-013`)

1. Cada periodo tiene `nombre`, `fechaInicio`, `fechaFin`, `estado ∈ {PENDIENTE, ABIERTO, CERRADO}`.
2. **Apertura secuencial** (equivalente genérico de `RB-05`/`BR-006`): el periodo *k* no puede pasar a `ABIERTO` si el *k−1* no está `CERRADO`. El periodo 1 no tiene predecesor. `422 E_PERIODO_NO_SECUENCIAL`.
3. Fechas de periodos de la misma gestión no se solapan (`422 E_PERIODOS_SOLAPADOS`).

#### 3.3 Evaluaciones (`FSD-UC-015`)

1. Las materias **usan** la plantilla de secciones de la gestión.
2. En cada (`Materia` × `PeriodoEvaluacion` × `SeccionEvaluacion`) el Profesor crea 1..N `Evaluacion`.
3. Toda evaluación de una sección se califica en **`[0, seccion.nota]`**. Si Saber tiene `nota = 45`, todas sus evals son 0–45. `puntajeMaximo` se deriva de la sección; no es configurable por evaluación.
4. `calificacion ∈ [0, seccion.nota]` → si no, `422 E_RANGO_INVALIDO`.
5. El catálogo `TipoEvaluacion` queda **diferido** (no bloquea 013–016).

#### 3.4 Cálculo (`FSD-UC-016`, `BR-020`)

Motor exclusivo de dominio (equivalente genérico de `BR-008`: ni SQL, ni adaptadores, ni frontend).

```text
nota_seccion     = round_2d( (Σ nota_evaluacion) / n )     // n = evals con nota; escala [0, seccion.nota]
nota_periodo     = round_HALF_UP_entero( Σ nota_seccion )
promedio_gestion = round_HALF_UP_entero( (Σ nota_periodo_o_cero) / N )
```

- `n = 0` en una sección → esa sección `INCOMPLETO` (no inventar 0 en el promedio de sección). Un periodo sin nota cuenta **0** en el promedio de gestión.
- `N` = cantidad de `PeriodoEvaluacion` de la gestión (3 en el seed). El promedio de gestión **sí se muestra** con datos parciales, marcado `PROVISIONAL` (distinto de `RB-11` Bolivia).
- **No** se usa `floor()` en este motor. `floor()` permanece en el Perfil Bolivia SIE (`BR-003`/`BR-008`, golden `FloorTest`) para consolidación/exportación SIE (`FSD-UC-003`/`004`).

**Ejemplo canónico** (Saber `nota = 45`, dos evaluaciones 35 y 40; Ser=5, Hacer=40, AE=10; N=3, solo T1):

```text
nota_seccion(Saber) = (35+40)/2 = 37.50
nota_periodo_cruda  = 5 + 37.50 + 40 + 10 = 92.50
nota_periodo        = 93
promedio_gestion    = round(93/3) = 31     // T2=T3=0, PROVISIONAL
```

`round_2d` = HALF_UP a 2 decimales. `round_HALF_UP_entero(92.5) = 93` (no `floor` → 92).

#### 3.5 Pendiente (fuera de alcance)

- Gobernanza de módulos nuevos (`ADR-0009` §3 punto 5).
- Reabrir un periodo `CERRADO`.
- Promedio **entre materias** (carga horaria / promedio institucional).
- Exportación SIE / `floor()` sobre este modelo (`FSD-UC-003`/`004`).

### 4. Consecuencias

#### 4.1 Positivas

- `FSD-UC-013`..`016` quedan desbloqueados para Design Docs (`DD-UC-015` en adelante).
- Un solo motor de notas en `academico`; Bolivia es seed, no fork.
- La fórmula es auditable con el ejemplo 37.5 → 92.5 → 93 → 31.

#### 4.2 Negativas / costos

- `POST /gestiones-escolares` vigente (`PR-IMPL-008`) **no siembra** periodos/secciones: hay un delta de implementación sobre `FSD-UC-012` (se aborda con el primer PR de 013/014).
- El centralizador SIE de 5 dimensiones (con Decidir) y `floor()` no sale “gratis” del genérico de 4 secciones; se diseña después en `notassie`.
- El promedio de gestión `/ N` con periodos vacíos en 0 (93 → 31) es deliberado y hay que explicarlo en UI (`PROVISIONAL`).

#### 4.3 Neutras

- `ADR-0009` no se reabre ni se supersede.
- Golden `FloorTest` **no** aplica a `FSD-UC-016` genérico.

### 5. Impacto en el sistema

- **Código (futuro):** `backend/src/main/java/com/edusync/academico/` — nuevos aggregates `PeriodoEvaluacion`, `SeccionEvaluacion`, `Evaluacion`; servicio de cálculo en `domain`/`application` (no en REST ni Angular). Delta de seed en `CrearGestionEscolarService`. Migraciones Flyway nuevas (`V9+`), `tenant_id` + RLS `FORCE`.
- **Operaciones:** sin cambio de infra. Schedulers de vencimiento de tenant intactos.
- **Seguridad:** mismas reglas RLS/`tenant_id`; sin PII en logs (`AGENTS.md` §7).
- **Equipo:** siguiente trabajo = `feature-design-doc` para `FSD-UC-013` (luego 014–016).
- **Costo:** nulo en AWS hasta implementar.

### 6. Plan de reversión

- **Señal:** instituciones que necesiten escalas distintas por trimestre, o promedio de gestión solo con periodos cerrados, o `floor()` en el boletín cotidiano.
- **Costo:** medio — cambiar fórmula y posiblemente colgar secciones del periodo (migración de `seccion.gestion_escolar_id` → `periodo_evaluacion_id`).
- **Plan B:** Alternativa A (dos motores) o reabrir punto 2/3 con un ADR-0014.

### 7. Validación

- Tests de dominio del motor: ejemplo canónico 35/40 → 37.50 → 93 → 31 (N=3).
- Rechazo de apertura no secuencial y de edición de secciones con un periodo `ABIERTO`.
- Suma de `nota` de secciones ≠ 100 → `E_SUMA_SECCIONES_INVALIDA`.
- Calificación fuera de `[0, seccion.nota]` → `E_RANGO_INVALIDO`.
- `FloorTest` sigue verde y **no** se reutiliza como oracle del genérico.
- Responsable: `qa-agent` en revisión de `PR-IMPL` de 013–016; `compliance-agent` en golden SIE.

### 8. Referencias

- `ADR-0009` §3 puntos 1–4 (resueltos aquí); punto 5 pendiente.
- `docs/product/FSD.md` `FSD-UC-012`..`016`, `BR-016`..`BR-020`.
- `docs/product/BRD.md` §11.1, §12 (`RB-05`/`RB-08`/`RB-11` Bolivia intactos).
- `AGENTS.md` §6 (`BR-008` `floor()` = Perfil SIE / `ConsolidacionDomainService`).
- Golden tests: `FloorTest` (SIE), no genérico.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 21/08/2026 | Rodrigo Aspeti | ADR formal a partir de las decisiones de negocio del modelo genérico (plantilla de secciones por gestión, seed 3+4, suma 100, escala `[0, nota]`, promedio simple, `round` 2 decimales / entero, apertura secuencial, promedio de gestión `/ N` visible `PROVISIONAL`); estado Aceptada; no supersede `ADR-0009` |
