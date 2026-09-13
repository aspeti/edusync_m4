package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.PeriodoEvaluacion;
import java.util.List;
import java.util.UUID;

/** @see com.edusync.academico.application.service.GestionEscolarVisibilidad */
public interface ListarPeriodosEvaluacionUseCase {

  /** @param actorVeTodas ver {@link com.edusync.academico.application.port.in.ObtenerGestionEscolarUseCase} */
  List<PeriodoEvaluacion> listar(UUID tenantId, UUID gestionEscolarId, boolean actorVeTodas);
}
