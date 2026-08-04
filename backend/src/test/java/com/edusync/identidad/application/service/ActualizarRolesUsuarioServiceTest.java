package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.InvarianteRolException;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ActualizarRolesUsuarioServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private ActualizarRolesUsuarioService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    service = new ActualizarRolesUsuarioService(usuarioRepositoryPort);
  }

  @Test
  void actualizaLosRolesDeUnUsuarioDelMismoTenant() {
    UUID tenantId = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario =
        Usuario.crear(usuarioId, tenantId, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
    when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    Usuario actualizado = service.actualizarRoles(usuarioId, tenantId, Set.of("ADMIN", "SECRETARIA"));

    assertThat(actualizado.getRoles()).containsExactlyInAnyOrder(Rol.ADMIN, Rol.SECRETARIA);
    verify(usuarioRepositoryPort).guardar(any(Usuario.class));
  }

  @Test
  void rechazaUsuarioDeOtroTenantConNotFound() {
    UUID tenantIdDelUsuario = UUID.randomUUID();
    UUID tenantIdActor = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario = Usuario.crear(
        usuarioId, tenantIdDelUsuario, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> service.actualizarRoles(usuarioId, tenantIdActor, Set.of("ADMIN")))
        .isInstanceOf(UsuarioNoEncontradoException.class);
  }

  @Test
  void rechazaUsuarioInexistenteConNotFound() {
    UsuarioId usuarioId = UsuarioId.nueva();
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.actualizarRoles(usuarioId, UUID.randomUUID(), Set.of("ADMIN")))
        .isInstanceOf(UsuarioNoEncontradoException.class);
  }

  @Test
  void propagaInvarianteRolVioladaSiSeIntentaAsignarSysAdmin() {
    UUID tenantId = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario =
        Usuario.crear(usuarioId, tenantId, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> service.actualizarRoles(usuarioId, tenantId, Set.of("SYSADMIN")))
        .isInstanceOf(InvarianteRolException.class);
  }
}
