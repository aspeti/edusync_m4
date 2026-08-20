package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.plataforma.application.port.in.TenantFiltro;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests de {@link ListarTenantsService} (DD-UC-004 §6, filtros/paginacion DD-UC-007).
 */
class ListarTenantsServiceTest {

  private TenantRepositoryPort tenantRepositoryPort;
  private ListarTenantsService service;

  @BeforeEach
  void setUp() {
    tenantRepositoryPort = mock(TenantRepositoryPort.class);
    service = new ListarTenantsService(tenantRepositoryPort);
  }

  @Test
  void listarDevuelveListaCompleta() {
    Tenant tenant1 = Tenant.reconstruir(
        TenantId.nueva(), "Colegio A", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), EstadoTenant.ACTIVO);
    Tenant tenant2 = Tenant.reconstruir(
        TenantId.nueva(), "Colegio B", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 12, 31), EstadoTenant.SUSPENDIDO);
    PageQuery pageQuery = PageQuery.of(null, null);
    when(tenantRepositoryPort.listarTodos(TenantFiltro.VACIO, pageQuery))
        .thenReturn(PageResult.of(List.of(tenant1, tenant2), pageQuery, 2));

    PageResult<Tenant> result = service.listar(TenantFiltro.VACIO, pageQuery);

    assertThat(result.content()).hasSize(2);
    assertThat(result.content()).extracting(Tenant::getNombre).containsExactly("Colegio A", "Colegio B");
  }

  @Test
  void listarDevuelveListaVaciaCuandoNoHayTenants() {
    PageQuery pageQuery = PageQuery.of(null, null);
    when(tenantRepositoryPort.listarTodos(any(), any()))
        .thenReturn(PageResult.of(List.of(), pageQuery, 0));

    PageResult<Tenant> result = service.listar(TenantFiltro.VACIO, pageQuery);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isZero();
  }
}
