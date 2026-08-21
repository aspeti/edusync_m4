package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de una {@link AsignacionMateriaProfesor}. Interno al modulo. */
public record AsignacionMateriaProfesorId(UUID valor) {

  public AsignacionMateriaProfesorId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static AsignacionMateriaProfesorId nueva() {
    return new AsignacionMateriaProfesorId(UUID.randomUUID());
  }

  public static AsignacionMateriaProfesorId de(UUID valor) {
    return new AsignacionMateriaProfesorId(valor);
  }
}
