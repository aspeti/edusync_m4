package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link GestionEscolar}. Interno al modulo (sin uso publico documentado hoy). */
public record GestionEscolarId(UUID valor) {

  public GestionEscolarId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static GestionEscolarId nueva() {
    return new GestionEscolarId(UUID.randomUUID());
  }

  public static GestionEscolarId de(UUID valor) {
    return new GestionEscolarId(valor);
  }
}
