package com.edusync.identidad.application.port.in;

import com.edusync.identidad.UsuarioId;
import java.util.UUID;

/** Puerto de entrada: inicio del flujo de restablecimiento de contrasena (DD-UC-005). */
public interface IniciarRestablecimientoPasswordUseCase {

  /**
   * @throws com.edusync.identidad.domain.UsuarioNoEncontradoException si el usuario no
   *     existe o pertenece a un tenant distinto de {@code tenantIdActor}
   */
  void iniciar(UsuarioId usuarioId, UUID tenantIdActor);
}
