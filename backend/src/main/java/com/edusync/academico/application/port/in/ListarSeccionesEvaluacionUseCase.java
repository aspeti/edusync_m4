package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.UUID;

/** @see com.edusync.academico.application.service.GestionEscolarVisibilidad */
public interface ListarSeccionesEvaluacionUseCase {

  /** @param actorVeTodas ver {@link com.edusync.academico.application.port.in.ObtenerGestionEscolarUseCase} */
  List<SeccionEvaluacion> listar(UUID tenantId, UUID gestionEscolarId, boolean actorVeTodas);
}
