package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/**
 * Puerto de entrada: listado paginado y filtrable de Gestiones Escolares de un tenant
 * (DD-UC-008, filtros/paginacion DD-UC-007).
 */
public interface ListarGestionesEscolaresUseCase {

  PageResult<GestionEscolar> listar(UUID tenantId, GestionEscolarFiltro filtro, PageQuery pageQuery);
}
