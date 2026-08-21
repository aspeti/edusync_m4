package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Estudiante;

/** Puerto de entrada: alta de un {@link Estudiante} ({@code FSD-UC-020}, paso 1). */
public interface CrearEstudianteUseCase {

  Estudiante crear(CrearEstudianteCommand command);
}
