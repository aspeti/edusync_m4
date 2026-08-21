package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EstudianteTest {

  @Test
  void creaUnEstudianteActivoPorDefectoCuandoEstadoEsNulo() {
    Estudiante estudiante =
        Estudiante.crear(EstudianteId.nueva(), UUID.randomUUID(), "12345678", "Ana Pérez", null, null);

    assertThat(estudiante.getEstado()).isEqualTo(EstadoEstudiante.ACTIVO);
    assertThat(estudiante.getDatosPersonales()).isEmpty();
  }

  @Test
  void rechazaRudeNulo() {
    assertThatThrownBy(
            () ->
                Estudiante.crear(
                    EstudianteId.nueva(), UUID.randomUUID(), null, "Ana Pérez", EstadoEstudiante.ACTIVO, Map.of()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void rechazaNombreCompletoNulo() {
    assertThatThrownBy(
            () ->
                Estudiante.crear(
                    EstudianteId.nueva(), UUID.randomUUID(), "12345678", null, EstadoEstudiante.ACTIVO, Map.of()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void datosPersonalesQuedanInmutables() {
    Estudiante estudiante =
        Estudiante.crear(
            EstudianteId.nueva(),
            UUID.randomUUID(),
            "12345678",
            "Ana Pérez",
            EstadoEstudiante.ACTIVO,
            Map.of("lugarNacimiento", "La Paz"));

    assertThat(estudiante.getDatosPersonales()).containsEntry("lugarNacimiento", "La Paz");
    assertThatThrownBy(() -> estudiante.getDatosPersonales().put("otro", "x"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
