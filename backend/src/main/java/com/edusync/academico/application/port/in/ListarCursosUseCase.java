package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Curso;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/**
 * Puerto de entrada: listado paginado y filtrable de Cursos de un tenant ({@code DD-UC-010},
 * filtros/paginacion {@code DD-UC-007}).
 */
public interface ListarCursosUseCase {

  PageResult<Curso> listar(UUID tenantId, CursoFiltro filtro, PageQuery pageQuery);
}
