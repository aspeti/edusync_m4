package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarSeccionesEvaluacionUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarSeccionesEvaluacionService implements ListarSeccionesEvaluacionUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<SeccionEvaluacion> listar(UUID tenantId, UUID gestionEscolarId, boolean actorVeTodas) {
    GestionEscolarId id = GestionEscolarId.de(gestionEscolarId);
    GestionEscolar gestionEscolar = gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)
        .orElseThrow(GestionEscolarNoEncontradaException::new);
    GestionEscolarVisibilidad.exigirVisible(gestionEscolar, actorVeTodas);
    return seccionEvaluacionRepositoryPort.listarPorGestionYTenant(id, tenantId);
  }
}
