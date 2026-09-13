package com.edusync.academico.application.service;

import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodosSolapadosException;
import java.util.List;

/**
 * Invariantes de conjunto de periodos de una misma gestion ({@code DD-UC-015} &sect;2).
 *
 * <p><b>{@code DD-UC-019}</b>: el freeze de inmutabilidad ({@code E_PERIODOS_INMUTABLES},
 * bloqueaba POST/PATCH/DELETE si habia un periodo {@code ABIERTO}) se elimino: los tres
 * escritores ({@code Crear}/{@code Actualizar}/{@code Eliminar}
 * {@code PeriodoEvaluacionService}) son exclusivamente {@code ADMIN}, y el requisito de
 * negocio es que {@code ADMIN} pueda editar periodos en cualquier momento. Solo permanece
 * la invariante de no-solape, que es de integridad de datos (no de bloqueo de edicion).
 */
final class PeriodoEvaluacionPolitica {

  private PeriodoEvaluacionPolitica() {}

  static void exigirSinSolape(List<PeriodoEvaluacion> existentes, PeriodoEvaluacion candidato) {
    for (PeriodoEvaluacion existente : existentes) {
      if (!existente.getId().equals(candidato.getId()) && existente.solapaCon(candidato)) {
        throw new PeriodosSolapadosException();
      }
    }
  }
}
