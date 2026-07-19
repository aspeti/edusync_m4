package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.plataforma.domain.TenantNoEncontradoException;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CambiarEstadoTenantServiceTest {

  private TenantRepositoryPort tenantRepositoryPort;
  private CambiarEstadoTenantService service;

  @BeforeEach
  void setUp() {
    tenantRepositoryPort = mock(TenantRepositoryPort.class);
    service = new CambiarEstadoTenantService(tenantRepositoryPort);
  }

  @Test
  void cambiaElEstadoDeUnTenantExistente() {
    TenantId id = TenantId.nueva();
    Tenant tenant = Tenant.reconstruir(
        id, "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), EstadoTenant.ACTIVO);
    when(tenantRepositoryPort.buscarPorId(id)).thenReturn(Optional.of(tenant));
    when(tenantRepositoryPort.guardar(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

    Tenant actualizado = service.cambiarEstado(id, EstadoTenant.SUSPENDIDO);

    assertThat(actualizado.getEstado()).isEqualTo(EstadoTenant.SUSPENDIDO);
  }

  @Test
  void rechazaTenantInexistente() {
    TenantId id = TenantId.nueva();
    when(tenantRepositoryPort.buscarPorId(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.cambiarEstado(id, EstadoTenant.SUSPENDIDO))
        .isInstanceOf(TenantNoEncontradoException.class);
  }
}
