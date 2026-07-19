package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.plataforma.application.port.in.RegistrarTenantCommand;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistrarTenantServiceTest {

  private TenantRepositoryPort tenantRepositoryPort;
  private RegistrarTenantService service;

  @BeforeEach
  void setUp() {
    tenantRepositoryPort = mock(TenantRepositoryPort.class);
    service = new RegistrarTenantService(tenantRepositoryPort);
  }

  @Test
  void registraUnTenantActivo() {
    when(tenantRepositoryPort.guardar(any(Tenant.class))).thenAnswer(inv -> inv.getArgument(0));

    Tenant tenant = service.registrar(
        new RegistrarTenantCommand("Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

    assertThat(tenant.getNombre()).isEqualTo("Colegio Ejemplo");
    assertThat(tenant.getEstado()).isEqualTo(EstadoTenant.ACTIVO);
  }
}
