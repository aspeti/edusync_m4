package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link Curso}. Interno al modulo (sin uso publico documentado hoy). */
public record CursoId(UUID valor) {

  public CursoId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static CursoId nueva() {
    return new CursoId(UUID.randomUUID());
  }

  public static CursoId de(UUID valor) {
    return new CursoId(valor);
  }
}
