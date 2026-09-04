package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Evaluacion;

public interface ActualizarEvaluacionUseCase {

  Evaluacion actualizar(ActualizarEvaluacionCommand command);
}
