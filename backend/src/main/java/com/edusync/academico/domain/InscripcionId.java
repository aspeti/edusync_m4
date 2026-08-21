package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link Inscripcion}. Interno al modulo. */
public record InscripcionId(UUID valor) {

  public InscripcionId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static InscripcionId nueva() {
    return new InscripcionId(UUID.randomUUID());
  }

  public static InscripcionId de(UUID valor) {
    return new InscripcionId(valor);
  }
}
