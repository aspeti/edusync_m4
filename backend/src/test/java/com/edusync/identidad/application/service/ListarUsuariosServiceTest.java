package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarUsuariosServiceTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private ListarUsuariosService service;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    service = new ListarUsuariosService(usuarioRepositoryPort);
  }

  @Test
  void delegaEnElRepositorioFiltrandoPorTenant() {
    UUID tenantId = UUID.randomUUID();
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), tenantId, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.listarPorTenant(tenantId)).thenReturn(List.of(usuario));

    List<Usuario> usuarios = service.listar(tenantId);

    assertThat(usuarios).containsExactly(usuario);
  }
}
