package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.PeriodoEvaluacion;

public interface ActualizarPeriodoEvaluacionUseCase {

  PeriodoEvaluacion actualizar(ActualizarPeriodoEvaluacionCommand command);
}
