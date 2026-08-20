package com.edusync.identidad.application.port.in;

import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;

/**
 * Puerto de entrada: listado paginado y filtrable de usuarios de un tenant (DD-UC-005,
 * filtros/paginacion DD-UC-007).
 */
public interface ListarUsuariosUseCase {

  PageResult<Usuario> listar(UUID tenantId, UsuarioFiltro filtro, PageQuery pageQuery);
}
