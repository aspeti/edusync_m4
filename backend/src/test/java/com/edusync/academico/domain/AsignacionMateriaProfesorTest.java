package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AsignacionMateriaProfesorTest {

  @Test
  void creaUnaAsignacionConProfesor() {
    UUID profesorId = UUID.randomUUID();

    AsignacionMateriaProfesor asignacion =
        AsignacionMateriaProfesor.crear(
            AsignacionMateriaProfesorId.nueva(),
            UUID.randomUUID(),
            MateriaId.nueva(),
            profesorId,
            CursoId.nueva(),
            ParaleloId.nueva());

    assertThat(asignacion.getProfesorId()).isEqualTo(profesorId);
  }
}
