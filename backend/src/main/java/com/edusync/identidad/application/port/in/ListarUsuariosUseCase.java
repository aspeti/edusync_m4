package com.edusync.identidad.application.port.in;

import com.edusync.identidad.domain.Usuario;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: listado de usuarios de un tenant (DD-UC-005). */
public interface ListarUsuariosUseCase {

  List<Usuario> listar(UUID tenantId);
}
