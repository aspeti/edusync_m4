package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.SeccionEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class EvaluacionRepositoryAdapter implements EvaluacionRepositoryPort {

  private final EvaluacionJpaRepository jpaRepository;

  @Override
  public Evaluacion guardar(Evaluacion evaluacion) {
    return aDominio(jpaRepository.save(aEntidad(evaluacion)));
  }

  @Override
  public Optional<Evaluacion> buscarPorIdYTenant(EvaluacionId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public List<Evaluacion> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId) {
    return jpaRepository.findByMateriaIdAndTenantId(materiaId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public List<Evaluacion> listarPorMateriaPeriodoYTenant(
      MateriaId materiaId, PeriodoEvaluacionId periodoId, UUID tenantId) {
    return jpaRepository
        .findByMateriaIdAndPeriodoEvaluacionIdAndTenantId(materiaId.valor(), periodoId.valor(), tenantId)
        .stream()
        .map(this::aDominio)
        .toList();
  }

  private EvaluacionJpaEntity aEntidad(Evaluacion evaluacion) {
    return new EvaluacionJpaEntity(
        evaluacion.getId().valor(),
        evaluacion.getTenantId(),
        evaluacion.getMateriaId().valor(),
        evaluacion.getPeriodoEvaluacionId().valor(),
        evaluacion.getSeccionEvaluacionId().valor(),
        evaluacion.getNombre(),
        evaluacion.getFecha(),
        evaluacion.getPuntajeMaximo(),
        evaluacion.getDescripcion(),
        evaluacion.getEstado().name());
  }

  private Evaluacion aDominio(EvaluacionJpaEntity entity) {
    return Evaluacion.reconstruir(
        EvaluacionId.de(entity.getId()),
        entity.getTenantId(),
        MateriaId.de(entity.getMateriaId()),
        PeriodoEvaluacionId.de(entity.getPeriodoEvaluacionId()),
        SeccionEvaluacionId.de(entity.getSeccionEvaluacionId()),
        entity.getNombre(),
        entity.getFecha(),
        entity.getPuntajeMaximo(),
        entity.getDescripcion(),
        EstadoEvaluacion.valueOf(entity.getEstado()));
  }
}
