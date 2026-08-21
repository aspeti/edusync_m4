package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Curso;

/** Puerto de entrada: alta de un {@link Curso} ({@code FSD-UC-017}, paso 1). */
public interface CrearCursoUseCase {

  Curso crear(CrearCursoCommand command);
}
