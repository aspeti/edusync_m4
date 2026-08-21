package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SeccionEvaluacionRepositoryAdapter implements SeccionEvaluacionRepositoryPort {

  private final SeccionEvaluacionJpaRepository jpaRepository;

  @Override
  public SeccionEvaluacion guardar(SeccionEvaluacion seccion) {
    return aDominio(jpaRepository.save(aEntidad(seccion)));
  }

  @Override
  public Optional<SeccionEvaluacion> buscarPorIdYTenant(SeccionEvaluacionId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public List<SeccionEvaluacion> listarPorGestionYTenant(GestionEscolarId gestionEscolarId, UUID tenantId) {
    return jpaRepository
        .findByGestionEscolarIdAndTenantIdOrderByOrdenAsc(gestionEscolarId.valor(), tenantId)
        .stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public List<SeccionEvaluacion> reemplazarPlantilla(
      GestionEscolarId gestionEscolarId, UUID tenantId, List<SeccionEvaluacion> nuevas) {
    jpaRepository.deleteByGestionEscolarIdAndTenantId(gestionEscolarId.valor(), tenantId);
    jpaRepository.flush();
    return nuevas.stream().map(this::guardar).toList();
  }

  private SeccionEvaluacionJpaEntity aEntidad(SeccionEvaluacion seccion) {
    return new SeccionEvaluacionJpaEntity(
        seccion.getId().valor(),
        seccion.getTenantId(),
        seccion.getGestionEscolarId().valor(),
        seccion.getNombre(),
        seccion.getOrden(),
        seccion.getNota());
  }

  private SeccionEvaluacion aDominio(SeccionEvaluacionJpaEntity entity) {
    return SeccionEvaluacion.reconstruir(
        SeccionEvaluacionId.de(entity.getId()),
        entity.getTenantId(),
        GestionEscolarId.de(entity.getGestionEscolarId()),
        entity.getNombre(),
        entity.getOrden(),
        entity.getNota());
  }
}
