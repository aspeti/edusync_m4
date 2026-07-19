package com.edusync.plataforma.application.service;

import com.edusync.plataforma.application.port.in.RegistrarTenantCommand;
import com.edusync.plataforma.application.port.in.RegistrarTenantUseCase;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el alta de Tenants ({@code FSD-UC-011}, paso 1). */
@Service
@RequiredArgsConstructor
public class RegistrarTenantService implements RegistrarTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  @Override
  @Transactional
  public Tenant registrar(RegistrarTenantCommand command) {
    Tenant tenant = Tenant.crear(
        TenantId.nueva(), command.nombre(), command.fechaInicioSuscripcion(), command.fechaVencimientoSuscripcion());
    return tenantRepositoryPort.guardar(tenant);
  }
}
