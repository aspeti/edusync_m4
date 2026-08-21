package com.edusync.academico.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListarCursosServiceTest {

  private CursoRepositoryPort cursoRepositoryPort;
  private ListarCursosService service;

  @BeforeEach
  void setUp() {
    cursoRepositoryPort = mock(CursoRepositoryPort.class);
    service = new ListarCursosService(cursoRepositoryPort);
  }

  @Test
  void delegaEnElRepositorioFiltrandoPorTenant() {
    UUID tenantId = UUID.randomUUID();
    Curso curso = Curso.crear(CursoId.nueva(), tenantId, "Primero de Primaria");
    PageQuery pageQuery = PageQuery.of(null, null);
    CursoFiltro filtro = CursoFiltro.VACIO;
    when(cursoRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery))
        .thenReturn(PageResult.of(List.of(curso), pageQuery, 1));

    PageResult<Curso> resultado = service.listar(tenantId, filtro, pageQuery);

    assertThat(resultado.content()).containsExactly(curso);
    assertThat(resultado.totalElements()).isEqualTo(1);
  }

  @Test
  void delegaFiltroYPaginacionSinModificarlos() {
    UUID tenantId = UUID.randomUUID();
    CursoFiltro filtro = new CursoFiltro("primero");
    PageQuery pageQuery = new PageQuery(1, 5);
    when(cursoRepositoryPort.listarPorTenant(any(), any(), any())).thenReturn(PageResult.of(List.of(), pageQuery, 0));

    service.listar(tenantId, filtro, pageQuery);

    verify(cursoRepositoryPort).listarPorTenant(tenantId, filtro, pageQuery);
  }
}
