package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarParametrosPeriodoUseCase;
import com.edusync.academico.application.port.out.ParametroPeriodoRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarParametrosPeriodoService implements ListarParametrosPeriodoUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final ParametroPeriodoRepositoryPort parametroPeriodoRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<ParametroPeriodo> listar(UUID tenantId, UUID periodoId) {
    PeriodoEvaluacionId id = PeriodoEvaluacionId.de(periodoId);
    periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(id, tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);
    return parametroPeriodoRepositoryPort.listarPorPeriodoYTenant(id, tenantId);
  }
}
