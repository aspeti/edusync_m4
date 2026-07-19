/**
 * Dominio puro del modulo {@code identidad} (sin dependencias de Spring/JPA).
 *
 * <p>{@link com.edusync.identidad.domain.Usuario} es el Aggregate Root; valida en su
 * factory la invariante permanente {@code tenantId == null <=> roles == {SYSADMIN}}
 * (ADR-0010). Poblado por {@code DD-UC-002} (docs/design/DD-UC-002.md).
 */
package com.edusync.identidad.domain;
