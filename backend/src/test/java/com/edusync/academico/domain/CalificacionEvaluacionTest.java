package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalificacionEvaluacionTest {

  @Test
  void crearNormalizaEscalaYAceptaLimites() {
    CalificacionEvaluacion c =
        CalificacionEvaluacion.crear(
            CalificacionEvaluacionId.nueva(),
            UUID.randomUUID(),
            EvaluacionId.nueva(),
            EstudianteId.nueva(),
            new BigDecimal("35"),
            new BigDecimal("45"));
    assertThat(c.getValor()).isEqualByComparingTo("35.00");
  }

  @Test
  void rechazaFueraDeRango() {
    assertThatThrownBy(
            () ->
                CalificacionEvaluacion.crear(
                    CalificacionEvaluacionId.nueva(),
                    UUID.randomUUID(),
                    EvaluacionId.nueva(),
                    EstudianteId.nueva(),
                    new BigDecimal("46"),
                    new BigDecimal("45")))
        .isInstanceOf(RangoCalificacionInvalidoException.class)
        .extracting(ex -> ((RangoCalificacionInvalidoException) ex).getErrorCode())
        .isEqualTo("E_RANGO_INVALIDO");
  }

  @Test
  void actualizarValorRevalida() {
    CalificacionEvaluacion c =
        CalificacionEvaluacion.crear(
            CalificacionEvaluacionId.nueva(),
            UUID.randomUUID(),
            EvaluacionId.nueva(),
            EstudianteId.nueva(),
            new BigDecimal("10"),
            new BigDecimal("45"));
    c.actualizarValor(new BigDecimal("40"), new BigDecimal("45"));
    assertThat(c.getValor()).isEqualByComparingTo("40.00");
  }
}
