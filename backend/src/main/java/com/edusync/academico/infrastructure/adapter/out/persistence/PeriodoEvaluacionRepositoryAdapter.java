package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PeriodoEvaluacionRepositoryAdapter implements PeriodoEvaluacionRepositoryPort {

  private final PeriodoEvaluacionJpaRepository jpaRepository;

  @Override
  public PeriodoEvaluacion guardar(PeriodoEvaluacion periodo) {
    PeriodoEvaluacionJpaEntity entity = new PeriodoEvaluacionJpaEntity(
        periodo.getId().valor(),
        periodo.getTenantId(),
        periodo.getGestionEscolarId().valor(),
        periodo.getNombre(),
        periodo.getFechaInicio(),
        periodo.getFechaFin(),
        periodo.getOrden(),
        periodo.getEstado().name());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public Optional<PeriodoEvaluacion> buscarPorIdYTenant(PeriodoEvaluacionId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public List<PeriodoEvaluacion> listarPorGestionYTenant(GestionEscolarId gestionEscolarId, UUID tenantId) {
    return jpaRepository
        .findByGestionEscolarIdAndTenantIdOrderByOrdenAsc(gestionEscolarId.valor(), tenantId)
        .stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public void eliminar(PeriodoEvaluacionId id, UUID tenantId) {
    jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .ifPresent(jpaRepository::delete);
  }

  private PeriodoEvaluacion aDominio(PeriodoEvaluacionJpaEntity entity) {
    return PeriodoEvaluacion.reconstruir(
        PeriodoEvaluacionId.de(entity.getId()),
        entity.getTenantId(),
        GestionEscolarId.de(entity.getGestionEscolarId()),
        entity.getNombre(),
        entity.getFechaInicio(),
        entity.getFechaFin(),
        entity.getOrden(),
        EstadoPeriodoEvaluacion.valueOf(entity.getEstado()));
  }
}
