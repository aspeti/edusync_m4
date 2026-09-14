# Architecture Decision Record (ADR)

## ADR-0014: Relajación de restricciones de edición en Periodos/Secciones y visibilidad de Gestión Escolar por rol

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0014` |
| Título | Relajación de restricciones de edición en Periodos/Secciones y visibilidad de Gestión Escolar por rol |
| Fecha | 12/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Capa viva (`docs/product/BRD.md`, `PRD.md`, `FSD.md`, `DTP.md`). Modifica puntos de `ADR-0013` §3.1.4/3.1.5 y §3.2.2 (freeze de secciones, secuencialidad de apertura de periodos). Añade una regla de visibilidad por rol sobre `GestionEscolar`, no contemplada antes. No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`). No supersede a `ADR-0001`..`0013`. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0013` (define las reglas que este ADR relaja; su propio §6 "Plan de reversión" anticipó explícitamente "reabrir punto 2/3 con un ADR-0014") |

### 1. Contexto

El negocio (Director de la institución, vía el rol `ADMIN`) reportó que las reglas de `ADR-0013` §3.1.4/3.1.5 (freeze de la plantilla de secciones una vez que el primer periodo pasa a `ABIERTO`, permanente incluso si ese periodo se cierra) y §3.2.2 (apertura secuencial de periodos, el periodo *k* no puede abrirse si el *k−1* no está `CERRADO`) son demasiado rígidas para el uso real: el `ADMIN` necesita poder corregir un nombre, una fecha, o reabrir/reordenar periodos y secciones en cualquier momento, sin que un periodo `ABIERTO`/`CERRADO` lo bloquee.

Adicionalmente, el negocio pidió una regla de visibilidad nueva, no contemplada en `ADR-0009`/`ADR-0013`: el resto de roles de tenant (`SECRETARIA`, `PROFESOR`, `ASESOR`) no deben ver el histórico completo de Gestiones Escolares — solo la gestión "actual" (`estado = ACTIVA`). Antes de este ADR, `GET /gestiones-escolares` y sus sub-recursos (periodos, secciones) eran accesibles en modo lectura sin ninguna restricción de estado para `SECRETARIA`/`PROFESOR` (`ASESOR` no tenía acceso en absoluto).

Fuerzas: precisión editorial estricta para preservar la integridad del motor de cálculo (`ADR-0013` §3.4) vs. flexibilidad operativa real que pide el `ADMIN`; exponer el histórico completo vs. acotar a la vista "actual" para roles no administrativos.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener las reglas de `ADR-0013` intactas; resolver con excepciones puntuales (ej. un endpoint de "reapertura" separado) | No reabre `ADR-0013` | No resuelve el pedido real (edición libre de nombre/fechas); multiplica endpoints y casos especiales | Alto — deuda de diseño |
| B. Eliminar por completo la máquina de estados/freeze/secuencialidad de `GestionEscolar`, `PeriodoEvaluacion` y `SeccionEvaluacion`, dejando solo las invariantes de integridad del motor de cálculo (suma de secciones = 100, no solape de fechas, al menos 1 periodo); añadir visibilidad `ACTIVA`-only para roles no admin | Resuelve el pedido exacto; los endpoints ya son `ADMIN`-only (RBAC ya acota el riesgo); consistente con el "Plan B" que el propio `ADR-0013` §6 anticipó | Se pierden las garantías de "snapshot congelado" de secciones/periodos una vez que empieza la evaluación; el `ADMIN` puede desalinear notas ya calculadas si reabre un periodo | Bajo — elimina código, no lo añade |
| C. Mantener el freeze/secuencialidad solo para roles no-`ADMIN` (ninguno los tenía de todos modos, porque los endpoints ya son `ADMIN`-only) | — | Alternativa vacía: los endpoints de escritura ya eran exclusivamente `ADMIN`; no hay a quién aplicarle una restricción adicional | — |

### 3. Decisión

> **Elegimos la Alternativa B.** Los tres endpoints de escritura afectados (`PATCH/DELETE` de periodos, `PUT/PATCH` de secciones, `PATCH estado` de gestión/periodo) ya eran exclusivamente `ADMIN` desde `DD-UC-015`/`DD-UC-016`; la máquina de estados y el freeze protegían contra un actor que, por diseño, ya es el único autorizado a tomar esa decisión. Eliminarlos no abre una superficie de riesgo nueva — solo le da al `ADMIN` la flexibilidad operativa que pidió. Las invariantes de **integridad del motor de cálculo** (`ADR-0013` §3.4) se mantienen intactas porque no dependen de un flujo de edición, sino de la consistencia matemática de los datos: la suma de `nota` de las secciones de una gestión sigue debiendo ser exactamente 100 (`E_SUMA_SECCIONES_INVALIDA`), los periodos de una misma gestión siguen sin poder solaparse en fechas (`E_PERIODOS_SOLAPADOS`), y siempre debe quedar al menos un periodo (`E_PERIODO_UNICO`).

#### 3.1 Qué se elimina (relaja `ADR-0013` §3.1.4/3.1.5 y §3.2.2)

1. **Freeze de secciones** (`E_SECCIONES_INMUTABLES`, `ADR-0013` §3.1.5): eliminado. `PUT /gestiones-escolares/{id}/secciones` y `PATCH /secciones-evaluacion/{id}` ya no consultan el estado de los periodos hermanos. El `ADMIN` puede reeditar la plantilla en cualquier momento, incluso con periodos `ABIERTO`/`CERRADO`.
2. **Secuencialidad de apertura** (`E_PERIODO_NO_SECUENCIAL`, `ADR-0013` §3.2.2): eliminada. `PATCH /periodos-evaluacion/{id}/estado` permite abrir/cerrar cualquier periodo en cualquier orden, sin exigir que el predecesor esté `CERRADO`.
3. **Freeze de periodos** (`E_PERIODOS_INMUTABLES`, introducido en `DD-UC-015` como corolario de la secuencialidad): eliminado. `POST/PATCH/DELETE` de periodos ya no se bloquean por la existencia de un periodo hermano `ABIERTO`.
4. **Máquina de estados de `GestionEscolar.cambiarEstado()`** (`PLANIFICACION→ACTIVA→CERRADA`, con `ACTIVA→PLANIFICACION` como única reapertura permitida, `CERRADA` terminal): eliminada. Cualquier transición es válida desde cualquier estado.
5. **Máquina de estados de `PeriodoEvaluacion.cambiarEstado()`** (`PENDIENTE→ABIERTO→CERRADO` estrictamente hacia adelante): eliminada. Un periodo `CERRADO` puede reabrirse a `ABIERTO`.

#### 3.2 Qué se conserva (invariantes de integridad del motor de cálculo, no de flujo/edición)

1. **Suma de secciones = 100** (`E_SUMA_SECCIONES_INVALIDA`, `ADR-0013` §3.1.3): intacta. Se valida en cada escritura (`POST`/`PUT`/`PATCH` de secciones) y también al **abrir** un periodo (`PATCH .../estado` a `ABIERTO`), para que el motor de cálculo (`ADR-0013` §3.4) nunca opere sobre una plantilla inconsistente.
2. **No solape de fechas de periodos** (`E_PERIODOS_SOLAPADOS`, `ADR-0013` §3.2.3): intacto.
3. **Al menos un periodo por gestión** (`E_PERIODO_UNICO`): intacto — `DELETE` de periodos sigue rechazando dejar la gestión sin periodos.

#### 3.3 Nueva regla: visibilidad de `GestionEscolar` por rol

1. **`ADMIN`** ve y edita **cualquier** `GestionEscolar` de su tenant, sin importar el estado (`PLANIFICACION`/`ACTIVA`/`CERRADA`).
2. **`SECRETARIA`**, **`PROFESOR`** y **`ASESOR`** solo ven la gestión **"actual"**, definida como `estado = ACTIVA` (no una combinación con el estado de sus periodos). Si no hay ninguna gestión `ACTIVA`, no ven ninguna.
3. El endpoint de listado (`GET /gestiones-escolares`) **ignora** el filtro `estado` enviado por un actor no-`ADMIN` y lo fuerza a `ACTIVA`.
4. Los endpoints de detalle/sub-recurso (`GET /{id}`, `GET /{id}/periodos`, `GET /{id}/secciones`) devuelven **`404`** (no `403`) si el actor no-`ADMIN` intenta acceder a una gestión que no está `ACTIVA` — mismo patrón 404-no-403 usado en el resto del proyecto para ocultar existencia (ej. aislamiento cross-tenant).
5. `ASESOR` ganó acceso de **solo lectura** a Gestión Escolar por primera vez (antes tenía cero acceso a este recurso).

### 4. Consecuencias

#### 4.1 Positivas

- El `ADMIN` obtiene la flexibilidad operativa real que pidió, sin código adicional (se elimina código, no se añade).
- El motor de cálculo (`ADR-0013` §3.4) sigue protegido por las invariantes matemáticas que realmente le importan (suma 100, sin solape, ≥1 periodo).
- `ASESOR` deja de estar completamente excluido de Gestión Escolar.
- Reduce la superficie de excepciones de dominio: 5 clases (`EstadoGestionEscolarInvalidoException`, `EstadoPeriodoEvaluacionInvalidoException`, `PeriodosInmutablesException`, `SeccionesInmutablesException`, `PeriodoNoSecuencialException`) eliminadas del `domain/` de `academico`.

#### 4.2 Negativas / costos

- Se pierde la garantía de "snapshot congelado" de secciones/periodos una vez iniciada la evaluación: el `ADMIN` puede, por error, reabrir un periodo `CERRADO` o cambiar la plantilla de secciones después de que ya se calcularon notas `PROVISIONAL` con la plantilla anterior, produciendo promedios que ya no reflejan la configuración vigente al momento del cálculo. No hay recálculo automático retroactivo (fuera de alcance — el motor de `CalculoNotas` es on-read, `DD-UC-018`, así que una vez cambiada la plantilla, la **próxima** lectura ya usa los nuevos pesos, lo que puede ser sorpresivo).
- Mitigación aceptada: el riesgo queda acotado porque solo `ADMIN` puede hacerlo (mismo actor que antes solo podía hacer las transiciones "seguras"); se documenta como advertencia operativa, no como bloqueo de código.
- La gobernanza formal (`audit_log`, `ADR-0009` §3 punto 5) sigue pendiente — sin ella, no queda rastro de quién reabrió qué ni cuándo. Se registra explícitamente como riesgo heredado, no nuevo.

#### 4.3 Neutras

- `ADR-0013` no se reabre en su totalidad ni se supersede: la fórmula de cálculo (§3.4), el seed de 3 periodos + 4 secciones (§3.1.2) y la escala `[0, seccion.nota]` (§3.3) permanecen sin cambio.
- `ADR-0009` no se reabre.

### 5. Impacto en el sistema

- **Código:** `backend/src/main/java/com/edusync/academico/` — `domain/GestionEscolar.java` (mutabilidad de `nombre`/`fechaInicio`/`fechaFin`, `actualizarDatos()`, `cambiarEstado()` sin validación), `domain/PeriodoEvaluacion.java` (`cambiarEstado()` sin validación); 5 excepciones de dominio eliminadas; `application/service/PeriodoEvaluacionPolitica.java` y `SeccionEvaluacionPolitica.java` reducidas a solo las invariantes de §3.2; nuevo `GestionEscolarVisibilidad.java` (helper de visibilidad); nuevo `ActualizarGestionEscolarUseCase`/`Command`/`Service` (`PATCH /gestiones-escolares/{id}`); delta en las 4 interfaces de listado/obtención (`+boolean actorVeTodas`); delta en 3 controladores REST (`GestionEscolarController`, `PeriodoEvaluacionController`, `SeccionEvaluacionController`) — RBAC ampliado a `ASESOR` en lectura, limpieza de códigos de error muertos.
- **Operaciones:** sin cambio de infra; sin migración Flyway nueva (no hay cambio de esquema, solo de reglas de aplicación).
- **Seguridad:** mismas reglas RLS/`tenant_id`; el patrón 404-no-403 se extiende a la visibilidad por estado de `GestionEscolar` (no solo a cross-tenant); sin PII en logs (`AGENTS.md` §7).
- **Frontend:** rutas de Gestión Escolar (`/academico/gestiones-escolares/**`) se abren a `SECRETARIA`/`PROFESOR`/`ASESOR` en modo lectura; UI de escritura (botones de alta/edición/cambio de estado) queda oculta para no-`ADMIN`; nuevo diálogo de edición de nombre/fechas de gestión y de periodo para `ADMIN`.
- **Costo:** nulo en AWS; reduce líneas de código neto (elimina 5 excepciones + lógica de validación).

### 6. Plan de reversión

- **Señal:** un incidente real donde un `ADMIN` reabre un periodo `CERRADO` o cambia secciones después de que ya se generaron notas oficiales, produciendo confusión sobre qué promedio es válido.
- **Costo:** medio — reintroducir la máquina de estados y el freeze exige restaurar las 5 excepciones eliminadas y las validaciones en los 3 servicios de escritura (revertible desde el historial de Git; este ADR documenta exactamente qué se quitó, §3.1).
- **Plan B:** si se necesita reabrir con salvaguardas (en vez de sin restricción), diseñar un flujo de "reapertura autorizada" con ventana de tiempo — análogo a `AutorizacionCorreccion` del Perfil Bolivia SIE (`BR-009`) — en un ADR futuro (nota `v2` §9: ese ADR ocupó el número `0016`, no `0015` — ese número lo tomó, en paralelo, un ADR no relacionado de Design System visual), sin volver a la secuencialidad estricta de `ADR-0013`.

### 7. Validación

- Tests de dominio: `GestionEscolar` acepta cualquier transición, incluida desde `CERRADA`; `PeriodoEvaluacion` acepta reabrir un `CERRADO`; `actualizarDatos()` valida solo `fechaFin > fechaInicio`.
- Tests de aplicación (Mockito): crear/abrir periodo con un hermano `ABIERTO` ya no lanza excepción; `PUT`/`PATCH` de secciones con periodo `ABIERTO`/`CERRADO` ya no lanza excepción; suma ≠ 100 y solape de fechas siguen rechazando.
- Tests de integración (Testcontainers): flujo completo de apertura no secuencial y edición sin freeze en verde; `GestionEscolarVisibilidad` fuerza `estado=ACTIVA` para actor no-admin y devuelve `404` en detalle/sub-recursos si la gestión no está `ACTIVA`.
- `mvn test` → 238/238 verde (incluye `ModularityTests` 7/7); `ng build` verde.
- Responsable: `qa-agent` en revisión del `PR-IMPL` de `DD-UC-019`.

### 8. Referencias

- `ADR-0013` §3.1.4/3.1.5, §3.2.2, §6 (el propio plan de reversión anticipó este ADR).
- `docs/design/DD-UC-019.md`.
- `docs/product/FSD.md` `FSD-UC-012`/`FSD-UC-013`/`FSD-UC-014`.
- `AGENTS.md` §6 (invariantes de dominio; ninguna de las invariantes RUDE/`floor`/`audit_log`/RLS se toca aquí).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 12/09/2026 | Rodrigo Aspeti | ADR formal a partir del pedido de negocio: eliminación de la secuencialidad de apertura y el freeze de periodos/secciones (`ADR-0013` §3.1.4/3.1.5/§3.2.2), manteniendo las invariantes de integridad del motor de cálculo (suma 100, sin solape, ≥1 periodo); nueva regla de visibilidad `ACTIVA`-only de `GestionEscolar` para `SECRETARIA`/`PROFESOR`/`ASESOR`; estado Aceptada; no supersede `ADR-0009`/`ADR-0013` |
| 2 | 13/09/2026 | Rodrigo Aspeti | Nota de redirección (sin cambio normativo): `ADR-0016` (no `ADR-0015`, ya tomado por un ADR no relacionado de Design System visual) reemplaza por completo la visibilidad `ACTIVA`-only definida en §3.3 — el negocio aclaró que `SECRETARIA`/`PROFESOR`/`ASESOR` no deben *listar ni elegir* ninguna `GestionEscolar` (ni siquiera en modo lectura, como permitía §3.3 punto 2-4), sino consumir "la gestión actual" de forma enteramente implícita. §3.3 de este ADR queda documentado como el estado intermedio real (12/09/2026), superado por `ADR-0016` (13/09/2026) — no se reescribe retroactivamente. |
