# Architecture Decision Record (ADR)

## ADR-0025: El SIE recibe la nota final de cada periodo; se retira `floor()` del producto vivo

### Metadatos

| Campo | Valor |
|-------|-------|
| Numero | `0025` |
| Titulo | Un solo motor de cálculo con `round`; el SIE recibe una nota final entera por estudiante, materia y periodo |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Propuesta** (pendiente de aprobación humana) |
| Alcance | Capa viva (`docs/product/`, `AGENTS.md`, `backend/`). **Adopta el modelo genérico (`ADR-0013`, `FSD-UC-016`) como único perfil de cálculo del producto** y descarta el Perfil Bolivia SIE de dimensiones fijas (`FSD-UC-001`/`003`/`004`/`005`/`009`). El SIE pasa a ser solo un **destino de exportación** del modelo genérico, no un segundo perfil. No toca `docs/baseline/**`. No supersede a ningún ADR. **Modifica** dos puntos: `ADR-0005` §3 (clave de idempotencia) y `ADR-0013` §3.4/§3.5 (la nota de que `floor()` queda reservado al SIE). |

### 1. Contexto

La capa viva convive hoy con dos modelos de cálculo que se contradicen:

- **Perfil Bolivia SIE** (`FSD-UC-001`/`003`/`004`, `BR-003`, `BR-008`, `RB-08`): cinco dimensiones fijas (Ser, Saber, Hacer, Decidir, Autoevaluación) con `peso_max`, truncado con `floor()` en `ConsolidacionDomainService` y exportación con clave `rude + periodo_id`. Ninguna pieza de este modelo existe en código. El módulo `notassie` solo tiene `package-info.java`.
- **Modelo genérico** (`ADR-0013`, `FSD-UC-013`..`016`): secciones configurables por gestión (`SeccionEvaluacion.nota`, suma 100), promedio simple con `round` HALF_UP, nota de periodo entera. Está implementado de punta a punta (`CalculoNotas`, `CalificacionEvaluacion`, matriz de calificaciones, `mvn test` 235/235 al cerrar `PR-IMPL-018`).

`ADR-0013` §3.5 difirió la exportación al SIE y dejó escrito que `floor()` seguía reservado a ese perfil. Eso obliga a mantener un segundo motor de redondeo, una segunda definición de "nota", y reglas de `AGENTS.md` (§6, §8.3, §11) que prohíben `Math.round()` en un lado y `floor()` en el otro. El 10/11/2026 el responsable del producto decidió:

0. **El perfil que seguimos es el modelo genérico** (`ADR-0013`). El Perfil Bolivia SIE con cinco dimensiones fijas deja de ser un perfil del producto.
1. Se mantienen las **secciones configurables**.
2. **Todos los redondeos son `round`**. No hay `floor()` en ningún punto.
3. El **SIE solo recibe la nota final de cada trimestre** (periodo).
4. Una nota de periodo se exporta **solo si todas sus secciones están completas** (las incompletas quedan `EXCLUIDO_NOTA_INCOMPLETA`).
5. La **inmutabilidad se difiere**. Por ahora los datos son modificables mientras el periodo está `ABIERTO`.
6. No hay "cierre de materia". Cerrar el periodo basta para habilitar la exportación.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras |
|-------------|------|---------|
| A. Mantener dos perfiles: Perfil SIE con 5 dimensiones fijas y `floor()` en `notassie`, genérico en `academico` (**descartada**: se sigue solo el modelo genérico) | Fiel al FSD clásico y al baseline. El SIE recibiría el detalle por dimensión | Dos modelos de nota para el mismo estudiante. El docente cargaría notas en secciones configurables y luego alguien tendría que mapearlas a 5 dimensiones fijas. Duplica el costo de pruebas y deja `AGENTS.md` con reglas opuestas. Contradice la decisión (1) |
| B. Un motor con `round`, y el SIE recibe además el detalle por sección | Más información para el SIE | El SIE no define secciones configurables: cada tenant tendría una forma distinta de payload. Sin especificación real del SIE no se puede validar. Contradice la decisión (3) |
| C. Un motor con `round`; el SIE recibe `rude`, materia, periodo y nota final entera; sin conversión de escala | Un solo cálculo (`CalculoNotas`). Payload mínimo, estable y sin PII más allá del RUDE. Reutiliza todo lo ya implementado | Si el SIE real exigiera otra escala o el detalle por dimensión, habría que añadir un adaptador de conversión. Se acepta porque la especificación real del SIE sigue sin conocerse (`ADR-0005`) |

### 3. Decisión

Alternativa C.

