package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el login falla por email inexistente, contrasena incorrecta o usuario
 * inactivo. Mensaje deliberadamente generico (no revela cual de las tres condiciones
 * fallo) para no filtrar informacion util a un atacante.
 */
public class CredencialesInvalidasException extends DomainException {

  public CredencialesInvalidasException() {
    super("E_CREDENCIALES_INVALIDAS", "Email o contrasena incorrectos");
  }
}
