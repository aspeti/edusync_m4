package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Materia;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/** Puerto de entrada: listado paginado y filtrable de Materias de un tenant ({@code DD-UC-012}). */
public interface ListarMateriasUseCase {

  PageResult<Materia> listar(UUID tenantId, MateriaFiltro filtro, PageQuery pageQuery);
}