**3.0 Perfil vigente: modelo genérico.** El producto sigue un único perfil, el **modelo genérico** de `ADR-0013`: periodos y secciones configurables por gestión, promedio con `round` y nota de periodo entera. Del Perfil Bolivia SIE **no se implementa** nada que lo distinga de ese modelo: ni las cinco dimensiones fijas (Ser, Saber, Hacer, Decidir, Autoevaluación), ni `peso_max`, ni el truncado con `floor()`, ni `ConsolidacionDomainService`. Esas piezas quedan **descartadas**, no diferidas. Lo que sí se conserva del dominio boliviano es la integración: el SIE es un destino al que el modelo genérico exporta su nota final de periodo, identificando al estudiante por `rude` (`BR-004`). Si un tenant requiriera un perfil con otra estructura de calificación, sería una decisión nueva con su propio ADR.

**3.1 Un solo motor.** El cálculo de notas vive únicamente en `academico.domain.CalculoNotas`: sección = promedio simple con `round` HALF_UP a 2 decimales; nota de periodo = suma de secciones con `round` HALF_UP a entero; nota de gestión = promedio de periodos con `round` HALF_UP a entero (`ADR-0013` §3.4). `floor()` queda **retirado** del producto vivo. `ConsolidacionDomainService` y el golden `FloorTest` **no se crean**. El golden de redondeo pasa a ser `CalculoNotasTest.redondeoHalfUpNoFloor` (64,5 → 65; `floor` daría 64).

**3.2 Qué recibe el SIE.** Un registro por (estudiante, materia, periodo) con: `rude`, identificador de la materia, identificador del periodo y `notaFinal` (entero). `notaFinal` es la nota de periodo calculada por `CalculoNotas` **en el momento de exportar**, sin conversión de escala. El payload **no** contiene nombre, apellido ni posición de lista (`BR-004` se mantiene).

**3.3 Nota completa.** Una nota de periodo es exportable solo si **todas** las secciones del periodo están `COMPLETO` para ese estudiante y materia (`CalculoNotas` ya clasifica `COMPLETO`/`INCOMPLETO`/`SIN_NOTAS`). Si falta alguna, el registro queda `EXCLUIDO_NOTA_INCOMPLETA` y no se envía. Si el estudiante no tiene un RUDE válido queda `EXCLUIDO_SIN_RUDE`. Ambos estados se reevalúan al volver a pulsar "Exportar" (un docente puede completar la nota después).

**3.4 Cuándo se exporta.** Solo un periodo `CERRADO` es exportable. No existe cierre por materia ni por centralizador: cambiar el estado del periodo a `CERRADO` (`ADR-0014`, `PATCH .../estado`) es el único requisito. Un periodo no `CERRADO` devuelve `422 E_PERIODO_NO_CERRADO`.

**3.5 Clave de idempotencia.** Se enmienda `ADR-0005` §3 y `DA-05`: la clave pasa de `rude + periodo_id` a **`rude + materia_id + periodo_id`**, porque ahora hay una nota por materia. Un registro `ENVIADO` **nunca** se reenvía ni cambia, aunque la nota se recalcule distinta después.

**3.6 Resiliencia.** Se mantiene la decisión de `ADR-0005`: circuit breaker Resilience4j, timeout de 30 s, reintento cada 5 min con estado por registro, procesamiento asíncrono tras el commit. Esta es la primera vez que `resilience4j` entra al `pom.xml`: se añade bajo este ADR, que es el que `AGENTS.md` §4 exige para dependencias nuevas, con versión fijada al ejecutar `PR-IMPL-031` tras verificar compatibilidad con Spring Boot 4.1.0 / Java 25.

**3.7 Diferido explícitamente.** Por decisión del responsable (10/11/2026), no forman parte de esta decisión ni de `DD-UC-031`:

- Inmutabilidad de la nota y de periodos cerrados, `append-only` con `registro_padre_id` (`BR-005`, `RB-10`). Hoy un periodo `CERRADO` rechaza escrituras con `E_PERIODO_NO_ABIERTO`, pero `ADR-0014` permite reabrirlo.
- `AutorizacionCorreccion`, ventanas de 1–72 h, `VentanaExpiracionScheduler`, `FSD-UC-005` (`BR-009`).
- `audit_log` de las escrituras nuevas (`ADR-0009` §3 punto 5, gobernanza). Las entidades de `notassie` siguen el mismo mínimo que `academico`: RLS `FORCE`, filtro por `tenant_id` y RBAC.
- Tabla de centralizador `PROVISIONAL`/`OFICIAL` y promedio anual hacia el SIE (`BR-011` aplica a indicadores institucionales, no a esta exportación).
- Boletines PDF (`FSD-UC-007`) y notificaciones de cierre.
- Cierre de materia por docente (`FSD-UC-002` no se crea).

### 4. Consecuencias

**Positivas.** Un único motor, una única definición de "nota", `AGENTS.md` sin reglas contradictorias. `notassie` pasa de vacío a un módulo pequeño que consume un puerto de `academico`. Se puede entregar el valor central del producto (exportar al SIE sin triple digitación) sin esperar la gobernanza.

