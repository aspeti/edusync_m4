package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;

public interface ActualizarSeccionEvaluacionUseCase {

  SeccionEvaluacion actualizar(ActualizarSeccionEvaluacionCommand command);
}
