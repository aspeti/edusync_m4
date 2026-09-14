package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarPeriodosEvaluacionUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarPeriodosEvaluacionService implements ListarPeriodosEvaluacionUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<PeriodoEvaluacion> listar(UUID tenantId, UUID gestionEscolarId) {
    GestionEscolarId id = GestionEscolarId.de(gestionEscolarId);
    gestionEscolarRepositoryPort.buscarPorIdYTenant(id, tenantId)
        .orElseThrow(GestionEscolarNoEncontradaException::new);
    return periodoEvaluacionRepositoryPort.listarPorGestionYTenant(id, tenantId);
  }
}
