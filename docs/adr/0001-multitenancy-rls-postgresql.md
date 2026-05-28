# Architecture Decision Record (ADR)

## ADR-0001: Multitenancy mediante Row-Level Security en PostgreSQL 15

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0001` |
| Título | Multitenancy mediante Row-Level Security en PostgreSQL 15 |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Todo el sistema — todas las tablas con datos sensibles de tenant |
| Stakeholders consultados | Directores de unidades educativas (Jeanneth), Equipo de arquitectura G-EduSync |

### 1. Contexto

EduSync atiende a múltiples colegios (tenants) sobre la misma infraestructura compartida en AWS. Cada unidad educativa boliviana opera con aislamiento total de datos: ningún docente, secretaria ni director puede ver ni modificar datos de otra institución, incluso si comparte la misma instancia de base de datos.

La fuga de datos entre colegios constituye un riesgo legal de primer nivel bajo la normativa boliviana de protección de datos y un riesgo de negocio crítico (pérdida de confianza institucional). Al mismo tiempo, operar una instancia de base de datos por colegio haría inviable la operación económica del modelo SaaS para el mercado boliviano, donde los colegios son mayoritariamente pequeños (< 500 estudiantes).

La estrategia de aislamiento debe balancear tres fuerzas en tensión:

- **Aislamiento suficiente**: que ninguna consulta acceda a datos de otro tenant, ni siquiera ante errores de programación.
- **Simplicidad operativa**: un solo equipo de desarrollo (Rodrigo + agentes IA) debe poder mantener migraciones Flyway y operar la BD.
- **Costo operativo**: el precio de la solución por tenant debe ser viable para colegios privados y de convenio en Bolivia.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Schema separado por tenant en la misma instancia | Aislamiento alto; búferes independientes por schema | Migraciones Flyway duplicadas por schema (N schemas × M migraciones); gestión de conexiones compleja; overhead de mantenimiento | Medio — requiere pool de conexiones por schema |
| B. Discriminador `tenant_id` en tablas compartidas + Row-Level Security (RLS) de PostgreSQL 15 | Migraciones únicas; una sola instancia RDS; Spring Security inyecta `tenant_id` en el contexto; escalable a schema separado sin cambiar el modelo de dominio | Requiere que la política RLS esté activa en TODAS las tablas sensibles; un bug de configuración puede silenciar el aislamiento | Bajo — una sola instancia RDS Multi-AZ |
| C. Base de datos separada por tenant | Aislamiento máximo; RPO/RTO independiente por tenant | Costo prohibitivo en etapa temprana; provisioning automático complejo; N instancias RDS × N tenants | Muy alto — inviable para el mercado boliviano actual |

### 3. Decisión

> **Elegimos la Alternativa B: discriminador `tenant_id` en tablas compartidas con Row-Level Security de PostgreSQL 15 habilitado en todas las tablas sensibles.**

Spring Security garantiza que el `tenant_id` del token JWT se inyecte en el contexto de cada consulta mediante `TenantContextProvider`, que a su vez configura la variable de sesión PostgreSQL `app.current_tenant`. Las políticas RLS de PostgreSQL filtran automáticamente toda lectura y escritura al `tenant_id` activo, convirtiendo el aislamiento en una restricción a nivel de motor de base de datos —no solo de aplicación.

Esta decisión permite arrancar con un equipo pequeño, mantener un único juego de migraciones Flyway y escalar a schema separado por tenant en una versión futura sin cambiar el modelo de dominio, solo la estrategia de routing de conexión.

### 4. Consecuencias

#### 4.1 Positivas

- Un único script de migración Flyway vale para todos los tenants; los cambios de esquema se despliegan en minutos.
- El aislamiento funciona como salvaguarda en profundidad: incluso si el código de aplicación tiene un bug en el filtro de tenant, la política RLS de PostgreSQL bloquea la fuga.
- Costo de infraestructura mínimo en etapa temprana: una sola instancia RDS Multi-AZ para todos los tenants.
- El modelo de dominio es agnóstico al mecanismo de aislamiento: migrar a schema separado no requiere cambiar `bo.edusync.domain`.

#### 4.2 Negativas / costos

- La política RLS debe aplicarse explícitamente a cada tabla nueva: un olvido en una tabla puede crear una brecha silenciosa de datos cross-tenant.
- Requiere que el `tenant_id` llegue correctamente al contexto de cada hilo de ejecución (incluyendo threads asíncronos de consolidación).
- Las pruebas de aislamiento multitenant (`MultitenantTest`) deben ejecutarse en CI para cada merge a `release/*`.
- Debugging más complejo: las consultas SQL deben incluir `SET app.current_tenant = '...'` para reproducir el comportamiento de producción.

#### 4.3 Neutras / observables

- El `tenant_id` aparece como columna en todas las tablas sensibles y como variable de sesión en cada conexión PostgreSQL.
- Los logs de CloudWatch incluirán el `tenant_id` en cada entrada estructurada (sin exponer datos del tenant, solo el identificador UUID).
- El rendimiento de RLS en PostgreSQL 15 sobre índices por `tenant_id` es comparable al de un discriminador en la cláusula WHERE de la aplicación.

### 5. Impacto en el sistema

- **Código**: `bo.edusync.infrastructure.adapter.out.persistence.config.TenantContextProvider` (inyección de `tenant_id` desde JWT al contexto de sesión PostgreSQL). Afecta todos los repositorios JPA en `bo.edusync.infrastructure.adapter.out.persistence.repository.*`.
- **Operaciones**: cada nueva tabla creada en Flyway MUST incluir la política RLS `CREATE POLICY tenant_isolation ON <tabla> USING (tenant_id = current_setting('app.current_tenant')::uuid)`.
- **Seguridad**: el `tenant_id` nunca se toma del body del request HTTP; siempre del JWT validado por `JwtAuthFilter` (NFR-003 + NFR-008). Afecta UC-01, UC-04, UC-06, UC-09, UC-10.
- **Equipo**: todo desarrollador nuevo debe entender el ciclo JWT → `TenantContextProvider` → variable de sesión PostgreSQL antes de escribir una query.
- **Costo**: sin impacto adicional sobre la factura AWS base; se opera sobre la misma instancia RDS Multi-AZ.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si la política RLS de PostgreSQL produce degradación de rendimiento > 20 % en p95 medida en k6, o si las migraciones Flyway se vuelven inmanejables por cantidad de tenants (> 200 tenants activos).
- **Costo estimado de revertir**: 3–4 semanas de ingeniería para migrar a schema separado por tenant + scripts de migración de datos + cambio en el routing de conexión JDBC.
- **Plan B**: migrar a la Alternativa A (schema separado) usando el mecanismo de Flyway multi-schema; el modelo de dominio `bo.edusync.domain` no requiere cambios porque es agnóstico a la estrategia de aislamiento.

### 7. Validación

- **Golden test `MultitenantTest`**: verifica que una consulta ejecutada con el `tenant_id` del Colegio A no devuelve registros del Colegio B, incluso si ambos tienen datos idénticos en las mismas tablas. Ejecuta en CI en cada merge a `release/*`.
- **Métrica**: cero filas cross-tenant en el reporte de auditoría mensual generado por el `qa-agent` ejecutando `SELECT * FROM calificacion WHERE tenant_id != current_setting('app.current_tenant')::uuid` (debe devolver 0 filas).
- **Prueba de política RLS**: `RLSPolicyTest` verifica que cada tabla del schema tiene una política `tenant_isolation` activa antes del merge.

### 8. Referencias

- `FSD-UC-01` (Registro de calificaciones — `tenant_id` como restricción implícita de toda operación de escritura).
- `FSD-UC-04` (Exportación SIE — filtro por tenant antes de construir el payload).
- `FSD-UC-09` (Administración de periodos — apertura de periodo acotada al tenant del Director).
- `FSD-UC-10` (Reportería — toda consulta de indicadores acotada al tenant activo).
- `BR-001` (RBAC: el docente solo opera en materias de su tenant y su asignación).
- `NFR-003` (Seguridad OWASP ASVS L2 — sin datos cross-tenant en ningún endpoint).
- `NFR-007` (Cifrado PII — `tenant_id` como partición de acceso a datos cifrados con KMS).
- `DA-01` en `docs/arquitectura_funcional_EduSync.md`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de DA-01 en arquitectura_funcional_EduSync.md; estado Aceptada |
