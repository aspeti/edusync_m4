package com.edusync.shared.tenant;

import java.util.Optional;
import java.util.UUID;

/**
 * Contenedor del {@code tenant_id} de la peticion en curso (aislamiento multitenant por
 * RLS, {@code ADR-0001}).
 *
 * <p><strong>Placeholder de bootstrap ({@code DD-UC-001}):</strong> hoy solo expone un
 * {@link ThreadLocal} sin ningun productor real. La implementacion definitiva
 * ({@code TenantContextProvider}, poblado desde el claim del JWT tras el login y usado
 * para ejecutar {@code SET app.current_tenant} en la conexion JDBC activa) se construye
 * en {@code DD-UC-002} (docs/design/DD-UC-002.md &sect;2). Un {@code tenant_id} nulo es
 * la representacion del rol de plataforma {@code SYSADMIN} ({@code ADR-0010}).
 */
public final class TenantContext {

  private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

  private TenantContext() {
  }

  public static Optional<UUID> getCurrentTenantId() {
    return Optional.ofNullable(CURRENT_TENANT.get());
  }

  public static void setCurrentTenantId(UUID tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
