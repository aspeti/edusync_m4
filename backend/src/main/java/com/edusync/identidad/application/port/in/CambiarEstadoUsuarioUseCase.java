package com.edusync.identidad.application.port.in;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.domain.Usuario;
import java.util.UUID;

/** Puerto de entrada: activacion/desactivacion de un usuario (DD-UC-005). */
public interface CambiarEstadoUsuarioUseCase {

  /**
   * @throws com.edusync.identidad.domain.UsuarioNoEncontradoException si el usuario no
   *     existe o pertenece a un tenant distinto de {@code tenantIdActor}
   */
  Usuario cambiarEstado(UsuarioId usuarioId, UUID tenantIdActor, boolean activo);
}
