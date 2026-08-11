package com.edusync.identidad.infrastructure.adapter.out.port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.ai.domain.UsuarioResumen;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BuscarUsuarioPorNombrePortImplTest {

  private UsuarioRepositoryPort usuarioRepositoryPort;
  private BuscarUsuarioPorNombrePortImpl port;
  private UUID tenantId;

  @BeforeEach
  void setUp() {
    usuarioRepositoryPort = mock(UsuarioRepositoryPort.class);
    port = new BuscarUsuarioPorNombrePortImpl(usuarioRepositoryPort);
    tenantId = UUID.randomUUID();
  }

  @Test
  void encuentraCoincidenciaParcialSinDistinguirMayusculas() {
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), tenantId, "Roberto Fernandez", "roberto@colegio.edu.bo", "hash",
        Set.of(Rol.PROFESOR), true);
    when(usuarioRepositoryPort.listarPorTenant(tenantId)).thenReturn(List.of(usuario));

    List<UsuarioResumen> resultado = port.buscarPorNombre(tenantId, "roberto");

    assertThat(resultado).hasSize(1);
    UsuarioResumen resumen = resultado.get(0);
    assertThat(resumen.nombreCompleto()).isEqualTo("Roberto Fernandez");
    assertThat(resumen.email()).isEqualTo("roberto@colegio.edu.bo");
    assertThat(resumen.roles()).containsExactly("PROFESOR");
    assertThat(resumen.activo()).isTrue();
  }

  @Test
  void noDevuelveUsuariosCuyoNombreNoContieneElTermino() {
    Usuario usuario = Usuario.crear(
        UsuarioId.nueva(), tenantId, "Maria Rojas", "maria@colegio.edu.bo", "hash",
        Set.of(Rol.SECRETARIA), true);
    when(usuarioRepositoryPort.listarPorTenant(tenantId)).thenReturn(List.of(usuario));

    List<UsuarioResumen> resultado = port.buscarPorNombre(tenantId, "Roberto");

    assertThat(resultado).isEmpty();
  }
}
