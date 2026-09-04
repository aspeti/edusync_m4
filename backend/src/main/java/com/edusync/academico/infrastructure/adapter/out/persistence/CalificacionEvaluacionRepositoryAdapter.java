package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.CalificacionEvaluacionRepositoryPort;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.CalificacionEvaluacionId;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EvaluacionId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CalificacionEvaluacionRepositoryAdapter implements CalificacionEvaluacionRepositoryPort {

  private final CalificacionEvaluacionJpaRepository jpaRepository;

  @Override
  public CalificacionEvaluacion guardar(CalificacionEvaluacion calificacion) {
    return aDominio(persistir(calificacion));
  }

  @Override
  public List<CalificacionEvaluacion> guardarTodas(List<CalificacionEvaluacion> calificaciones) {
    List<CalificacionEvaluacion> guardadas = new ArrayList<>(calificaciones.size());
    for (CalificacionEvaluacion c : calificaciones) {
      guardadas.add(aDominio(persistir(c)));
    }
    return List.copyOf(guardadas);
  }

  @Override
  public Optional<CalificacionEvaluacion> buscarPorEvaluacionEstudianteYTenant(
      EvaluacionId evaluacionId, EstudianteId estudianteId, UUID tenantId) {
    return jpaRepository
        .findByEvaluacionIdAndEstudianteIdAndTenantId(
            evaluacionId.valor(), estudianteId.valor(), tenantId)
        .map(this::aDominio);
  }

  @Override
  public List<CalificacionEvaluacion> listarPorEvaluacionYTenant(
      EvaluacionId evaluacionId, UUID tenantId) {
    return jpaRepository.findByEvaluacionIdAndTenantId(evaluacionId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public List<CalificacionEvaluacion> listarPorEvaluacionesEstudianteYTenant(
      Collection<EvaluacionId> evaluacionIds, EstudianteId estudianteId, UUID tenantId) {
    if (evaluacionIds.isEmpty()) {
      return List.of();
    }
    List<UUID> ids = evaluacionIds.stream().map(EvaluacionId::valor).toList();
    return jpaRepository
        .findByEvaluacionIdInAndEstudianteIdAndTenantId(ids, estudianteId.valor(), tenantId)
        .stream()
        .map(this::aDominio)
        .toList();
  }

  private CalificacionEvaluacionJpaEntity persistir(CalificacionEvaluacion calificacion) {
    Instant ahora = Instant.now();
    Optional<CalificacionEvaluacionJpaEntity> existente =
        jpaRepository.findByEvaluacionIdAndEstudianteIdAndTenantId(
            calificacion.getEvaluacionId().valor(),
            calificacion.getEstudianteId().valor(),
            calificacion.getTenantId());
    if (existente.isPresent()) {
      CalificacionEvaluacionJpaEntity entity = existente.get();
      entity.actualizarValor(calificacion.getValor(), ahora);
      return jpaRepository.save(entity);
    }
    return jpaRepository.save(
        new CalificacionEvaluacionJpaEntity(
            calificacion.getId().valor(),
            calificacion.getTenantId(),
            calificacion.getEvaluacionId().valor(),
            calificacion.getEstudianteId().valor(),
            calificacion.getValor(),
            ahora));
  }

  private CalificacionEvaluacion aDominio(CalificacionEvaluacionJpaEntity entity) {
    return CalificacionEvaluacion.reconstruir(
        CalificacionEvaluacionId.de(entity.getId()),
        entity.getTenantId(),
        EvaluacionId.de(entity.getEvaluacionId()),
        EstudianteId.de(entity.getEstudianteId()),
        entity.getValor());
  }
}
