package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InscripcionTest {

  @Test
  void naceSiempreActiva() {
    Inscripcion inscripcion =
        Inscripcion.crear(
            InscripcionId.nueva(),
            UUID.randomUUID(),
            EstudianteId.nueva(),
            GestionEscolarId.nueva(),
            CursoId.nueva(),
            ParaleloId.nueva(),
            LocalDate.of(2026, 2, 1));

    assertThat(inscripcion.getEstado()).isEqualTo(EstadoInscripcion.ACTIVA);
  }

  @Test
  void rechazaFechaNula() {
    assertThatThrownBy(
            () ->
                Inscripcion.crear(
                    InscripcionId.nueva(),
                    UUID.randomUUID(),
                    EstudianteId.nueva(),
                    GestionEscolarId.nueva(),
                    CursoId.nueva(),
                    ParaleloId.nueva(),
                    null))
        .isInstanceOf(NullPointerException.class);
  }
}
