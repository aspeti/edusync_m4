package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.util.List;
import java.util.UUID;

/** Persistencia de {@link ParametroPeriodo}. Filtra explicitamente por {@code tenantId}. */
public interface ParametroPeriodoRepositoryPort {

  List<ParametroPeriodo> listarPorPeriodoYTenant(PeriodoEvaluacionId periodoId, UUID tenantId);

  List<ParametroPeriodo> reemplazarPorPeriodo(
      PeriodoEvaluacionId periodoId, UUID tenantId, List<ParametroPeriodo> nuevos);
}
