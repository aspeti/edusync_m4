package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SeccionEvaluacionTest {

  @Test
  void creaSeccionConNotaValida() {
    SeccionEvaluacion seccion = SeccionEvaluacion.crear(
        SeccionEvaluacionId.nueva(),
        UUID.randomUUID(),
        GestionEscolarId.nueva(),
        "Saber",
        2,
        new BigDecimal("45"));

    assertThat(seccion.getNombre()).isEqualTo("Saber");
    assertThat(seccion.getOrden()).isEqualTo(2);
    assertThat(seccion.getNota()).isEqualByComparingTo("45.00");
  }

  @Test
  void rechazaNotaCero() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "Saber",
            1,
            BigDecimal.ZERO))
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
            new BigDecimal("101")))
        .isInstanceOf(PesoInvalidoException.class);
  }

  @Test
  void rechazaNombreEnBlanco() {
    assertThatThrownBy(() -> SeccionEvaluacion.crear(
            SeccionEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "   ",
            1,
            new BigDecimal("45")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
