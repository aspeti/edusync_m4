package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de {@link Evaluacion}. Filtra explicitamente por {@code tenantId}
 * (mitigacion RLS, {@code DD-UC-017} &sect;2).
 */
public interface EvaluacionRepositoryPort {

  Evaluacion guardar(Evaluacion evaluacion);

  Optional<Evaluacion> buscarPorIdYTenant(EvaluacionId id, UUID tenantId);

  List<Evaluacion> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId);

  List<Evaluacion> listarPorMateriaPeriodoYTenant(
      MateriaId materiaId, PeriodoEvaluacionId periodoId, UUID tenantId);
}
