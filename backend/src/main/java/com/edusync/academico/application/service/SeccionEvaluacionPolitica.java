package com.edusync.academico.application.service;

import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionesInmutablesException;
import com.edusync.academico.domain.SumaSeccionesInvalidaException;
import java.math.BigDecimal;
import java.util.List;

/** Invariantes de conjunto de secciones de una misma gestion ({@code DD-UC-016} &sect;2). */
final class SeccionEvaluacionPolitica {

  static final BigDecimal SUMA_REQUERIDA = new BigDecimal("100.00");

  private SeccionEvaluacionPolitica() {}

  static boolean hayPeriodoNoPendiente(List<PeriodoEvaluacion> periodos) {
    return periodos.stream().anyMatch(p -> p.getEstado() != EstadoPeriodoEvaluacion.PENDIENTE);
  }

  static void exigirMutables(List<PeriodoEvaluacion> periodos) {
    if (hayPeriodoNoPendiente(periodos)) {
      throw new SeccionesInmutablesException();
    }
  }

  static void exigirSumaCien(List<SeccionEvaluacion> secciones) {
    if (!sumaEsCien(secciones)) {
      throw new SumaSeccionesInvalidaException();
    }
  }

  static boolean sumaEsCien(List<SeccionEvaluacion> secciones) {
    if (secciones.isEmpty()) {
      return false;
    }
    BigDecimal suma = secciones.stream()
        .map(SeccionEvaluacion::getNota)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    return suma.compareTo(SUMA_REQUERIDA) == 0;
  }
}