**Negativas / riesgos aceptados.**

- Un periodo `CERRADO` puede reabrirse (`ADR-0014`) y la nota modificarse después de que algunos registros ya fueron `ENVIADO`. Esos registros no se reenvían. La diferencia entre EduSync y el SIE no se detecta ni se corrige hasta que exista el flujo de correcciones diferido.
- Sin la especificación real del SIE, el payload y la autenticación son provisionales y el adaptador HTTP no puede validarse contra el sistema real. Se prueba con un puerto falso y con un adaptador simulado solo en `dev`.
- Se invalidan textos del corpus del asistente (`ai/base_conocimiento/exportacion_sie.md`, `calculo_notas.md`) que dicen que `floor()` está reservado al SIE. `PR-IMPL-031` los corrige y ajusta las pruebas que dependan de esa frase.

### 5. Impacto en otros documentos

| Documento | Cambio |
|-----------|--------|
| `docs/product/FSD.md` | `FSD-UC-001` absorbido por `FSD-UC-016`; `FSD-UC-003` reducido a la nota final de periodo; `FSD-UC-004` reescrito; `FSD-UC-005` y cierre por centralizador de `FSD-UC-009` marcados diferidos; `BR-003`/`BR-008`/`RB-08` marcados superados |
| `AGENTS.md` | §4 añade Resilience4j; §6, §8.3, §10, §11 retiran `floor()`/`ConsolidacionDomainService`; append-only, ventana y `audit_log` quedan anotados como diferidos |
| `docs/adr/0005-*.md`, `docs/adr/0013-*.md` | Nota de modificación apuntando a este ADR (el cuerpo histórico no se reescribe) |
| `docs/design/DD-UC-031.md`, `docs/prompts/impl/PR-IMPL-031.md` | Diseño y prompt de la exportación |
| `docs/product/BRD.md` (v3.3), `PRD.md` (v2.4) | `BR-003`/`RB-08` y `PRD-US-011`/`PRD-REQ-010` marcadas superadas; nota de vigencia (propagado con `sync-doc-chain`, 10/11/2026) |
| `docs/LFSD-EduSync.md` (v1.0.2), `docs/dtos_EduSync.md` (v0.1.1), `docs/arquitectura_hexagonal_EduSync.md` (v0.3), `docs/arquitectura_funcional_EduSync.md` | Nota de vigencia; el cuerpo histórico no se reescribe |
| `docs/diagrams/c4_level2.mmd`, `c4_level3_{domain_layer,sie_adapter,api_gateway}.{mmd,md}` | Etiquetas de `floor()` y de la clave de idempotencia corregidas; `.mmd` y `.md` sincronizados (IG-09) |

### 6. Reversión

Costo bajo antes de ejecutar `PR-IMPL-031` (solo documentos). Después, el cálculo no cambia: `CalculoNotas` no se modifica salvo por un método nuevo que exige secciones completas. Revertir implica eliminar `notassie` y la migración `V14`, o añadir un adaptador de conversión de escala detrás de `SieClientPort` si el SIE real lo exigiera. Reintroducir `floor()` solo requeriría un ADR nuevo, porque contradice a este.

### 7. Validación

- `CalculoNotasTest.redondeoHalfUpNoFloor` (64,5 → 65) en verde y nuevo caso "nota de periodo exige todas las secciones completas".
- `SIEPayloadTest.payload_uses_rude_only`: el payload contiene `rude`, materia, periodo y nota, y ningún nombre ni posición.
- Un registro `ENVIADO` no se reenvía al reintentar (prueba de aplicación con puerto falso).
- `ModularityTests` 7/7 con la arista `notassie → academico`/`plataforma`.
- `mvn test` en verde.

### 8. Referencias

`FSD-UC-001`, `FSD-UC-003`, `FSD-UC-004`, `FSD-UC-005`, `FSD-UC-009`, `FSD-UC-016`, `BR-003`, `BR-004`, `BR-005`, `BR-008`, `BR-009`, `RB-08`, `DA-05`, `NFR-007`, `ADR-0005`, `ADR-0009`, `ADR-0013`, `ADR-0014`, `DD-UC-018`, `DD-UC-031`, `PR-IMPL-031`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 10/11/2026 | G-EduSync | Propuesta inicial |
| v0.2 | 10/11/2026 | G-EduSync | Se explicita que el perfil seguido es el modelo genérico (`ADR-0013`): nueva §3.0; el Perfil Bolivia SIE de dimensiones fijas queda descartado (no diferido) y el SIE pasa a ser solo un destino de exportación. Alcance, contexto y Alternativa A ajustados. La decisión técnica (§3.1–§3.7) no cambia |
