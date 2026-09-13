package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;

/**
 * Puerto de entrada: actualizar nombre y/o fechas de una {@link GestionEscolar}
 * ({@code DD-UC-019}, {@code PATCH /{id}}, exclusivamente {@code ADMIN}).
 */
public interface ActualizarGestionEscolarUseCase {

  /**
   * @throws com.edusync.academico.domain.GestionEscolarNoEncontradaException si no existe o es
   *     de otro tenant
   * @throws com.edusync.academico.domain.FechasInvalidasException si la {@code fechaFin}
   *     resultante no es posterior a la {@code fechaInicio} resultante
   */
  GestionEscolar actualizar(ActualizarGestionEscolarCommand command);
}
