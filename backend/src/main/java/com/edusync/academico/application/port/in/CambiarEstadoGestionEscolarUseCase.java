package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import java.util.UUID;

/**
 * Puerto de entrada: transicion de estado de una {@link GestionEscolar} ({@code FSD-UC-012},
 * pasos 3-4). Acepta cualquier {@link EstadoGestionEscolar} destino ({@code DD-UC-019}:
 * endpoint exclusivamente {@code ADMIN}).
 */
public interface CambiarEstadoGestionEscolarUseCase {

  /**
   * @throws com.edusync.academico.domain.GestionEscolarNoEncontradaException si {@code id} no
   *     existe o pertenece a un tenant distinto de {@code tenantIdActor}
   */
  GestionEscolar cambiarEstado(GestionEscolarId id, UUID tenantIdActor, EstadoGestionEscolar nuevoEstado);
}
