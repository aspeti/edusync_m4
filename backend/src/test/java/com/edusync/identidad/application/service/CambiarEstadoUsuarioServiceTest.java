package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CambiarEstadoUsuarioServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private CambiarEstadoUsuarioService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    service = new CambiarEstadoUsuarioService(usuarioRepositoryPort);
  }

  @Test
  void desactivaUnUsuarioDelMismoTenant() {
    UUID tenantId = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario =
        Usuario.crear(usuarioId, tenantId, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
    when(usuarioRepositoryPort.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    Usuario actualizado = service.cambiarEstado(usuarioId, tenantId, false);

    assertThat(actualizado.isActivo()).isFalse();
  }

  @Test
  void rechazaUsuarioDeOtroTenantConNotFound() {
    UUID tenantIdDelUsuario = UUID.randomUUID();
    UUID tenantIdActor = UUID.randomUUID();
    UsuarioId usuarioId = UsuarioId.nueva();
    Usuario usuario = Usuario.crear(
        usuarioId, tenantIdDelUsuario, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

    assertThatThrownBy(() -> service.cambiarEstado(usuarioId, tenantIdActor, false))
        .isInstanceOf(UsuarioNoEncontradoException.class);
  }
}
