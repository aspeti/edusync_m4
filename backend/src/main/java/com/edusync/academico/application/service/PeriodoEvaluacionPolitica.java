package com.edusync.academico.application.service;

import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodosInmutablesException;
import com.edusync.academico.domain.PeriodosSolapadosException;
import java.util.List;

/** Invariantes de conjunto de periodos de una misma gestion ({@code DD-UC-015} &sect;2). */
final class PeriodoEvaluacionPolitica {

  private PeriodoEvaluacionPolitica() {}

  static boolean hayAbierto(List<PeriodoEvaluacion> periodos) {
    return periodos.stream().anyMatch(p -> p.getEstado() == EstadoPeriodoEvaluacion.ABIERTO);
  }

  static void exigirMutables(List<PeriodoEvaluacion> periodos) {
    if (hayAbierto(periodos)) {
      throw new PeriodosInmutablesException();
    }
  }

  static void exigirSinSolape(List<PeriodoEvaluacion> existentes, PeriodoEvaluacion candidato) {
    for (PeriodoEvaluacion existente : existentes) {
      if (!existente.getId().equals(candidato.getId()) && existente.solapaCon(candidato)) {
        throw new PeriodosSolapadosException();
      }
    }
  }
}
