package com.edusync.academico.application.service;

import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.MateriaSinDocenteException;
import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.ParametrosIncompletosException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Gates de apertura de {@code FSD-UC-009} ({@code DD-UC-029}). */
final class AperturaPeriodoPolitica {

  private AperturaPeriodoPolitica() {}

  static void exigirParametrosCompletos(
      List<SeccionEvaluacion> secciones, List<ParametroPeriodo> parametros) {
    Set<UUID> vigentes = secciones.stream().map(seccion -> seccion.getId().valor()).collect(Collectors.toSet());
    Set<UUID> configuradas = parametros.stream()
        .map(parametro -> parametro.getSeccionEvaluacionId().valor())
        .collect(Collectors.toSet());
    if (!vigentes.equals(configuradas)) {
      throw new ParametrosIncompletosException();
    }
  }

  static void exigirCoberturaDocente(
      AsignacionMateriaCursoRepositoryPort cursos,
      AsignacionMateriaProfesorRepositoryPort profesores,
      UUID tenantId) {
    List<AsignacionMateriaCurso> conCurso = cursos.listarPorTenant(tenantId);
    if (conCurso.isEmpty()) {
      return;
    }
    Set<UUID> materiasConCurso = conCurso.stream()
        .map(asignacion -> asignacion.getMateriaId().valor())
        .collect(Collectors.toSet());
    Set<UUID> materiasConProfesor = profesores.listarPorTenant(tenantId).stream()
        .map(AsignacionMateriaProfesor::getMateriaId)
        .map(materiaId -> materiaId.valor())
        .collect(Collectors.toSet());
    if (!materiasConProfesor.containsAll(materiasConCurso)) {
      throw new MateriaSinDocenteException();
    }
  }
}
