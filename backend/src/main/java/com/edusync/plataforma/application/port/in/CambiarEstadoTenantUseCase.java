package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;

/** Puerto de entrada: cambio manual de estado de un {@link Tenant} ({@code FSD-UC-011}, paso 4). */
public interface CambiarEstadoTenantUseCase {

  /**
   * @throws com.edusync.plataforma.domain.TenantNoEncontradoException si {@code id} no
   *     corresponde a ningun Tenant
   */
  Tenant cambiarEstado(TenantId id, EstadoTenant nuevoEstado);
}
