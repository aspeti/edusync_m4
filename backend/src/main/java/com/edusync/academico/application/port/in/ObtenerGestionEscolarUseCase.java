package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import java.util.UUID;

/**
 * Puerto de entrada: detalle de una Gestion Escolar ({@code DD-UC-015}: {@code GET /{id}}).
 *
 * @see com.edusync.academico.application.service.GestionEscolarVisibilidad
 */
public interface ObtenerGestionEscolarUseCase {

  /**
   * @param actorVeTodas {@code true} si el actor es {@code ADMIN} (ve cualquier estado);
   *     {@code false} restringe a la gestion {@code ACTIVA} ({@code DD-UC-019})
   * @throws com.edusync.academico.domain.GestionEscolarNoEncontradaException si no existe,
   *     es de otro tenant, o {@code actorVeTodas} es {@code false} y no esta {@code ACTIVA}
   */
  GestionEscolar obtener(GestionEscolarId id, UUID tenantId, boolean actorVeTodas);
}
