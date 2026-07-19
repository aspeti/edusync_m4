package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.Tenant;

/** Puerto de entrada: alta de un {@link Tenant} ({@code FSD-UC-011}, paso 1). */
public interface RegistrarTenantUseCase {

  /**
   * @throws com.edusync.plataforma.domain.SuscripcionIncompletaException si
   *     {@code fechaVencimientoSuscripcion} es nula
   */
  Tenant registrar(RegistrarTenantCommand command);
}
