package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AsignacionMateriaCursoTest {

  @Test
  void creaUnaAsignacionConCursoYParalelo() {
    CursoId cursoId = CursoId.nueva();
    ParaleloId paraleloId = ParaleloId.nueva();

    AsignacionMateriaCurso asignacion =
        AsignacionMateriaCurso.crear(
            AsignacionMateriaCursoId.nueva(),
            UUID.randomUUID(),
            MateriaId.nueva(),
            cursoId,
            paraleloId);

    assertThat(asignacion.getCursoId()).isEqualTo(cursoId);
    assertThat(asignacion.getParaleloId()).isEqualTo(paraleloId);
  }
}
