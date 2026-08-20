package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;

/** Puerto de entrada: alta de una {@link GestionEscolar} ({@code FSD-UC-012}, pasos 1-2). */
public interface CrearGestionEscolarUseCase {

  /**
   * @throws com.edusync.academico.domain.FechasInvalidasException si {@code fechaFin} no es
   *     posterior a {@code fechaInicio}
   */
  GestionEscolar crear(CrearGestionEscolarCommand command);
}
