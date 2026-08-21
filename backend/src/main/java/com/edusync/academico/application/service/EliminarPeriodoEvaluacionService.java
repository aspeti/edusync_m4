package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.EliminarPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.PeriodoUnicoException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EliminarPeriodoEvaluacionService implements EliminarPeriodoEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional
  public void eliminar(UUID tenantId, UUID periodoId) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(periodoId), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);

    List<PeriodoEvaluacion> hermanos = periodoEvaluacionRepositoryPort.listarPorGestionYTenant(
        periodo.getGestionEscolarId(), tenantId);
    PeriodoEvaluacionPolitica.exigirMutables(hermanos);
    if (hermanos.size() <= 1) {
      throw new PeriodoUnicoException();
    }

    periodoEvaluacionRepositoryPort.eliminar(periodo.getId(), tenantId);

    List<PeriodoEvaluacion> restantes = periodoEvaluacionRepositoryPort.listarPorGestionYTenant(
        periodo.getGestionEscolarId(), tenantId);
    int orden = 1;
    for (PeriodoEvaluacion restante : restantes) {
      if (restante.getOrden() != orden) {
        restante.reasignarOrden(orden);
        periodoEvaluacionRepositoryPort.guardar(restante);
      }
      orden++;
    }
  }
}
