package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: traduce entre {@code academico.domain.Paralelo} y
 * {@code ParaleloJpaEntity}. Filtra explicitamente por {@code tenantId}, mismo criterio de
 * mitigacion que {@link CursoRepositoryAdapter}.
 */
@Component
@RequiredArgsConstructor
class ParaleloRepositoryAdapter implements ParaleloRepositoryPort {

  private final ParaleloJpaRepository jpaRepository;

  @Override
  public Paralelo guardar(Paralelo paralelo) {
    ParaleloJpaEntity entity = new ParaleloJpaEntity(
        paralelo.getId().valor(), paralelo.getTenantId(), paralelo.getCursoId().valor(), paralelo.getNombre());
    ParaleloJpaEntity guardado = jpaRepository.save(entity);
    return aDominio(guardado);
  }

  @Override
  public Optional<Paralelo> buscarPorIdYTenant(ParaleloId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public List<Paralelo> listarPorCursoYTenant(CursoId cursoId, UUID tenantId) {
    return jpaRepository.findByCursoIdAndTenantId(cursoId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  private Paralelo aDominio(ParaleloJpaEntity entity) {
    return Paralelo.reconstruir(
        ParaleloId.de(entity.getId()), entity.getTenantId(), CursoId.de(entity.getCursoId()), entity.getNombre());
  }
}
