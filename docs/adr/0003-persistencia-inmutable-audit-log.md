# Architecture Decision Record (ADR)

## ADR-0003: Persistencia inmutable con audit_log explícito y modelo append-only

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0003` |
| Título | Persistencia inmutable con audit_log explícito y modelo append-only |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Capa de persistencia — todas las operaciones de escritura del sistema |
| Stakeholders consultados | Directores de unidades educativas, Equipo de arquitectura G-EduSync |

### 1. Contexto

La normativa educativa boliviana y el contexto legal de unidades educativas exige trazabilidad completa de cualquier cambio en calificaciones oficiales: quién realizó el cambio, cuándo, cuál era el valor anterior y cuál es el valor nuevo. El análisis del BRD (BR-005) y los requisitos del SIE confirman que sobrescribir registros de calificaciones es inaceptable: en caso de impugnación o auditoría ministerial, el historial completo debe ser recuperable.

Adicionalmente, la modificación retroactiva de calificaciones ya cerradas (UC-05) —que requiere autorización explícita del Director— debe dejar una traza inalterable que incluya: la solicitud del docente, la resolución del Director con alcance y ventana temporal, y el nuevo registro de calificación con referencia al original.

Las fuerzas en tensión son: **trazabilidad completa** vs. **complejidad de consulta del valor vigente** vs. **madurez de la solución con el stack Spring Boot 3.3 / Hibernate 6**.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. Tabla `audit_log` separada con escritura desde Spring AOP (`@AuditLogAspect`) | Alta trazabilidad; tabla principal siempre refleja el valor actual; fácil de consultar el "estado vigente"; AOP desacopla la lógica de negocio de la auditoría | El aspecto AOP puede silenciarse si el método no pasa por el proxy Spring; require disciplina para no saltarse el aspecto | Bajo — solo configuración AOP + tabla adicional |
| B. Hibernate Envers (revisiones automáticas `_AUD` tables) | Automático; cero código extra por entidad anotada con `@Audited`; revisiones numeradas | Consulta del historial requiere API específica de Envers; tablas `_AUD` no son intuitivas para reportes; dependencia de Envers a largo plazo | Bajo — dependencia Maven adicional |
| C. Modelo append-only nativo (`estado` + `registro_padre_id` en la misma tabla) | Inmutabilidad garantizada a nivel de esquema; cualquier consulta SQL puede reconstruir el historial; alineado con el patrón Event Sourcing a futuro | La consulta del "valor vigente" requiere filtrar por `estado = VIGENTE`; mayor volumen de filas en la tabla `calificacion` | Medio — DDL más complejo; índices adicionales |

### 3. Decisión

> **Elegimos una estrategia combinada de las Alternativas A y C: tabla `audit_log` explícita con escritura desde Spring AOP para todas las operaciones de escritura del sistema, complementada con el modelo append-only específicamente en el flujo de modificación retroactiva aprobada (UC-05).**

La tabla `audit_log` centralizada con campos `usuario_id`, `accion`, `entidad_afectada`, `valor_anterior`, `valor_nuevo` y `timestamp_utc` responde directamente a BR-005 sin acoplar la inmutabilidad al framework ORM. Hibernate Envers se añade para las entidades críticas de calificación como segunda capa de auditoría.

El modelo append-only en UC-05 garantiza que el registro de calificación original nunca se sobreescribe: toda corrección autorizada genera un nuevo registro con `registro_padre_id` apuntando al original, y el original queda en estado `REEMPLAZADO` (nunca eliminado ni modificado).

### 4. Consecuencias

#### 4.1 Positivas

- Trazabilidad completa e inalterable: toda operación de escritura queda registrada en `audit_log` dentro de la misma transacción de BD (garantizado por `@Transactional`).
- El modelo append-only de UC-05 cumple el requisito legal boliviano de no sobrescribir registros académicos oficiales.
- El historial de calificaciones de cualquier estudiante es reconstructible en cualquier punto en el tiempo usando `registro_padre_id`.
- La regla `RULE` de PostgreSQL sobre `audit_log` previene `UPDATE` y `DELETE` a nivel de motor de BD, incluso ante bugs de aplicación.

#### 4.2 Negativas / costos

- `AuditLogAspect` debe cubrir todos los métodos de aplicación que producen escrituras; un método que no esté anotado correctamente puede silenciar la auditoría.
- La tabla `audit_log` crece con el tiempo; requiere política de retención y particionado por `timestamp_utc` en producción.
- Las consultas del "valor vigente" en la tabla `calificacion` requieren `WHERE estado = 'VIGENTE'`; sin el índice correcto pueden degradar.
- Los tests de integración que usan `audit_log` necesitan Testcontainers PostgreSQL (no H2 en memoria, que no soporta `RULE`).

#### 4.3 Neutras / observables

- Hibernate Envers genera tablas `calificacion_AUD` y `centralizador_AUD` que conviven con `audit_log`; ambas son fuentes de auditoría complementarias.
- El campo `registro_padre_id` en `calificacion` es nullable (null = registro original); solo los registros de corrección retroactiva tienen valor.
- La anotación `@Immutable` de Hibernate en la entidad `AuditLogEntry` previene que el ORM emita `UPDATE` sobre esa entidad por accidente.

### 5. Impacto en el sistema

- **Código**: `bo.edusync.infrastructure.adapter.in.web.aspect.AuditLogAspect` (Spring AOP, corte transversal sobre métodos anotados con `@AuditLog`). Entidad `AuditLogEntry` en `bo.edusync.domain.model.auditoria` con `@Immutable`. Puerto de salida `AuditLogRepository` en `bo.edusync.domain.port.out`. Afecta UC-01, UC-02, UC-04, UC-05, UC-06.
- **Operaciones**: migración Flyway V003 crea tablas `audit_log` y `calificacion` (con `registro_padre_id`). La regla PostgreSQL `CREATE RULE no_update_audit_log AS ON UPDATE TO audit_log DO INSTEAD NOTHING` bloquea `UPDATE` a nivel de motor.
- **Seguridad**: el `audit_log` registra `usuario_id` (UUID del JWT) y `tenant_id`; nunca nombre ni datos PII del operador más allá del UUID (NFR-003).
- **Equipo**: toda operación de escritura nueva MUST anotarse con `@AuditLog` antes del merge; el `qa-agent` verifica cobertura de auditoría en CI.
- **Costo**: sin impacto adicional en factura AWS; volumen estimado de `audit_log` < 10 MB/mes por tenant en un colegio de 500 estudiantes.

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si `AuditLogAspect` produce problemas de rendimiento > 10 % en p95 de UC-01, o si la tabla `audit_log` supera 100 GB en producción sin particionado.
- **Costo estimado de revertir**: 1 semana para desactivar `AuditLogAspect` y migrar la auditoría a triggers PostgreSQL nativos; el modelo append-only de UC-05 es independiente y no requiere cambio.
- **Plan B**: reemplazar `AuditLogAspect` por triggers `AFTER INSERT/UPDATE` en PostgreSQL, que son igualmente confiables y sin overhead en el hilo de aplicación.

### 7. Validación

- **Golden test `AuditLogTest`**: `AuditLogTest.toda_escritura_genera_entrada_audit_log()` — verifica que después de cada operación de escritura en UC-01, UC-02, UC-04, UC-05, UC-06 existe exactamente una entrada en `audit_log` con el `usuario_id`, `accion`, `entidad_afectada` y `timestamp_utc` correctos. Bloquea merge a `release/*` si falla.
- **Golden test `VentanaTest`**: `VentanaTest.correccion_retroactiva_genera_nuevo_registro_con_padre_id()` — verifica que al aplicar una corrección autorizada en UC-05, el registro original permanece con `estado = REEMPLAZADO` y el nuevo tiene `registro_padre_id` apuntando al original.
- **Regla PostgreSQL**: `AuditLogRuleTest` verifica en CI que la regla `no_update_audit_log` está activa y que un intento directo de `UPDATE audit_log SET accion = 'X'` no modifica ninguna fila.

### 8. Referencias

- `FSD-UC-01` (Registro de calificaciones — toda operación genera entrada en `audit_log`).
- `FSD-UC-02` (Cierre de materia — el cierre genera entrada de auditoría con actor y timestamp).
- `FSD-UC-04` (Exportación SIE — la operación completa de exportación genera entrada de auditoría con resumen).
- `FSD-UC-05` (Modificación retroactiva — triple entrada en `audit_log`: solicitud, resolución y cierre de ventana).
- `FSD-UC-06` (Nóminas — alta/baja/transferencia de estudiante genera entrada de auditoría).
- `BR-005` (Toda modificación retroactiva aprobada genera un nuevo registro versionado; el original es inmutable).
- `BR-010` (El `audit_log` se escribe en la misma transacción que la operación de negocio).
- `NFR-006` (Inmutabilidad: `audit_log` con cero `UPDATE/DELETE` sobre ninguna fila).
- `DA-03` en `docs/arquitectura_funcional_EduSync.md`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de DA-03 en arquitectura_funcional_EduSync.md; estado Aceptada |
