package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.AsignacionMateriaCurso;

/** Puerto de entrada: asignar una Materia a un Curso/Paralelo ({@code FSD-UC-018}, paso 2). */
public interface CrearAsignacionCursoUseCase {

  AsignacionMateriaCurso crear(CrearAsignacionCursoCommand command);
}
