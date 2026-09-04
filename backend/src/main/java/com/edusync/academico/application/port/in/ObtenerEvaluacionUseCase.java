package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Evaluacion;
import java.util.UUID;

/** Puerto de entrada: detalle de una {@link Evaluacion}. */
public interface ObtenerEvaluacionUseCase {

  Evaluacion obtener(UUID tenantId, UUID evaluacionId, UUID actorId, boolean veTodasLasMaterias);
}
