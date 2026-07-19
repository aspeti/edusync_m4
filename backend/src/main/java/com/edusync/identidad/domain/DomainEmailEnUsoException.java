package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/** Se lanza al intentar crear un usuario con un email ya registrado (email es unico global). */
public class DomainEmailEnUsoException extends DomainException {

  public DomainEmailEnUsoException(String email) {
    super("E_EMAIL_EN_USO", "El email ya esta registrado: " + email);
  }
}
