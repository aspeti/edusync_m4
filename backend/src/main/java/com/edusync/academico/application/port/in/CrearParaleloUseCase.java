package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Paralelo;

/** Puerto de entrada: alta de un {@link Paralelo} ({@code FSD-UC-017}, paso 2). */
public interface CrearParaleloUseCase {

  /**
   * @throws com.edusync.academico.domain.CursoNoEncontradoException si el curso padre no
   *     existe o pertenece a un tenant distinto
   */
  Paralelo crear(CrearParaleloCommand command);
}
