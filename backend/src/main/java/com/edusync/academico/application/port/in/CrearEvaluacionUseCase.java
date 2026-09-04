package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Evaluacion;

/** Puerto de entrada: alta de {@link Evaluacion} ({@code FSD-UC-015}). */
public interface CrearEvaluacionUseCase {

  Evaluacion crear(CrearEvaluacionCommand command);
}
