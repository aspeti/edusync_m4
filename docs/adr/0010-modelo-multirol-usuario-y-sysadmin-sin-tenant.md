# Architecture Decision Record (ADR)

## ADR-0010: Modelo de roles múltiples por usuario y aislamiento permanente del rol SysAdmin sin tenant

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0010` |
| Título | Modelo de roles múltiples por usuario (`usuario_rol` N:M) y aislamiento permanente del rol `SYSADMIN` sin `tenant_id` |
| Fecha | 14/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Modelo funcional y de dominio de la capa viva (`docs/product/BRD.md`, `docs/product/PRD.md`, `docs/product/FSD.md`), en particular `BR-024`/`FSD-UC-021` (Usuarios y Roles) y el actor `SYSADMIN` introducido por `ADR-0009`. No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`), que permanece intacto. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0009` (generalización del modelo de dominio; introduce el rol `SYSADMIN` y `BR-024` con la restricción original "exactamente un rol por usuario") |

### 1. Contexto

`ADR-0009` generalizó el modelo de roles de EduSync introduciendo `SYSADMIN` (plataforma) junto a los roles de tenant (`ADMIN`/`SECRETARIA`/`ASESOR`/`PROFESOR`), y fijó en `BR-024` la regla "todo `Usuario` activo tiene exactamente un rol vigente", modelada como un atributo `Usuario.rol` de tipo `ENUM` de un solo valor (`docs/product/FSD.md` §6.3.2).

Durante el diseño del flujo de login y de alta de tenants surgieron dos necesidades que ese modelo de "un rol por usuario" no puede expresar:

1. **Multi-rol operativo**: dentro de un mismo tenant, una persona real frecuentemente cubre más de una función (ej. quien administra la institución también actúa como Secretaría en colegios pequeños; un Profesor puede ser también Asesor de su propio curso). Forzar un único rol por `Usuario` obligaría a crear cuentas duplicadas para la misma persona, rompiendo la trazabilidad de `audit_log` (`actor_id` dejaría de identificar unívocamente a la persona).
2. **Alcance del rol `SYSADMIN`**: quedó una ambigüedad sobre si el `tenant_id` nulo de un `SYSADMIN` es una condición **transitoria** (solo mientras no se le asocia a ningún tenant, ej. durante el *bootstrap* inicial del sistema, antes de que exista el primer tenant) o **permanente** (un `SYSADMIN` nunca pertenece a un tenant, ni siquiera después de crearse el primer tenant). Esta decisión afecta directamente el diseño de `TenantContextProvider`/JWT del login y el script de *seed* del primer usuario de plataforma.

Adicionalmente, se confirmó que habrá un **tenant "demo"** (el primer tenant registrado en el sistema) que funcionará como una funcionalidad de producto real — un sandbox para prospectos de venta —, y no solo como un artefacto técnico de *seed*. El diseño detallado de ese tenant demo (cómo se crea, si tiene reglas especiales de datos) queda **fuera de alcance de este ADR**: no afecta el modelo de `Usuario`/`Rol`/`tenant_id` decidido aquí, y se resolverá en el Design Doc de `FSD-UC-011` cuando se aborde su implementación.

### 2. Alternativas consideradas

**Para el multi-rol:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener `Usuario.rol` de un solo valor y añadir un flag booleano independiente `es_sysadmin` para representar la capacidad adicional de plataforma | Cambio mínimo sobre el modelo de `ADR-0009`; migración trivial | No resuelve el caso general de multi-rol dentro de un mismo tenant (ej. Admin+Secretaria); solo cubre el caso puntual de SysAdmin | Bajo a corto plazo; insuficiente para el requisito real |
| B. Reemplazar `Usuario.rol` (enum simple) por una relación `usuario_rol` N:M (`usuario_id`, `rol`), permitiendo cero o más roles por usuario | Resuelve el caso general de multi-rol para cualquier combinación de roles de tenant; el JWT lleva `roles: [...]` en vez de `rol: "..."`, patrón estándar de Spring Security (`hasAnyRole`) | Requiere ajustar el diseño de JWT/RBAC ya planificado para el login (de rol único a lista de roles); exige definir explícitamente qué combinaciones son válidas (para no permitir, por ejemplo, `SYSADMIN` + un rol de tenant sin una regla que lo respalde) | Medio — cambio de esquema antes de escribir código (`src/` sigue vacío), sin costo de migración de datos reales |

**Para el alcance de `tenant_id` nulo en `SYSADMIN`:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| C. Transitorio: `tenant_id` nulo solo hasta que el `SYSADMIN` se asocie a un tenant (ej. al tenant demo tras el *bootstrap*); a partir de ahí, el `SYSADMIN` "pertenece" a ese tenant conservando su capacidad de plataforma | Evita tener usuarios "huérfanos" de tenant en régimen normal | Con un `Usuario.tenant_id` de valor único, asociar a un tenant obligaría a decidir qué pasa con el rol `SYSADMIN` (¿convive con un rol de tenant en la misma fila?); reabre la pregunta de multi-rol cruzado plataforma/tenant sin necesidad real confirmada | Medio — exige diseño adicional no solicitado |
| D. Permanente: `tenant_id` es nulo **si y solo si** el conjunto de roles del usuario es exactamente `{SYSADMIN}`; un `SYSADMIN` nunca se asocia a ningún tenant, ni siquiera al tenant demo. Roles de tenant y `SYSADMIN` son mutuamente excluyentes por diseño de datos mientras `Usuario.tenant_id` sea un único campo | Regla simple, verificable con una única invariante de base de datos; no depende de decisiones futuras sobre el tenant demo; el *bootstrap* y el régimen normal usan exactamente la misma regla (sin caso transitorio especial) | Si en el futuro se quisiera que la misma persona sea `SYSADMIN` y además `ADMIN` de un tenant, se necesitaría un rediseño (separar identidad de membresía en una tabla `usuario_tenant_rol` con `tenant_id` por fila) — deuda de diseño explícita, no bloqueante hoy | Bajo — decisión explícita confirmada por el negocio; el rediseño futuro queda anotado como posible ADR de seguimiento, no se construye ahora |

### 3. Decisión

> **Elegimos la Alternativa B (multi-rol vía `usuario_rol` N:M) combinada con la Alternativa D (`tenant_id` nulo permanente y exclusivo de `SYSADMIN`)**, confirmadas explícitamente por el negocio.

Regla de modelado resultante, que sustituye a la redacción original de `BR-024` (`ADR-0009`):

1. `Usuario` puede tener **uno o más roles simultáneos**, modelados mediante una relación `usuario_rol(usuario_id, rol)` en lugar de un atributo `Usuario.rol` de valor único. Los roles posibles siguen siendo `SYSADMIN`/`ADMIN`/`SECRETARIA`/`ASESOR`/`PROFESOR` (`ADR-0009`).
2. **Invariante de exclusión mutua:** `Usuario.tenant_id IS NULL` **si y solo si** el conjunto de roles de ese usuario es exactamente `{SYSADMIN}`. En consecuencia:
   - Un usuario con `tenant_id` nulo únicamente puede tener el rol `SYSADMIN` (no puede combinarse con ningún rol de tenant).
   - Un usuario con `tenant_id` no nulo puede tener una o más roles de tenant (`ADMIN`/`SECRETARIA`/`ASESOR`/`PROFESOR`) combinados libremente entre sí, pero **nunca** el rol `SYSADMIN`.
3. Esta invariante es **permanente**, no transitoria: no existe un flujo que "reasigne" a un `SYSADMIN` a un tenant (ni al tenant demo ni a ningún otro) conservando su rol `SYSADMIN`. El *bootstrap* del primer `SYSADMIN` (creado antes de que exista cualquier tenant, con `tenant_id = NULL`) usa exactamente la misma invariante que el régimen normal, sin caso especial.
4. El JWT emitido en el login lleva `roles: string[]` (no un `rol` singular). Si `roles` incluye `SYSADMIN`, el token no lleva `tenantId` (o va `null`); en caso contrario, `tenantId` es obligatorio.

Queda **explícitamente pendiente de definición** (fuera de alcance de este ADR, a resolver en un ADR de seguimiento si el negocio lo requiere en el futuro):

- Separar identidad de membresía (permitir que la misma persona sea `SYSADMIN` y, además, tenga un rol de tenant en uno o más tenants) exigiría dejar de modelar `tenant_id` como un campo único de `Usuario` y pasar a una tabla `usuario_tenant_rol` con `tenant_id` por fila. No se construye ahora porque no hay una necesidad de negocio confirmada.
- El diseño detallado del **tenant demo** como funcionalidad de producto (alta única vs. bajo demanda, reglas de reseteo/expiración de datos) — no afecta el modelo decidido aquí y se resuelve en el Design Doc de `FSD-UC-011`.

### 4. Consecuencias

#### 4.1 Positivas

- El modelo soporta el caso real de una persona con múltiples funciones dentro de un mismo tenant sin duplicar cuentas ni comprometer la trazabilidad de `audit_log` (`actor_id` sigue identificando unívocamente a la persona, ahora con su conjunto de roles).
- La regla de aislamiento de `SYSADMIN` queda simple y verificable con una única invariante (`tenant_id IS NULL ⟺ roles = {SYSADMIN}`), sin casos especiales de *bootstrap* vs. régimen normal.
- El diseño de JWT/`TenantContextProvider` para el login (pendiente de implementación) queda desbloqueado: `roles: string[]` + `tenantId: string | null` es suficiente para todos los casos.
- El tenant demo (funcionalidad de producto confirmada) no bloquea esta decisión: puede diseñarse después sin reabrir el modelo de `Usuario`/`Rol`.

#### 4.2 Negativas / costos

- `BR-024` deja de tener la redacción simple "exactamente un rol"; el texto y su métrica de aceptación deben actualizarse en `docs/product/BRD.md` y `docs/product/FSD.md` para reflejar "uno o más roles, con la exclusión mutua `SYSADMIN`/tenant".
- El modelo de datos de `docs/product/FSD.md` §6.3.2 pasa de un atributo `Usuario.rol` (ENUM) a una entidad `UsuarioRol` (N:M); esto afecta el diagrama ER (§6.3.1) y el flujo de `FSD-UC-021` (`POST /api/v1/usuarios` recibe `roles: [...]` en lugar de `rol`).
- Si en el futuro el negocio pide que un `SYSADMIN` también sea `ADMIN` de un tenant, se necesitará un ADR de seguimiento y un cambio de esquema (tabla `usuario_tenant_rol`); no se resuelve preventivamente ahora.

#### 4.3 Neutras / observables

- No se crea ningún ADR que "supersede" a `ADR-0009`; permanece `Aceptada` sin cambios en su contenido. Este ADR-0010 **refina** la redacción de `BR-024` que `ADR-0009` dejó abierta a interpretación de "un rol", sin contradecir el resto de la decisión de `ADR-0009` (capa de plataforma SaaS, roles de tenant, módulos configurables).
- El directorio `src/` sigue vacío (greenfield): este ADR es puramente de modelo funcional/de dominio documental, sin impacto en código todavía.

### 5. Impacto en el sistema

- **Documentación**: `docs/product/BRD.md` (`BR-024` reescrito), `docs/product/FSD.md` (§3.1 nota sobre `SYSADMIN`, §5.1 `BR-024` reescrito, §6.3.1 diagrama ER con `USUARIO_ROL`, §6.3.2 diccionario de datos con la entidad `UsuarioRol`, §4.6.11 `FSD-UC-021` con `roles: [...]`, §14 glosario), `docs/product/PRD.md` (`PRD-REQ-031` reescrito). `docs/product/DTP.md` §A.1/§A.2 registra este ADR como delta.
- **Código**: sin impacto directo (`src/` vacío). Cuando se implemente el login (`FSD-UC-001`/autenticación) y `FSD-UC-021`, el JWT y el `SecurityContext` deben modelar `roles` como colección, no como valor único.
- **Seguridad**: la invariante `tenant_id IS NULL ⟺ roles = {SYSADMIN}` debe validarse a nivel de aplicación (no solo de UI) en el alta y en cualquier modificación de roles de un `Usuario`, para no permitir combinaciones inválidas (`SYSADMIN` + rol de tenant).
- **Equipo**: cualquier Design Doc (`DD-UC-NNN`) que aborde `FSD-UC-021` o el login debe citar explícitamente este ADR para el modelo de roles.
- **Costo**: sin impacto en infraestructura.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si en la práctica surge una necesidad de negocio confirmada de que la misma persona sea `SYSADMIN` y, a la vez, `ADMIN`/otro rol de un tenant específico (no solo hipotética).
- **Costo estimado de revertir**: bajo — no hay código productivo (`src/` vacío); revertir implica migrar de `Usuario.tenant_id` único a una tabla `usuario_tenant_rol` con `tenant_id` por fila, y actualizar la documentación viva sin afectar al baseline (`docs/baseline/`, intacto por diseño).
- **Plan B**: si esa necesidad se confirma, crear un ADR de seguimiento (`ADR-0011` o siguiente disponible) que introduzca `usuario_tenant_rol`, sin reabrir este ADR-0010 ni `ADR-0009`.

### 7. Validación

- **Revisión de consistencia documental**: `docs/product/BRD.md`, `PRD.md` y `FSD.md` deben citar este ADR junto a `ADR-0009` en toda referencia a `BR-024`/`FSD-UC-021`/roles.
- **Checklist de no divergencia**: `BR-001`..`BR-023` y el resto de `ADR-0009` no quedan editados; solo se reescribe `BR-024` y sus artefactos derivados directos (`PRD-REQ-031`, `FSD-UC-021`, §6.3.1/§6.3.2).
- **Verificación de la invariante**: antes de implementar `FSD-UC-021` en código, debe existir un test (unitario o de integración) que verifique que ningún `Usuario` puede persistirse con `tenant_id` nulo y un rol distinto de `SYSADMIN`, ni con `tenant_id` no nulo y el rol `SYSADMIN` presente.

### 8. Referencias

- `ADR-0009` (generalización del modelo de dominio; introduce `SYSADMIN` y la redacción original de `BR-024`, refinada por este ADR).
- `plantillas/plantillas3/MODELO_DOCUMENTAL_IMPLEMENTACION.md` (regla de oro de cero divergencia silenciosa).
- `docs/product/DTP.md` §A.2 (delta registrado a partir de este ADR).
- `docs/product/BRD.md`, `PRD.md`, `FSD.md` (documentos actualizados a partir de esta decisión).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 14/07/2026 | Rodrigo Aspeti | ADR formal creado a partir de la clarificación de negocio sobre multi-rol y alcance del rol `SYSADMIN` recibida durante el diseño del login y del alta de tenants; refina `BR-024` (`ADR-0009`) sin contradecir el resto de esa decisión; deja pendiente de definición (fuera de alcance) el diseño detallado del tenant demo y la posible combinación futura `SYSADMIN` + rol de tenant; estado Aceptada |
