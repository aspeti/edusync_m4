package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link Estudiante}. Interno al modulo. */
public record EstudianteId(UUID valor) {

  public EstudianteId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static EstudianteId nueva() {
    return new EstudianteId(UUID.randomUUID());
  }

  public static EstudianteId de(UUID valor) {
    return new EstudianteId(valor);
  }
}
