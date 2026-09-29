package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ParametroPeriodoTest {

  private final UUID tenantId = UUID.randomUUID();
  private final PeriodoEvaluacionId periodoId = PeriodoEvaluacionId.nueva();
  private final SeccionEvaluacionId seccionId = SeccionEvaluacionId.nueva();

  @Test
  void crearNormalizaEscalaYAceptaRangoPositivo() {
    ParametroPeriodo parametro = ParametroPeriodo.crear(
        ParametroPeriodoId.nueva(),
        tenantId,
        periodoId,
        seccionId,
        new BigDecimal("0"),
        new BigDecimal("45.5"),
        ReglaCombinacionPeriodo.PROMEDIO_SIMPLE);

    assertThat(parametro.getRangoMin()).isEqualByComparingTo("0.00");
    assertThat(parametro.getRangoMax()).isEqualByComparingTo("45.50");
    assertThat(parametro.getReglaCombinacion()).isEqualTo(ReglaCombinacionPeriodo.PROMEDIO_SIMPLE);
  }

  @Test
  void rangoMinNegativoRechaza() {
    assertThatThrownBy(() -> crear(new BigDecimal("-1"), new BigDecimal("10")))
        .isInstanceOf(ParametroRangoInvalidoException.class);
  }

  @Test
  void rangoMaxMenorOIgualAlMinimoRechaza() {
    assertThatThrownBy(() -> crear(new BigDecimal("10"), new BigDecimal("10")))
        .isInstanceOf(ParametroRangoInvalidoException.class);
  }

  @Test
  void reglaDistintaDePromedioSimpleRechaza() {
    assertThatThrownBy(() -> ReglaCombinacionPeriodo.parse("SUMA"))
        .isInstanceOf(ReglaCombinacionNoSoportadaException.class);
    assertThatThrownBy(() -> ReglaCombinacionPeriodo.parse(" "))
        .isInstanceOf(ReglaCombinacionNoSoportadaException.class);
  }

  private ParametroPeriodo crear(BigDecimal min, BigDecimal max) {
    return ParametroPeriodo.crear(
        ParametroPeriodoId.nueva(),
        tenantId,
        periodoId,
        seccionId,
        min,
        max,
        ReglaCombinacionPeriodo.PROMEDIO_SIMPLE);
  }
}
