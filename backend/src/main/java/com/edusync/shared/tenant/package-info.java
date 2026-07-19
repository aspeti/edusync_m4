/**
 * Contexto de tenant para el aislamiento multitenant por RLS (ADR-0001).
 *
 * <p>{@code TenantContext} es un placeholder en este bootstrap: la implementacion real
 * (poblado desde el claim del JWT, {@code SET app.current_tenant} en la conexion JDBC)
 * llega en {@code DD-UC-002} (docs/design/DD-UC-002.md).
 */
package com.edusync.shared.tenant;
