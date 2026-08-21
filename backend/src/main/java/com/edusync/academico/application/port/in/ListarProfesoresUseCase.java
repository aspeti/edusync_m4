package com.edusync.academico.application.port.in;

import com.edusync.academico.ProfesorResumen;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/** Puerto de entrada: listado paginado y filtrable de profesores de un tenant ({@code DD-UC-014}). */
public interface ListarProfesoresUseCase {

  PageResult<ProfesorResumen> listar(UUID tenantId, ProfesorFiltro filtro, PageQuery pageQuery);
}
