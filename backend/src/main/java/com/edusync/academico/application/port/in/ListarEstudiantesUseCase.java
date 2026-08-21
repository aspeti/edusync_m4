package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Estudiante;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/** Puerto de entrada: listado paginado y filtrable de Estudiantes de un tenant ({@code DD-UC-013}). */
public interface ListarEstudiantesUseCase {

  PageResult<Estudiante> listar(UUID tenantId, EstudianteFiltro filtro, PageQuery pageQuery);
}
