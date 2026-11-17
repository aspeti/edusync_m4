package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SeccionEvaluacionTest {

  @Test
  void creaSeccionConNotaYRangosValidos() {
    SeccionEvaluacion seccion = SeccionEvaluacion.crear(
        SeccionEvaluacionId.nueva(),
        UUID.randomUUID(),
        GestionEscolarId.nueva(),
        "Saber",
        2,
        new BigDecimal("45"),
        BigDecimal.ZERO,
        new BigDecimal("45"));

    assertThat(seccion.getNombre()).isEqualTo("Saber");
    assertThat(seccion.getOrden()).isEqualTo(2);
    assertThat(seccion.getNota()).isEqualByComparingTo("45.00");
    assertThat(seccion.getRangoMin()).isEqualByComparingTo("0.00");
    assertThat(seccion.getRangoMax()).isEqualByComparingTo("45.00");
  }

  @Test
  void rechazaNotaCero() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "Saber",
            1,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ONE))
        .isInstanceOf(PesoInvalidoException.class)
        .satisfies(ex -> assertThat(((PesoInvalidoException) ex).getErrorCode()).isEqualTo("E_PESO_INVALIDO"));
  }

  @Test
  void rechazaNotaMayorACien() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "Saber",
            1,
            new BigDecimal("101"),
            BigDecimal.ZERO,
            new BigDecimal("100")))
        .isInstanceOf(PesoInvalidoException.class);
  }

  @Test
  void rechazaRangoMaxMayorQueNota() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "Saber",
            1,
            new BigDecimal("45"),
            BigDecimal.ZERO,
            new BigDecimal("50")))
        .isInstanceOf(ParametroRangoInvalidoException.class);
  }

  @Test
  void rechazaNombreEnBlanco() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "   ",
            1,
            new BigDecimal("45"),
            BigDecimal.ZERO,
            new BigDecimal("45")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
