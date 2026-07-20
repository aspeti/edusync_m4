package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests de {@link ListarTenantsService} (DD-UC-004 §6).
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
    when(tenantRepositoryPort.listarTodos()).thenReturn(List.of(tenant1, tenant2));

    List<Tenant> result = service.listar();

    assertThat(result).hasSize(2);
    assertThat(result).extracting(Tenant::getNombre).containsExactly("Colegio A", "Colegio B");
  }

  @Test
  void listarDevuelveListaVaciaCuandoNoHayTenants() {
    when(tenantRepositoryPort.listarTodos()).thenReturn(List.of());

    List<Tenant> result = service.listar();

    assertThat(result).isEmpty();
  }
}
