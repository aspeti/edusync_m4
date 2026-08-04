package com.edusync.identidad.application.port.in;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.domain.Usuario;
import java.util.Set;
import java.util.UUID;

/** Puerto de entrada: modificacion del conjunto de roles vigentes de un usuario (DD-UC-005). */
public interface ActualizarRolesUsuarioUseCase {

  /**
   * @throws com.edusync.identidad.domain.InvarianteRolException si {@code roles} viola la
   *     invariante permanente (ADR-0010)
   * @throws com.edusync.identidad.domain.UsuarioNoEncontradoException si el usuario no
   *     existe o pertenece a un tenant distinto de {@code tenantIdActor}
   */
  Usuario actualizarRoles(UsuarioId usuarioId, UUID tenantIdActor, Set<String> roles);
}
