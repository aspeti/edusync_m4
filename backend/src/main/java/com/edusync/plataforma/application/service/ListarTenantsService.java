package com.edusync.plataforma.application.service;

import com.edusync.plataforma.application.port.in.ListarTenantsUseCase;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.Tenant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion que implementa {@link ListarTenantsUseCase}.
 * Delega directamente al repositorio; sin lógica de negocio adicional
 * ({@code DD-UC-004} §2).
 */
@Service
@RequiredArgsConstructor
class ListarTenantsService implements ListarTenantsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Tenant> listar() {
    return tenantRepositoryPort.listarTodos();
  }
}
