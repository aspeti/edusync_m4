package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link Paralelo}. Interno al modulo. */
public record ParaleloId(UUID valor) {

  public ParaleloId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static ParaleloId nueva() {
    return new ParaleloId(UUID.randomUUID());
  }

  public static ParaleloId de(UUID valor) {
    return new ParaleloId(valor);
  }
}
