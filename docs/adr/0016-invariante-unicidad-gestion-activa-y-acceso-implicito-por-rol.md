# Architecture Decision Record (ADR)

## ADR-0016: Invariante de unicidad de Gestión Escolar ACTIVA y acceso implícito por rol

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0016` |
| Título | Invariante de unicidad de Gestión Escolar ACTIVA y acceso implícito por rol |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Capa viva (`docs/product/BRD.md`, `PRD.md`, `FSD.md`, `DTP.md`). Reemplaza por completo la regla de visibilidad `ACTIVA`-only definida en `ADR-0014` §3.3 (esa sección queda documentada como estado intermedio superado, no reescrita). No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`). No supersede a `ADR-0001`..`0015`. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0014` (define la regla de visibilidad `ACTIVA`-only que este ADR reemplaza); `ADR-0013` (define `GestionEscolar`/`PeriodoEvaluacion`/`SeccionEvaluacion`, sin cambio en este ADR) |

> **Nota de numeración:** el número lógico siguiente a `ADR-0014` era `0015`, pero ese número ya lo había tomado, en paralelo, un ADR no relacionado (adopción de Design System visual del login, `docs/adr/0015-adopcion-design-system-visual-edusync.md`). Este ADR toma el siguiente número libre, `0016`.

### 1. Contexto

`ADR-0014` (§3.3) introdujo una regla de visibilidad por rol sobre `GestionEscolar`: `ADMIN` ve y edita cualquier gestión; `SECRETARIA`/`PROFESOR`/`ASESOR` solo ven la gestión `ACTIVA`, pero **seguían pudiendo listarla y navegarla** (`GET /gestiones-escolares` con el filtro forzado a `estado=ACTIVA`, `GET /{id}` con 404 si no está `ACTIVA`).

El negocio (vía el rol `ADMIN`) reportó, probando el flujo real de `PROFESOR`, que esto seguía siendo incorrecto: *"el profesor no puede modificar ni ver la gestión escolar, tampoco seleccionar la gestión escolar al momento de ingresar a los cursos, es por eso que solo puede haber una gestión escolar activa y todos los datos se muestran de esa gestión escolar, no de otra"*. Es decir: `PROFESOR` (y, por extensión de la misma regla, `SECRETARIA`/`ASESOR`) no debe tener **ninguna** superficie de "Gestión Escolar" propia — ni lista, ni selector, ni pantalla de detalle — sino consumir "la gestión actual" de forma enteramente implícita en cada pantalla que dependa de ella (evaluaciones de una materia, inscripción de un estudiante, etc.).

Esto expuso un gap no resuelto antes: el backend nunca garantizaba que hubiera **como máximo una** `GestionEscolar` en estado `ACTIVA` por tenant. Sin esa garantía, "la gestión actual" es ambigua — no hay una única gestión que un endpoint implícito (`.../activa`) pueda resolver de forma determinística.

Fuerzas: simplicidad de la superficie de UI/API para roles no administrativos (una sola gestión "actual", sin selector) vs. flexibilidad de `ADMIN` para operar con varias gestiones en paralelo durante una transición (ej. cerrar la gestión anterior mientras se prepara la siguiente).

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener `ADR-0014` §3.3 sin cambios (listar/ver por id permitido en modo lectura para no-`ADMIN`, acotado a `ACTIVA`) | Ya implementado (`DD-UC-019`) | No resuelve el pedido real: el `PROFESOR` sigue teniendo una ruta/selector de "Gestión Escolar" que el negocio pidió explícitamente eliminar | Nulo — no resuelve el problema |
| B. Permitir múltiples `GestionEscolar` en `ACTIVA` simultáneamente y resolver "la actual" con una regla adicional (ej. la más reciente por `fechaInicio`) | No requiere invariante de unicidad en el dominio | Ambigüedad real de negocio: dos "actuales" a la vez no tiene sentido pedagógico (evaluaciones/inscripciones quedarían repartidas sin criterio claro); complica el criterio de resolución sin necesidad | Medio — mueve la ambigüedad al criterio de desempate, no la elimina |
| C. Invariante "como máximo una `ACTIVA` por tenant" (auto-cierre de la anterior al activar una nueva) + revocar por completo el acceso de listar/elegir por id para `SECRETARIA`/`PROFESOR`/`ASESOR`, reemplazándolo por un endpoint `.../activa` que resuelve la única gestión `ACTIVA` server-side | Resuelve el pedido exacto; "la gestión actual" queda inequívoca por construcción; `ADMIN` conserva control total (incluida la transición manual entre gestiones) | El `ADMIN` pierde la posibilidad de tener dos gestiones `ACTIVA` en paralelo durante una transición — debe cerrar explícitamente o dejar que el auto-cierre lo haga | Bajo — un invariante nuevo + 3 endpoints nuevos, sin tocar el modelo de datos |

### 3. Decisión

> **Elegimos la Alternativa C.** Confirmado con el negocio (tres preguntas estructuradas, 13/09/2026): (1) al activar una segunda gestión mientras otra está `ACTIVA`, la primera se **cierra automáticamente** (`auto_cerrar`, no un error `409`); (2) la revocación de acceso directo a Gestión Escolar aplica a **`SECRETARIA` y `ASESOR` también**, no solo a `PROFESOR` (`todos`); (3) dentro de la gestión activa, si existe exactamente un periodo `ABIERTO` se autoselecciona sin selector visible — en cualquier otro caso se muestra un selector de periodo, nunca de gestión (`abierto_o_selector_periodo`).

#### 3.1 Invariante de dominio: unicidad de `GestionEscolar.ACTIVA` por tenant

- Al transicionar una `GestionEscolar` a `ACTIVA` (`PATCH /gestiones-escolares/{id}/estado`), si el mismo tenant tiene otra gestión distinta ya en `ACTIVA`, esa otra se cierra automáticamente (`CERRADA`) en la **misma transacción**, antes de activar la solicitada (`CambiarEstadoGestionEscolarService.cerrarOtraActivaSiExiste`).
- Reactivar la misma gestión que ya está `ACTIVA` es un no-op sobre el auto-cierre (no se cierra a sí misma).
- Transicionar a un estado distinto de `ACTIVA` (`PLANIFICACION`, `CERRADA`) no dispara el auto-cierre — solo el evento "activar" lo hace.
- Esta invariante no reabre `ADR-0014` §3.1/§3.2 (edición sin restricción de nombre/fechas/estado por `ADMIN` sigue igual); es una regla nueva y ortogonal.

#### 3.2 Acceso a `GestionEscolar` por rol (reemplaza `ADR-0014` §3.3)

1. **`ADMIN`**: sin cambio. Lista, ve por id, crea, edita y transiciona cualquier `GestionEscolar` de su tenant (`GET/POST/PATCH /gestiones-escolares[, /{id}, /{id}/estado]`, `GET /{id}/periodos`, `GET /{id}/secciones`).
2. **`SECRETARIA`, `PROFESOR`, `ASESOR`**: **pierden por completo** el acceso a listar (`GET /gestiones-escolares` → `403`) y a ver por id (`GET /gestiones-escolares/{id}[, /periodos, /secciones]` → `403`). No existe para estos roles ningún selector ni pantalla de "Gestión Escolar" — ni siquiera de solo lectura (a diferencia de `ADR-0014` §3.3, que sí lo permitía acotado a `ACTIVA`).
3. Nuevo punto de entrada implícito, exclusivo para estos tres roles (aunque `ADMIN` también puede usarlo): `GET /gestiones-escolares/activa[, /periodos, /secciones]`. Resuelve server-side la única gestión `ACTIVA` del tenant (§3.1). `404 E_GESTION_ESCOLAR_NO_ENCONTRADA` si el tenant no tiene ninguna `ACTIVA` — mismo patrón 404-no-403 usado en el resto del proyecto.
4. Cualquier pantalla que antes ofrecía un selector manual de "Gestión escolar" a `SECRETARIA`/`PROFESOR` (evaluaciones de una materia, inscripción de un estudiante) deja de mostrarlo para esos roles: la gestión se resuelve automáticamente contra `.../activa`. `ADMIN` conserva su selector manual (puede operar sobre cualquier gestión, no solo la `ACTIVA`).

#### 3.3 Resolución de periodo por defecto para roles no administrativos

- Dentro de la gestión resuelta implícitamente, si existe **exactamente un** periodo en estado `ABIERTO`, se autoselecciona sin mostrar un selector (se muestra como texto informativo "Periodo actual: <nombre> (ABIERTO)").
- Si hay cero o más de un periodo `ABIERTO` (ej. ningún periodo abierto todavía, o el `ADMIN` reabrió varios a la vez — permitido desde `ADR-0014` §3.1.5), se muestra un selector de **periodo únicamente** (nunca de gestión) para que el actor elija manualmente.

### 4. Consecuencias

#### 4.1 Positivas

- "La gestión actual" queda inequívoca por construcción (invariante de dominio, no una convención de UI que se pueda romper).
- Superficie de API/UI más simple y segura para roles no administrativos: cero riesgo de que `PROFESOR`/`SECRETARIA` operen accidentalmente sobre una gestión histórica (`CERRADA`) creyendo que es la vigente.
- El auto-cierre es transaccional y no requiere intervención manual del `ADMIN` en el caso común (transición ordenada de una gestión a la siguiente).

#### 4.2 Negativas / costos

- El `ADMIN` pierde la posibilidad de tener deliberadamente dos gestiones `ACTIVA` en paralelo (ej. una transición larga con doble matriculación). Mitigación aceptada: no es un caso de uso pedido hoy; si se necesita, requiere un ADR futuro que relaje esta invariante con una regla de desempate explícita.
- Si el `ADMIN` activa una gestión por error, la anterior se cierra silenciosamente (sin confirmación adicional más allá del propio `PATCH .../estado`) — mismo nivel de fricción que cualquier otra transición de estado ya `ADMIN`-only y sin restricción (`ADR-0014` §3.1.4).
- La gobernanza formal (`audit_log`, `ADR-0009` §3 punto 5) sigue pendiente — el auto-cierre tampoco deja rastro explícito de "por qué" se cerró la gestión anterior, más allá de la entrada `CERRADA` en sí. Riesgo heredado, no nuevo.

#### 4.3 Neutras

- `ADR-0013`/`ADR-0014` §3.1/§3.2 (edición sin restricción, invariantes de integridad del motor de cálculo) no se reabren.
- El modelo de datos no cambia (sin migración Flyway nueva): la unicidad se aplica a nivel de aplicación (`CambiarEstadoGestionEscolarService`), no con una constraint de base de datos — coherente con que `GestionEscolar` no tiene hoy ninguna otra constraint de negocio a nivel de esquema.

### 5. Impacto en el sistema

- **Código:** `backend/src/main/java/com/edusync/academico/` — `application/port/out/GestionEscolarRepositoryPort.buscarActivaPorTenant` (nuevo); `application/service/CambiarEstadoGestionEscolarService` (auto-cierre); nuevo `application/port/in/ObtenerGestionEscolarActivaUseCase` + `application/service/ObtenerGestionEscolarActivaService`; `infrastructure/adapter/in/rest/GestionEscolarController` (`GET .../activa[, /periodos, /secciones]` nuevos; `GET /`, `/{id}`, `/{id}/periodos`, `/{id}/secciones` pasan de `hasAnyRole('ADMIN','SECRETARIA','PROFESOR','ASESOR')` a `hasRole('ADMIN')`); eliminado `application/service/GestionEscolarVisibilidad` (ya no hace falta el parámetro `actorVeTodas` en `ListarGestionesEscolaresUseCase`/`ObtenerGestionEscolarUseCase`/`ListarPeriodosEvaluacionUseCase`/`ListarSeccionesEvaluacionUseCase`, todos ahora exclusivamente `ADMIN`).
- **Frontend:** `app.routes.ts` (`/academico/gestiones-escolares/**` vuelve a ser exclusivo `ADMIN`, revierte la ampliación de `ADR-0014`); `shell.component.ts` (enlace "Gestión Escolar" vuelve a ser exclusivo `ADMIN`); `materia-evaluaciones.page.ts` (elimina el selector de gestión para `PROFESOR`, resuelve `.../activa` implícitamente, autoselecciona el periodo `ABIERTO` único); `estudiante-detalle.page.ts` (elimina el selector de gestión para `SECRETARIA` al crear una inscripción, resuelve `.../activa` implícitamente).
- **Operaciones:** sin cambio de infra; sin migración Flyway nueva.
- **Seguridad:** mismas reglas RLS/`tenant_id`; el patrón 404-no-403 se mantiene en `.../activa`; los endpoints revocados devuelven `403` (RBAC estándar de Spring Security), no `404` — es la denegación esperada de un recurso que el rol nunca debió listar, no una ocultación de existencia.
- **Costo:** nulo en AWS.

### 6. Plan de reversión

- **Señal:** un caso de uso real que necesite dos `GestionEscolar` `ACTIVA` simultáneas del mismo tenant (ej. transición prolongada con doble matriculación), o que un rol no-`ADMIN` necesite consultar el histórico de gestiones cerradas.
- **Costo:** bajo-medio — el auto-cierre es una única función (`cerrarOtraActivaSiExiste`) fácil de remover o condicionar; el acceso revocado es una anotación `@PreAuthorize` por endpoint, fácil de restaurar a `hasAnyRole(...)`.
- **Plan B:** si se necesita reabrir el acceso de solo lectura de `SECRETARIA`/`PROFESOR`/`ASESOR` al histórico, diseñarlo como una vista explícitamente distinta de "consultar histórico" (ej. `GET /gestiones-escolares/historico`, de solo lectura, sin selector de "gestión actual" en las pantallas operativas) en un ADR futuro, sin reabrir la invariante de unicidad de §3.1.

### 7. Validación

- Tests de dominio: sin cambio (la invariante vive en `application/`, no en `GestionEscolar` como Aggregate Root).
- Tests de aplicación (Mockito): `CambiarEstadoGestionEscolarServiceTest` — activar una gestión cierra automáticamente la otra `ACTIVA` del tenant; reactivar la misma gestión no se cierra a sí misma; transicionar a un estado distinto de `ACTIVA` no dispara el auto-cierre. `ObtenerGestionEscolarActivaServiceTest` — devuelve la única `ACTIVA`; lanza `GestionEscolarNoEncontradaException` si ninguna lo está. `ListarGestionesEscolaresServiceTest` — el servicio (ahora exclusivo `ADMIN`) delega filtro/paginación sin modificarlos.
- Tests de integración (Testcontainers, `GestionEscolarIntegrationTest`): activar una segunda gestión cierra la primera (verificado con `GET /{id}` de ambas); `PROFESOR` recibe `403` en `GET /gestiones-escolares` y en `GET /{id}`, `404` en `GET .../activa` sin gestión `ACTIVA`, y `200` con la gestión correcta (incluidos sus 3 periodos seed) una vez que el `ADMIN` la activa.
- `mvn test` verde (incluye `ModularityTests`); `ng build` verde.
- Responsable: `qa-agent` en revisión del `PR-IMPL` de `DD-UC-021`.

### 8. Referencias

- `ADR-0014` §3.3 (regla de visibilidad que este ADR reemplaza).
- `docs/design/DD-UC-021.md`.
- `docs/product/FSD.md` `FSD-UC-012`/`FSD-UC-013`/`FSD-UC-014`/`FSD-UC-015`.
- `AGENTS.md` §6 (invariantes de dominio; sin impacto en RUDE/`floor`/`audit_log`/RLS).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 13/09/2026 | Rodrigo Aspeti | ADR formal a partir del pedido de negocio: invariante "como máximo una `GestionEscolar.ACTIVA` por tenant" (auto-cierre de la anterior al activar una nueva) + revocación total del acceso de listar/ver por id a `GestionEscolar` para `SECRETARIA`/`PROFESOR`/`ASESOR` (reemplaza la visibilidad `ACTIVA`-only de solo lectura de `ADR-0014` §3.3) + nuevo endpoint implícito `GET .../activa[, /periodos, /secciones]` + resolución automática de periodo `ABIERTO` único; estado Aceptada; no supersede `ADR-0001`..`0015` |
