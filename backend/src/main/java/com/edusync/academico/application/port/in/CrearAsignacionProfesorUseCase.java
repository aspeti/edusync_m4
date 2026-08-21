package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.AsignacionMateriaProfesor;

/** Puerto de entrada: asignar un Profesor a una Materia en un Curso/Paralelo ({@code FSD-UC-018}, paso 3). */
public interface CrearAsignacionProfesorUseCase {

  AsignacionMateriaProfesor crear(CrearAsignacionProfesorCommand command);
}
