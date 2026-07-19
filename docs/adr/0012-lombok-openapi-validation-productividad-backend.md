# Architecture Decision Record (ADR)

## ADR-0012: Herramientas de productividad backend — Lombok (incl. `domain/` bajo *allowlist*), springdoc-openapi (Swagger) y Bean Validation

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0012` |
| Título | Adopción de Lombok (sin restricción en `infrastructure`/`application`; en `domain/` restringido a un *allowlist* de anotaciones compatibles con Aggregate Roots inmutables), springdoc-openapi para documentación viva de la API REST, y `spring-boot-starter-validation` para DTOs de entrada |
| Fecha | 19/07/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | `backend/` — todos los módulos Spring Modulith (`identidad`, `plataforma`, `academico`, `notassie`, `shared`), capas `domain`, `application` e `infrastructure`. No afecta `docs/baseline/` ni el modelo de dominio decidido en `ADR-0009`/`ADR-0010`. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | `ADR-0008` (stack Java 25 LTS / Spring Boot 4.1.0), `ADR-0010` (multi-rol + invariante `Usuario`), `ADR-0011` (monolito modular Spring Modulith, paquete `com.edusync`) |

### 1. Contexto

`PR-IMPL-001`/`PR-IMPL-002` (bootstrap + módulo `identidad`) ya establecieron un patrón de código consistente con `AGENTS.md` §5: DTOs de API como `record` en `infrastructure/adapter/in/rest/` (`LoginRequest`, `LoginResponse`, `ErrorResponse`), entidades JPA con boilerplate manual (`UsuarioJpaEntity`, `UsuarioRolJpaEntity`: constructor protegido vacío + getters escritos a mano) y un Aggregate Root (`Usuario`) inmutable, con constructor privado y factory methods (`crear()`/`reconstruir()`) que validan la invariante permanente de `ADR-0010` (`tenant_id IS NULL ⟺ roles == {SYSADMIN}`), expuesto con accessors de estilo fluido sin prefijo (`id()`, `tenantId()`, etc.).

A medida que se añadan más módulos (`plataforma` en `PR-IMPL-003`, luego `academico`), este patrón de boilerplate se repite linealmente: cada Aggregate Root nuevo necesita el mismo tipo de accessors, cada entidad JPA nueva necesita el mismo constructor protegido + getters, y cada endpoint REST nuevo necesita el mismo patrón de DTO. Al mismo tiempo, no existe ninguna forma de que un consumidor de la API (frontend Angular, o un tercero en `release/3.0.0`+) descubra los contratos REST sin leer el código fuente, y los DTOs de entrada (`LoginRequest`) no validan formato (`email` vacío o inválido pasa sin error hasta llegar al dominio).

Ninguna de las tres herramientas propuestas (Lombok, springdoc-openapi, `spring-boot-starter-validation`) está en la tabla de stack autoritativa de `AGENTS.md` §4, que exige explícitamente un ADR + aprobación humana antes de introducir cualquier dependencia nueva.

Restricción de diseño relevante (`AGENTS.md` §5, texto previo a este ADR): *"El paquete `domain/` **MUST NOT** importar de `infrastructure/` ni de frameworks externos (Spring, JPA, AWS). Solo interfaces puras."* Este ADR revisa el alcance de esa regla para distinguir entre (a) frameworks que imponen comportamiento o ciclo de vida en tiempo de ejecución (Spring, JPA, AWS — lo que la regla original quiere prevenir) y (b) procesadores de anotaciones sin huella en runtime (Lombok), que no acoplan el dominio a ningún contenedor ni ORM. La decisión de este ADR incluye actualizar la redacción de `AGENTS.md` §5 para reflejar esta distinción de forma explícita.

### 2. Alternativas consideradas

**Para reducir boilerplate de persistencia/aplicación/dominio:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener todo el código a mano (estado actual) | Cero dependencias nuevas; cero riesgo de "magia" de anotaciones sobre el equipo (aunque el equipo es de 1 persona) | El boilerplate de entidades JPA (constructor protegido + N getters) y de Aggregate Roots (accessors) escala linealmente con cada módulo nuevo (`plataforma`, `academico`); mayor superficie de error humano en mappers | Bajo hoy; creciente con cada módulo nuevo |
| B. Lombok en `infrastructure`/`application` sin restricción de anotaciones; `domain/` sin Lombok, 100 % código manual | `domain/` queda con cero imports de terceros bajo cualquier lectura de `AGENTS.md` §5, incluida la más literal | El ahorro real es parcial: los Aggregate Roots (`Usuario` y los que vendrán en `plataforma`/`academico`) siguen escribiendo a mano ~15-20 líneas de accessors por clase, repitiéndose módulo tras módulo | Bajo — una dependencia menos usada de lo posible |
| **C. Lombok en `infrastructure`/`application` sin restricción; en `domain/` permitido bajo *allowlist* estrecho** (`@Getter` con nomenclatura JavaBean estándar — `getId()`, `getTenantId()`, `isActivo()` —, `@EqualsAndHashCode`, `@ToString`; **prohibidos siempre** `@Data`, `@Setter`, `@Builder`/`@AllArgsConstructor` con acceso público) | Elimina el boilerplate de accessors en Aggregate Roots sin habilitar mutación no controlada; el constructor privado y los factory methods con validación de invariante (`crear()`/`reconstruir()`) se siguen escribiendo siempre a mano — Lombok no participa en la validación de negocio; los accessors quedan en convención JavaBean estándar, compatible con librerías de serialización/introspección que la esperan (ej. frameworks de mapeo, Jackson si se necesitara en el futuro) | Requiere una regla explícita y auditable (no solo "Lombok sí/no", sino "qué anotación específica"); exige actualizar la redacción de `AGENTS.md` §5 | Bajo — el riesgo se controla con la lista blanca, no con la prohibición total |
| D. Lombok en `domain/` sin ninguna restricción (incluye `@Data`/`@Setter`/`@Builder` público) | Máxima reducción de boilerplate | Habilita mutación no controlada de Aggregate Roots (`usuario.setRoles(...)` saltándose `Usuario.crear()`), rompiendo por diseño el enforcement de la invariante permanente de `ADR-0010`; regresión real de DDD, no solo un tema de estilo | Alto — reintroduce exactamente la clase de bug que la validación centralizada en `crear()` existe para prevenir |

**Para documentación de API:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| E. Sin documentación automática (estado actual); README/Markdown manual de endpoints | Cero dependencias nuevas | Documentación manual se desincroniza del código rápidamente; sin forma de que el frontend Angular (o un tercero) explore la API interactivamente | Bajo hoy; alto costo de mantenimiento manual a mediano plazo |
| F. springdoc-openapi (`springdoc-openapi-starter-webmvc-ui`), generación automática desde anotaciones + reflexión sobre los controladores/DTOs existentes | Documentación siempre sincronizada con el código; UI interactiva (`/swagger-ui.html`) útil para probar el login/futuros endpoints de Tenants sin Postman; soportado activamente para Spring Boot 4.x (rama `3.x.x`, confirmada compatible) | Superficie de exposición nueva a proteger | Bajo — una dependencia + una clase de configuración |

**Para validación de entrada:**

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| G. Validación manual dentro de cada servicio de aplicación | Cero dependencias nuevas | Repetitivo, propenso a inconsistencia entre módulos; no declarativo | Bajo hoy; alto en consistencia a mediano plazo |
| H. `spring-boot-starter-validation` (Bean Validation / Jakarta Validation) + `@Valid` en los controladores | Declarativo (`@NotBlank`, `@Email`, `@Size` en el `record` del DTO); falla temprano en el borde de la API; estándar Jakarta EE 11 | Requiere un `@ExceptionHandler(MethodArgumentNotValidException.class)` común en `shared/` para normalizar al formato `ErrorResponse{codigo, mensaje}` ya usado | Bajo — una dependencia + un handler de excepción compartido |

### 3. Decisión

> **Elegimos la Alternativa C (Lombok en `infrastructure`/`application` sin restricción; en `domain/` bajo *allowlist* estilo JavaBean) + la Alternativa F (springdoc-openapi) + la Alternativa H (`spring-boot-starter-validation`)**.

**Alcance exacto de Lombok** (regla que todo PR de implementación futuro **MUST** respetar):

- **`infrastructure`/`application` — permitido sin restricción de anotaciones**: entidades JPA (`infrastructure/adapter/out/persistence/*JpaEntity.java`) — `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`/`@Builder` según convenga; servicios de aplicación (`application/service/*.java`) — `@RequiredArgsConstructor` sobre los campos `final` inyectados.
- **`domain/` — permitido únicamente bajo *allowlist*, estilo JavaBean estándar**: `@Getter` (nomenclatura por defecto de Lombok: `getId()`, `getTenantId()`, `getNombreCompleto()`, `getEmail()`, `getPasswordHash()`, `isActivo()` para el campo `boolean activo`) sobre Aggregate Roots (`Usuario` y los que se creen en `plataforma`/`academico`). Métodos derivados que transforman el tipo del campo (ej. `Usuario.roles()`, que expone `Set<Rol>` a partir del campo interno `Set<UsuarioRol> roles`) **MUST NOT** generarse con Lombok — se escriben a mano y se renombran a convención JavaBean (`getRoles()`) por consistencia, igual que `tieneRol(Rol)` permanece manual. `@EqualsAndHashCode`/`@ToString` si el Aggregate Root los necesita explícitamente. El constructor privado y los factory methods con validación de invariante (`crear()`/`reconstruir()`) **MUST** seguir escribiéndose a mano en todos los casos — la inmutabilidad (campos `final`, sin setters) se mantiene sin cambios; solo cambia la convención de nombres de los accessors, de estilo fluido (`id()`) a JavaBean (`getId()`).
- **`domain/` — prohibido siempre, sin excepción**: `@Data`, `@Setter`, `@Builder`/`@AllArgsConstructor` con acceso público o `PACKAGE`, y cualquier anotación de Lombok sobre un `record` ya existente (`UsuarioId`, `UsuarioRol`, y cualquier Value Object futuro) — los `record` de Java ya resuelven lo que Lombok resolvía antes de Java 14; mezclar ambos mecanismos en el mismo tipo es inconsistente y no aporta valor.
- Se actualiza `AGENTS.md` §5 (regla de dominio) para dejar de leerse como prohibición total de imports de terceros en `domain/`, y pasar a leerse como: *"El paquete `domain/` **MUST NOT** importar de `infrastructure/` ni depender de frameworks que impongan comportamiento o ciclo de vida en runtime (Spring, JPA, AWS). Procesadores de anotaciones sin huella en runtime están permitidos bajo el *allowlist* de `ADR-0012` (Lombok: `@Getter`/`@EqualsAndHashCode`/`@ToString`, siempre con nomenclatura JavaBean estándar; nunca `@Data`/`@Setter`/`@Builder` público, que evadirían la validación de invariantes de los Aggregate Roots)."* Se añade la misma nota en `docs/arquitectura_hexagonal_EduSync.md`.

**Versiones** (a fijar en `pom.xml`, verificadas contra Java 25 LTS / Spring Boot 4.1.0 / Jakarta EE 11):

- `org.projectlombok:lombok:1.18.46` (scope `provided`) — primera versión con soporte oficial de JDK 25 es 1.18.40; se fija la última estable (1.18.46, incluye soporte JDK 26) para evitar bugs conocidos de `val`/anotaciones en JDK 25 corregidos después de 1.18.40.
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3` — rama `3.x.x` da soporte a Spring Boot `4.x.x` (`2.x.x` es exclusiva de Boot 3). Build oficial contra Boot 4.0.5; se verifica en la ejecución de este ADR que no hay incompatibilidad con 4.1.0 (ver §7).
- `spring-boot-starter-validation` — sin versión explícita, gestionada por el BOM `spring-boot-starter-parent:4.1.0` ya declarado en `pom.xml`.

**Decisión de seguridad — exposición de Swagger UI**: público (`permitAll`) en todos los perfiles actuales (`dev`/`test`, únicos perfiles existentes hoy); cuando se cree un perfil de producción real, `MUST` revisarse si se restringe (autenticación + rol `SYSADMIN`, o deshabilitado vía `springdoc.swagger-ui.enabled=false`). Se documenta esta pendiente explícitamente en `SecurityConfig` y en `application.yml`.

### 4. Consecuencias

#### 4.1 Positivas

- El boilerplate de accessors en Aggregate Roots (`Usuario` y sucesores en `plataforma`/`academico`) se reduce sin habilitar mutación no controlada, gracias al *allowlist* de anotaciones.
- El boilerplate mecánico de entidades JPA y servicios de aplicación se reduce de forma medible en cada módulo nuevo, sin tocar la capa que el proyecto más protege (`domain/` sigue sin setters ni construcción pública que evada `crear()`).
- La documentación de la API REST (login hoy; Tenants/Admins en `PR-IMPL-003`, académico después) queda siempre sincronizada con el código.
- Los DTOs de entrada ganan validación declarativa y consistente (`@NotBlank`, `@Email`) en el borde de la API.
- La frontera explícita "Lombok con *allowlist* en `domain`, sin restricción en `infrastructure`/`application`" es una regla simple de auditar en code review (`grep -r "import lombok" domain/` debe dar únicamente `lombok.Getter`, `lombok.EqualsAndHashCode` o `lombok.ToString`).

#### 4.2 Negativas / costos

- Tres dependencias nuevas que requieren verificación de compatibilidad en cada bump de Spring Boot futuro.
- El *allowlist* de Lombok en `domain/` exige disciplina de code review más fina que un simple "sí/no" — hay que verificar qué anotación específica se usó, no solo si se importó Lombok.
- Lombok requiere que cualquier IDE usado por el equipo tenga el plugin instalado para que el código generado no aparezca con errores falsos.
- Exposición de `/v3/api-docs`/`/swagger-ui.html` es una superficie nueva que debe evaluarse contra OWASP ASVS L2 (`.cursor/rules/seguridad.mdc`) antes de cualquier despliegue no-local.

#### 4.3 Neutras / observables

- No se crea ningún ADR que "supersede" a `ADR-0008`/`ADR-0009`/`ADR-0010`/`ADR-0011`; los cuatro permanecen `Aceptada` sin cambios. Este ADR es aditivo y afinado (Lombok se permite en `domain/` bajo condiciones estrechas, no de forma libre).
- Los DTOs ya existentes (`LoginRequest`, `LoginResponse`, `ErrorResponse`, `CrearUsuarioCommand`, `UsuarioId`, `UsuarioRol`) no requieren Lombok — siguen siendo `record`; solo ganan anotaciones de validación donde aplique (`LoginRequest`).

### 5. Impacto en el sistema

- **Documentación**: `AGENTS.md` §4 (tabla de stack) añade una fila para Lombok/springdoc-openapi/Bean Validation; §5 (convenciones de código) añade la regla de frontera de Lombok (JavaBean estándar en `domain/`, sin restricción en `infrastructure`/`application`). `docs/product/DTP.md` §A.2 registra este ADR como delta transversal (no ligado a un único `FSD-UC`).
- **Código**: `backend/pom.xml` (3 dependencias nuevas); refactor de `Usuario.java` (Lombok `@Getter` JavaBean + `getRoles()` manual) y sus 7 sitios de llamada (`JwtTokenProvider`, `JwtTokenProviderTest`, `UsuarioRepositoryAdapter`, `AutenticarUsuarioService`, `UsuarioTest`, `CrearUsuarioService`); Lombok en `UsuarioJpaEntity`, `UsuarioRolJpaEntity`, `AutenticarUsuarioService`, `CrearUsuarioService`; `@Valid` + anotaciones en `LoginRequest`; `@ExceptionHandler(MethodArgumentNotValidException.class)` común; nueva clase de configuración `OpenApiConfig`.
- **Seguridad**: Swagger UI público en los perfiles actuales (`dev`/`test`); pendiente explícita de revisión cuando exista un perfil de producción. Sin impacto en JWT/RBAC ya decidido (`ADR-0010`).
- **Equipo**: cualquier Design Doc (`DD-UC-NNN`) que cree una entidad JPA, un servicio de aplicación o un Aggregate Root nuevo debe usar Lombok según el alcance de §3; cualquier PR que use `@Data`/`@Setter`/`@Builder` público dentro de un paquete `domain/` **MUST** rechazarse en review.
- **Costo**: sin impacto en infraestructura ni en factura AWS; Lombok es `scope=provided`; springdoc añade un JAR pequeño en runtime.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si aparece `@Data`, `@Setter` o `@Builder` público en un Aggregate Root de `domain/` (violación del *allowlist* — revertir ese PR específico, no la decisión completa); si springdoc-openapi genera documentación incorrecta/incompleta; si la validación declarativa de Bean Validation resulta insuficiente para reglas de negocio complejas (que de todas formas **MUST** seguir viviendo en el dominio, no en `@Valid`).
- **Costo estimado de revertir**: bajo en los tres casos — Lombok es un *refactor* mecánico soportado por el IDE (expandir anotaciones a código explícito); springdoc y Bean Validation son aditivos, remover la dependencia no rompe el resto del flujo.
- **Plan B**: si Lombok resulta problemático incluso en el *allowlist* de `domain/`, se documenta un ADR de seguimiento que reduce el alcance de vuelta a la Alternativa B (Lombok solo en `infrastructure`/`application`).

### 7. Validación

- **Revisión de consistencia documental**: `AGENTS.md` §4/§5 y `docs/arquitectura_hexagonal_EduSync.md` citan este ADR y reflejan el *allowlist* de Lombok.
- **Checklist de no divergencia**: `grep -r "import lombok" backend/src/main/java/*/domain/` debe devolver únicamente `lombok.Getter`, `lombok.EqualsAndHashCode` o `lombok.ToString` — cualquier otro import de `lombok.*` en `domain/` **MUST** fallar la revisión.
- **Verificación automatizada**: `mvn test` (incluye `ModularityTests`) en verde tras el refactor de `identidad` con Lombok; `mvn -q verify` genera el JAR sin warnings de anotaciones nuevas; smoke test manual de `/swagger-ui.html` mostrando el endpoint de login documentado con su `SecurityScheme` Bearer.

### 8. Referencias

- `ADR-0008` (stack Java 25 LTS / Spring Boot 4.1.0 — Lombok 1.18.40+ y springdoc-openapi 3.x.x son compatibles con esta versión).
- `ADR-0010` (invariante permanente de `Usuario`; el *allowlist* de Lombok en `domain/` preserva esta invariante al prohibir `@Setter`/`@Builder` público).
- `ADR-0011` (monolito modular Spring Modulith; la frontera de Lombok respeta los límites de módulo ya establecidos).
- `AGENTS.md` §4 (tabla de stack autoritativa, exige este ADR antes de introducir la dependencia), §5 (regla de dominio, redacción actualizada por este ADR).
- springdoc-openapi FAQ — matriz de compatibilidad Spring Boot ↔ springdoc-openapi (`4.x.x` ↔ `3.x.x`).
- Lombok changelog — soporte JDK 25 introducido en v1.18.40, consolidado en v1.18.46.
- `docs/design/DD-UC-002.md` (primer módulo con código de dominio real al que se aplica este ADR retroactivamente).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 19/07/2026 | Rodrigo Aspeti | Aceptada — adopción de Lombok (sin restricción en `infrastructure`/`application`; en `domain/` bajo *allowlist* JavaBean: `@Getter`/`@EqualsAndHashCode`/`@ToString`, nunca `@Data`/`@Setter`/`@Builder` público), springdoc-openapi 3.0.3 y `spring-boot-starter-validation` como herramientas de productividad backend. Decisión refinada en dos rondas con el usuario: primero se propuso excluir Lombok de `domain/` por completo, luego se acordó permitirlo bajo un *allowlist* estrecho con nomenclatura JavaBean estándar (no fluida) para que los Aggregate Roots sean POJOs inmutables convencionales. Swagger UI público en todos los perfiles actuales (`dev`/`test`), pendiente de revisión cuando exista un perfil de producción. |
