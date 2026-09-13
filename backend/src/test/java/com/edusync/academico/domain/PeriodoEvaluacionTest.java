package com.edusync.academico.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PeriodoEvaluacionTest {

  @Test
  void creaPeriodoPendienteConFechasValidas() {
    PeriodoEvaluacion periodo = unPeriodo("Trimestre 1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 1), 1);

    assertThat(periodo.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.PENDIENTE);
    assertThat(periodo.getOrden()).isEqualTo(1);
  }

  @Test
  void rechazaFechaFinNoPosteriorAFechaInicio() {
    assertThatThrownBy(() -> PeriodoEvaluacion.crear(
            PeriodoEvaluacionId.nueva(),
            UUID.randomUUID(),
            GestionEscolarId.nueva(),
            "T1",
            LocalDate.of(2027, 2, 1),
            LocalDate.of(2027, 2, 1),
            1))
        .isInstanceOf(FechasInvalidasException.class);
  }

  @Test
  void transicionaPendienteAAbiertoACerrado() {
    PeriodoEvaluacion periodo = unPeriodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 5, 1), 1);

    periodo.cambiarEstado(EstadoPeriodoEvaluacion.ABIERTO);
    assertThat(periodo.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.ABIERTO);

    periodo.cambiarEstado(EstadoPeriodoEvaluacion.CERRADO);
    assertThat(periodo.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.CERRADO);
  }

  /**
   * {@code DD-UC-019}: el endpoint que invoca {@code cambiarEstado} es exclusivamente
   * {@code ADMIN}; el requisito de negocio es que pueda reabrir/cerrar cualquier periodo en
   * cualquier orden, incluida la reapertura de un periodo {@code CERRADO} (antes terminal).
   */
  @Test
  void permiteReabrirCerrado() {
    PeriodoEvaluacion periodo = PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.nueva(),
        UUID.randomUUID(),
        GestionEscolarId.nueva(),
        "T1",
        LocalDate.of(2027, 2, 1),
        LocalDate.of(2027, 5, 1),
        1,
        EstadoPeriodoEvaluacion.CERRADO);

    periodo.cambiarEstado(EstadoPeriodoEvaluacion.ABIERTO);

    assertThat(periodo.getEstado()).isEqualTo(EstadoPeriodoEvaluacion.ABIERTO);
  }

  @Test
  void detectaSolapeInclusivo() {
    PeriodoEvaluacion a = unPeriodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 4, 30), 1);
    PeriodoEvaluacion b = unPeriodo("T2", LocalDate.of(2027, 4, 30), LocalDate.of(2027, 7, 31), 2);

    assertThat(a.solapaCon(b)).isTrue();
  }

  @Test
  void noSolapaCuandoSonContiguos() {
    PeriodoEvaluacion a = unPeriodo("T1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 4, 30), 1);
    PeriodoEvaluacion b = unPeriodo("T2", LocalDate.of(2027, 5, 1), LocalDate.of(2027, 7, 31), 2);

    assertThat(a.solapaCon(b)).isFalse();
  }

  private PeriodoEvaluacion unPeriodo(String nombre, LocalDate inicio, LocalDate fin, int orden) {
    return PeriodoEvaluacion.crear(
        PeriodoEvaluacionId.nueva(), UUID.randomUUID(), GestionEscolarId.nueva(), nombre, inicio, fin, orden);
  }
}
