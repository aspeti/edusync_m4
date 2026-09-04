package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.AnularEvaluacionUseCase;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.EvaluacionNoEncontradaException;
import com.edusync.academico.domain.PeriodoNoAbiertoException;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnularEvaluacionService implements AnularEvaluacionUseCase {

  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;

  @Override
  @Transactional
  public Evaluacion anular(UUID tenantId, UUID evaluacionId, UUID actorId, boolean actorEsAdmin) {
    Evaluacion evaluacion = evaluacionRepositoryPort
        .buscarPorIdYTenant(EvaluacionId.de(evaluacionId), tenantId)
        .orElseThrow(EvaluacionNoEncontradaException::new);
    materiaAccesoService.exigirEscritura(
        materiaAccesoService.exigirMateria(tenantId, evaluacion.getMateriaId().valor()),
        tenantId,
        actorId,
        actorEsAdmin);
    var periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(evaluacion.getPeriodoEvaluacionId(), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);
    if (periodo.getEstado() != EstadoPeriodoEvaluacion.ABIERTO) {
      throw new PeriodoNoAbiertoException();
    }
    evaluacion.anular();
    return evaluacionRepositoryPort.guardar(evaluacion);
  }
}
