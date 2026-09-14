package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CambiarEstadoGestionEscolarUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa la transicion de estado de una Gestion Escolar ({@code FSD-UC-012}, pasos
 * 3-4), con el mismo filtro explicito de tenant que
 * {@code identidad.application.service.CambiarEstadoUsuarioService}.
 *
 * <p><strong>{@code DD-UC-021}:</strong> invariante "una sola gestion {@code ACTIVA} por
 * tenant" (requerido para que "la gestion actual" sea inequivoca en
 * {@code ObtenerGestionEscolarActivaService} y en el resto de flujos que la consumen de
 * forma implicita). Al transicionar a {@code ACTIVA}, si ya existe otra gestion del mismo
 * tenant en {@code ACTIVA}, esta se cierra automaticamente ({@code CERRADA}) en la misma
 * transaccion, antes de activar la solicitada.
 */
@Service
@RequiredArgsConstructor
public class CambiarEstadoGestionEscolarService implements CambiarEstadoGestionEscolarUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional
  public GestionEscolar cambiarEstado(GestionEscolarId id, UUID tenantIdActor, EstadoGestionEscolar nuevoEstado) {
    GestionEscolar gestionEscolar = gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantIdActor)
        .orElseThrow(GestionEscolarNoEncontradaException::new);

    if (nuevoEstado == EstadoGestionEscolar.ACTIVA) {
      cerrarOtraActivaSiExiste(id, tenantIdActor);
    }

    gestionEscolar.cambiarEstado(nuevoEstado);
    return gestionEscolarRepositoryPort.guardar(gestionEscolar);
  }

  /** {@code DD-UC-021}: auto-cierra la gestion {@code ACTIVA} previa del tenant (si no es {@code id}). */
  private void cerrarOtraActivaSiExiste(GestionEscolarId id, UUID tenantIdActor) {
    gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantIdActor)
        .filter(otra -> !otra.getId().equals(id))
        .ifPresent(otra -> {
          otra.cambiarEstado(EstadoGestionEscolar.CERRADA);
          gestionEscolarRepositoryPort.guardar(otra);
        });
  }
}
