package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.PeriodoEvaluacion;

public interface CrearPeriodoEvaluacionUseCase {

  PeriodoEvaluacion crear(CrearPeriodoEvaluacionCommand command);
}
