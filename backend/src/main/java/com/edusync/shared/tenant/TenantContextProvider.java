package com.edusync.shared.tenant;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Implementacion real de la propagacion de tenant (cierra el placeholder de
 * {@code ADR-0001} dejado por {@code DD-UC-001}). {@code activar(tenantId)} lo invoca
 * {@code JwtAuthenticationFilter} tras validar el JWT; {@code limpiar()} se ejecuta al
 * final de cada request (el hilo se reutiliza entre requests en el pool de Tomcat, por lo
 * que NO limpiar dejaria fugar el tenant de una request a la siguiente).
 *
 * <p>El {@code tenant_id} efectivo llega hasta PostgreSQL a traves de
 * {@link TenantAwareDataSource}, que lee {@link TenantContext} en cada
 * {@code getConnection()} y ejecuta {@code set_config('app.current_tenant', ...)} (RLS,
 * {@code ADR-0001}).
 */
@Component
public class TenantContextProvider {

  public void activar(UUID tenantId) {
    TenantContext.setCurrentTenantId(tenantId);
  }

  public void limpiar() {
    TenantContext.clear();
  }

  public Optional<UUID> tenantActual() {
    return TenantContext.getCurrentTenantId();
  }
}
