package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link AsignacionMateriaCurso}. Interno al modulo. */
public record AsignacionMateriaCursoId(UUID valor) {

  public AsignacionMateriaCursoId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static AsignacionMateriaCursoId nueva() {
    return new AsignacionMateriaCursoId(UUID.randomUUID());
  }

  public static AsignacionMateriaCursoId de(UUID valor) {
    return new AsignacionMateriaCursoId(valor);
  }
}
