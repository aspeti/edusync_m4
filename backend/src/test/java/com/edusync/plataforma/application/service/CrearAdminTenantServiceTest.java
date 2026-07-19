package com.edusync.plataforma.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioCreacionPort;
import com.edusync.identidad.UsuarioId;
import com.edusync.plataforma.application.port.in.CrearAdminTenantCommand;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.plataforma.domain.TenantNoEncontradoException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CrearAdminTenantServiceTest {

  private TenantRepositoryPort tenantRepositoryPort;
  private UsuarioCreacionPort usuarioCreacionPort;
  private CrearAdminTenantService service;

  @BeforeEach
  void setUp() {
    tenantRepositoryPort = mock(TenantRepositoryPort.class);
    usuarioCreacionPort = mock(UsuarioCreacionPort.class);
    service = new CrearAdminTenantService(tenantRepositoryPort, usuarioCreacionPort);
  }

  @Test
  void delegaLaCreacionDelAdminAIdentidadConRolAdmin() {
    TenantId tenantId = TenantId.nueva();
    Tenant tenant = Tenant.reconstruir(
        tenantId, "Colegio Ejemplo", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), EstadoTenant.ACTIVO);
    when(tenantRepositoryPort.buscarPorId(tenantId)).thenReturn(Optional.of(tenant));
    UsuarioId usuarioId = UsuarioId.nueva();
    when(usuarioCreacionPort.crear(any(CrearUsuarioCommand.class))).thenReturn(usuarioId);

    UsuarioId resultado = service.crearAdmin(
        new CrearAdminTenantCommand(tenantId, "Admin Colegio", "admin@colegio.edu.bo", "secreto123"));

    assertThat(resultado).isEqualTo(usuarioId);
    verify(usuarioCreacionPort).crear(new CrearUsuarioCommand(
        tenantId.valor(), "Admin Colegio", "admin@colegio.edu.bo", "secreto123", Set.of("ADMIN")));
  }

  @Test
  void rechazaTenantInexistenteSinLlamarAIdentidad() {
    TenantId tenantId = TenantId.nueva();
    when(tenantRepositoryPort.buscarPorId(tenantId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.crearAdmin(
        new CrearAdminTenantCommand(tenantId, "Admin Colegio", "admin@colegio.edu.bo", "secreto123")))
        .isInstanceOf(TenantNoEncontradoException.class);
  }
}
