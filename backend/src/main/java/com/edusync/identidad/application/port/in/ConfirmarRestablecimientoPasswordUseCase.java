package com.edusync.identidad.application.port.in;

/**
 * Puerto de entrada: confirmacion del restablecimiento de contrasena (DD-UC-005). Publico
 * (sin actor autenticado): el usuario no tiene sesion en este paso.
 */
public interface ConfirmarRestablecimientoPasswordUseCase {

  /**
   * @throws com.edusync.identidad.domain.TokenResetInvalidoException si el token ya fue
   *     usado o esta expirado ({@code FSD-UC-021}, flujo alternativo A2)
   */
  void confirmar(String token, String passwordNuevo);
}
