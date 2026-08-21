package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.MateriaFiltro;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class MateriaRepositoryAdapter implements MateriaRepositoryPort {

  private final MateriaJpaRepository jpaRepository;

  @Override
  public Optional<Materia> buscarPorIdYTenant(MateriaId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public Materia guardar(Materia materia) {
    MateriaJpaEntity entity =
        new MateriaJpaEntity(materia.getId().valor(), materia.getTenantId(), materia.getNombre());
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public PageResult<Materia> listarPorTenant(UUID tenantId, MateriaFiltro filtro, PageQuery pageQuery) {
    Specification<MateriaJpaEntity> spec =
        MateriaSpecifications.deTenant(tenantId).and(MateriaSpecifications.conFiltro(filtro));
    Page<MateriaJpaEntity> pagina = jpaRepository.findAll(spec, PageRequest.of(pageQuery.page(), pageQuery.size()));
    List<Materia> contenido = pagina.getContent().stream().map(this::aDominio).toList();
    return PageResult.of(contenido, pageQuery, pagina.getTotalElements());
  }

  private Materia aDominio(MateriaJpaEntity entity) {
    return Materia.reconstruir(MateriaId.de(entity.getId()), entity.getTenantId(), entity.getNombre());
  }
}
