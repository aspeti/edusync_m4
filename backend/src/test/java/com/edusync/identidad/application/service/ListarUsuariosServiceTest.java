package com.edusync.identidad.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.UsuarioFiltro;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
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
    PageQuery pageQuery = PageQuery.of(null, null);
    UsuarioFiltro filtro = UsuarioFiltro.VACIO;
    when(usuarioRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery))
        .thenReturn(PageResult.of(List.of(usuario), pageQuery, 1));

    PageResult<Usuario> resultado = service.listar(tenantId, filtro, pageQuery);

    assertThat(resultado.content()).containsExactly(usuario);
    assertThat(resultado.totalElements()).isEqualTo(1);
  }

  @Test
  void delegaFiltroYPaginacionSinModificarlos() {
    UUID tenantId = UUID.randomUUID();
    UsuarioFiltro filtro = new UsuarioFiltro("roberto", true, Rol.PROFESOR);
    PageQuery pageQuery = new PageQuery(1, 5);
    when(usuarioRepositoryPort.listarPorTenant(any(), any(), any()))
        .thenReturn(PageResult.of(List.of(), pageQuery, 0));

    service.listar(tenantId, filtro, pageQuery);

    org.mockito.Mockito.verify(usuarioRepositoryPort).listarPorTenant(tenantId, filtro, pageQuery);
  }
}
