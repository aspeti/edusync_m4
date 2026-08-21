package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaCursoId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AsignacionMateriaCursoRepositoryAdapter implements AsignacionMateriaCursoRepositoryPort {

  private final AsignacionMateriaCursoJpaRepository jpaRepository;

  @Override
  public AsignacionMateriaCurso guardar(AsignacionMateriaCurso asignacion) {
    AsignacionMateriaCursoJpaEntity entity =
        new AsignacionMateriaCursoJpaEntity(
            asignacion.getId().valor(),
            asignacion.getTenantId(),
            asignacion.getMateriaId().valor(),
            asignacion.getCursoId().valor(),
            asignacion.getParaleloId().valor());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public List<AsignacionMateriaCurso> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId) {
    return jpaRepository.findByMateriaIdAndTenantId(materiaId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public boolean existePorMateriaCursoParaleloYTenant(
      MateriaId materiaId, CursoId cursoId, ParaleloId paraleloId, UUID tenantId) {
    return jpaRepository.existsByMateriaIdAndCursoIdAndParaleloIdAndTenantId(
        materiaId.valor(), cursoId.valor(), paraleloId.valor(), tenantId);
  }

  private AsignacionMateriaCurso aDominio(AsignacionMateriaCursoJpaEntity entity) {
    return AsignacionMateriaCurso.reconstruir(
        AsignacionMateriaCursoId.de(entity.getId()),
        entity.getTenantId(),
        MateriaId.de(entity.getMateriaId()),
        CursoId.de(entity.getCursoId()),
        ParaleloId.de(entity.getParaleloId()));
  }
}
