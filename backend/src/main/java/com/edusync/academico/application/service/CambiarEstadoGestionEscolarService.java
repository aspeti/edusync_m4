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

    gestionEscolar.cambiarEstado(nuevoEstado);
    return gestionEscolarRepositoryPort.guardar(gestionEscolar);
  }
}
