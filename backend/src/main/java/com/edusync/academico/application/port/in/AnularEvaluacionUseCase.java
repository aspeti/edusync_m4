package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Evaluacion;
import java.util.UUID;

public interface AnularEvaluacionUseCase {

  Evaluacion anular(UUID tenantId, UUID evaluacionId, UUID actorId, boolean actorEsAdmin);
}
