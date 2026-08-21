package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Inscripcion;

/** Puerto de entrada: alta de una {@link Inscripcion} ({@code FSD-UC-020}, pasos 2-3). */
public interface CrearInscripcionUseCase {

  Inscripcion crear(CrearInscripcionCommand command);
}
