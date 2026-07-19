/**
 * Contexto de tenant para el aislamiento multitenant por RLS (ADR-0001).
 *
 * <p>{@link com.edusync.shared.tenant.TenantContext} (holder {@code ThreadLocal}),
 * {@link com.edusync.shared.tenant.TenantContextProvider} (API usada por
 * {@code identidad.infrastructure.security.JwtAuthenticationFilter}) y
 * {@link com.edusync.shared.tenant.TenantAwareDataSource} (fija
 * {@code app.current_tenant} en cada conexion JDBC via {@code set_config}) forman la
 * implementacion real, poblada por {@code DD-UC-002} (docs/design/DD-UC-002.md).
 */
package com.edusync.shared.tenant;
