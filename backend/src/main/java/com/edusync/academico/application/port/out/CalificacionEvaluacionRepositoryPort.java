package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EvaluacionId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de {@link CalificacionEvaluacion}. Filtra explicitamente por
 * {@code tenantId} (mitigacion RLS, {@code DD-UC-018} §2).
 */
public interface CalificacionEvaluacionRepositoryPort {

  CalificacionEvaluacion guardar(CalificacionEvaluacion calificacion);

  List<CalificacionEvaluacion> guardarTodas(List<CalificacionEvaluacion> calificaciones);

  Optional<CalificacionEvaluacion> buscarPorEvaluacionEstudianteYTenant(
      EvaluacionId evaluacionId, EstudianteId estudianteId, UUID tenantId);

  List<CalificacionEvaluacion> listarPorEvaluacionYTenant(EvaluacionId evaluacionId, UUID tenantId);

  List<CalificacionEvaluacion> listarPorEvaluacionesEstudianteYTenant(
      Collection<EvaluacionId> evaluacionIds, EstudianteId estudianteId, UUID tenantId);
}
