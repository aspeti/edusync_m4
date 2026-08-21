package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;

public interface CrearSeccionEvaluacionUseCase {

  SeccionEvaluacion crear(CrearSeccionEvaluacionCommand command);
}
