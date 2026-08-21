package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Materia;

/** Puerto de entrada: alta de una {@link Materia} ({@code FSD-UC-018}, paso 1). */
public interface CrearMateriaUseCase {

  Materia crear(CrearMateriaCommand command);
}
