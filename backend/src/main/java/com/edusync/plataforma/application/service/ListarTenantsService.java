package com.edusync.plataforma.application.service;

import com.edusync.plataforma.application.port.in.ListarTenantsUseCase;
import com.edusync.plataforma.application.port.in.TenantFiltro;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicacion que implementa {@link ListarTenantsUseCase}.
 * Delega directamente al repositorio; sin lógica de negocio adicional
 * ({@code DD-UC-004} §2, filtros/paginacion {@code DD-UC-007}).
 */
@Service
@RequiredArgsConstructor
class ListarTenantsService implements ListarTenantsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<Tenant> listar(TenantFiltro filtro, PageQuery pageQuery) {
    return tenantRepositoryPort.listarTodos(filtro, pageQuery);
  }
}
