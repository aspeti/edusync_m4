package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.ParametroPeriodoRepositoryPort;
import com.edusync.academico.domain.ParametroPeriodo;
import com.edusync.academico.domain.ParametroPeriodoId;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.SeccionEvaluacionId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ParametroPeriodoRepositoryAdapter implements ParametroPeriodoRepositoryPort {

  private final ParametroPeriodoJpaRepository jpaRepository;

  @Override
  public List<ParametroPeriodo> listarPorPeriodoYTenant(PeriodoEvaluacionId periodoId, UUID tenantId) {
    return jpaRepository.findByPeriodoEvaluacionIdAndTenantId(periodoId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public List<ParametroPeriodo> reemplazarPorPeriodo(
      PeriodoEvaluacionId periodoId, UUID tenantId, List<ParametroPeriodo> nuevos) {
    jpaRepository.deleteByPeriodoEvaluacionIdAndTenantId(periodoId.valor(), tenantId);
    jpaRepository.flush();
    return nuevos.stream().map(this::guardar).toList();
  }

  private ParametroPeriodo guardar(ParametroPeriodo parametro) {
    return aDominio(jpaRepository.save(aEntidad(parametro)));
  }

  private ParametroPeriodoJpaEntity aEntidad(ParametroPeriodo parametro) {
    return new ParametroPeriodoJpaEntity(
        parametro.getId().valor(),
        parametro.getTenantId(),
        parametro.getPeriodoEvaluacionId().valor(),
        parametro.getSeccionEvaluacionId().valor(),
        parametro.getRangoMin(),
        parametro.getRangoMax(),
        parametro.getReglaCombinacion());
  }

  private ParametroPeriodo aDominio(ParametroPeriodoJpaEntity entity) {
    return ParametroPeriodo.reconstruir(
        ParametroPeriodoId.de(entity.getId()),
        entity.getTenantId(),
        PeriodoEvaluacionId.de(entity.getPeriodoEvaluacionId()),
        SeccionEvaluacionId.de(entity.getSeccionEvaluacionId()),
        entity.getRangoMin(),
        entity.getRangoMax(),
        entity.getReglaCombinacion());
  }
}
