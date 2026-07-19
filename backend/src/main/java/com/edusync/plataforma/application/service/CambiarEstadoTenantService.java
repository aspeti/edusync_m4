package com.edusync.plataforma.application.service;

import com.edusync.plataforma.application.port.in.CambiarEstadoTenantUseCase;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.plataforma.domain.TenantNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el cambio manual de estado de un Tenant ({@code FSD-UC-011}, paso 4). */
@Service
@RequiredArgsConstructor
public class CambiarEstadoTenantService implements CambiarEstadoTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  @Override
  @Transactional
  public Tenant cambiarEstado(TenantId id, EstadoTenant nuevoEstado) {
    Tenant tenant = tenantRepositoryPort.buscarPorId(id)
        .orElseThrow(() -> new TenantNoEncontradoException(id.valor()));
    tenant.cambiarEstado(nuevoEstado);
    return tenantRepositoryPort.guardar(tenant);
  }
}
