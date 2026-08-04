package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando un {@link PasswordResetToken} ya fue usado o esta expirado
 * ({@code FSD-UC-021}, flujo alternativo A2). Mapea a HTTP 410 en la capa REST
 * ({@code PasswordResetController}).
 */
public class TokenResetInvalidoException extends DomainException {

  public TokenResetInvalidoException() {
    super("E_ENLACE_INVALIDO", "El enlace de restablecimiento de contrasena ya fue usado o expiro");
  }
}
