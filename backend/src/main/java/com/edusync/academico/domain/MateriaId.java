package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link Materia}. Interno al modulo. */
public record MateriaId(UUID valor) {

  public MateriaId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static MateriaId nueva() {
    return new MateriaId(UUID.randomUUID());
  }

  public static MateriaId de(UUID valor) {
    return new MateriaId(valor);
  }
}
