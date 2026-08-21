package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import java.util.UUID;

/** Puerto de entrada: detalle de una Gestion Escolar ({@code DD-UC-015}: {@code GET /{id}}). */
public interface ObtenerGestionEscolarUseCase {

  /**
   * @throws com.edusync.academico.domain.GestionEscolarNoEncontradaException si no existe
   *     o es de otro tenant
   */
  GestionEscolar obtener(GestionEscolarId id, UUID tenantId);
}
