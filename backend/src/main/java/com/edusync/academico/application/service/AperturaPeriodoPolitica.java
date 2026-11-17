package com.edusync.academico.application.service;

import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.MateriaSinDocenteException;
import com.edusync.academico.domain.ParametroRangoInvalidoException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Gates de apertura ({@code FSD-UC-009} / {@code ADR-0027}). */
final class AperturaPeriodoPolitica {

  private AperturaPeriodoPolitica() {}

  /**
   * Defensa en profundidad: las secciones ya validan rangos al escribir; al abrir se
   * revalida que ninguna fila persistida haya quedado inconsistente.
   */
  static void exigirRangosSecciones(List<SeccionEvaluacion> secciones) {
    for (SeccionEvaluacion seccion : secciones) {
      BigDecimal min = seccion.getRangoMin();
      BigDecimal max = seccion.getRangoMax();
      BigDecimal nota = seccion.getNota();
      if (min == null
          || max == null
          || min.compareTo(BigDecimal.ZERO) < 0
          || max.compareTo(min) <= 0
          || max.compareTo(nota) > 0) {
        throw new ParametroRangoInvalidoException();
      }
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
