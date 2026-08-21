package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.AsignacionMateriaProfesorId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AsignacionMateriaProfesorRepositoryAdapter implements AsignacionMateriaProfesorRepositoryPort {

  private final AsignacionMateriaProfesorJpaRepository jpaRepository;

  @Override
  public AsignacionMateriaProfesor guardar(AsignacionMateriaProfesor asignacion) {
    AsignacionMateriaProfesorJpaEntity entity =
        new AsignacionMateriaProfesorJpaEntity(
            asignacion.getId().valor(),
            asignacion.getTenantId(),
            asignacion.getMateriaId().valor(),
            asignacion.getProfesorId(),
            asignacion.getCursoId().valor(),
            asignacion.getParaleloId().valor());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public List<AsignacionMateriaProfesor> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId) {
    return jpaRepository.findByMateriaIdAndTenantId(materiaId.valor(), tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  private AsignacionMateriaProfesor aDominio(AsignacionMateriaProfesorJpaEntity entity) {
    return AsignacionMateriaProfesor.reconstruir(
        AsignacionMateriaProfesorId.de(entity.getId()),
        entity.getTenantId(),
        MateriaId.de(entity.getMateriaId()),
        entity.getProfesorId(),
        CursoId.de(entity.getCursoId()),
        ParaleloId.de(entity.getParaleloId()));
  }
}
