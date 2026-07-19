package com.edusync.identidad.application.port.in;

/**
 * Puerto de entrada: login. Implementado por {@code AutenticarUsuarioService}.
 */
public interface AutenticarUsuarioUseCase {

  /**
   * @throws com.edusync.identidad.domain.CredencialesInvalidasException si el email no
   *     existe, la contrasena no coincide o el usuario esta inactivo
   */
  TokenAcceso autenticar(String email, String password);
}
