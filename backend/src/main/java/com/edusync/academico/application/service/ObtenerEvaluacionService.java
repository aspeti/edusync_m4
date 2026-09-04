package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerEvaluacionUseCase;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.EvaluacionNoEncontradaException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerEvaluacionService implements ObtenerEvaluacionUseCase {

  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;

  @Override
  @Transactional(readOnly = true)
  public Evaluacion obtener(UUID tenantId, UUID evaluacionId, UUID actorId, boolean veTodasLasMaterias) {
    Evaluacion evaluacion = evaluacionRepositoryPort
        .buscarPorIdYTenant(EvaluacionId.de(evaluacionId), tenantId)
        .orElseThrow(EvaluacionNoEncontradaException::new);
    materiaAccesoService.exigirLectura(
        materiaAccesoService.exigirMateria(tenantId, evaluacion.getMateriaId().valor()),
        tenantId,
        actorId,
        veTodasLasMaterias);
    return evaluacion;
  }
}
