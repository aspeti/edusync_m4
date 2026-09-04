package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvaluacionTest {

  @Test
  void crearCopiaPuntajeMaximoYNaceActiva() {
    Evaluacion evaluacion = evaluacionDe("Prueba 1", new BigDecimal("45"));

    assertThat(evaluacion.getNombre()).isEqualTo("Prueba 1");
    assertThat(evaluacion.getPuntajeMaximo()).isEqualByComparingTo("45.00");
    assertThat(evaluacion.getEstado()).isEqualTo(EstadoEvaluacion.ACTIVA);
  }

  @Test
  void rechazaNombreEnBlanco() {
    assertThatThrownBy(() -> evaluacionDe("   ", new BigDecimal("45")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void anularDesdeActiva() {
    Evaluacion evaluacion = evaluacionDe("Prueba 1", new BigDecimal("45"));
    evaluacion.anular();
    assertThat(evaluacion.getEstado()).isEqualTo(EstadoEvaluacion.ANULADA);
  }

  @Test
  void anularYaAnuladaRechaza() {
    Evaluacion evaluacion = evaluacionDe("Prueba 1", new BigDecimal("45"));
    evaluacion.anular();
    assertThatThrownBy(evaluacion::anular)
        .isInstanceOf(EvaluacionYaAnuladaException.class)
        .satisfies(ex -> assertThat(((EvaluacionYaAnuladaException) ex).getErrorCode())
            .isEqualTo("E_EVALUACION_YA_ANULADA"));
  }

  private Evaluacion evaluacionDe(String nombre, BigDecimal puntajeMaximo) {
    return Evaluacion.crear(
        EvaluacionId.nueva(),
        UUID.randomUUID(),
        MateriaId.nueva(),
        PeriodoEvaluacionId.nueva(),
        SeccionEvaluacionId.nueva(),
        nombre,
        LocalDate.of(2026, 3, 1),
        puntajeMaximo,
        null);
  }
}
