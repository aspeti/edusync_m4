package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
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

/**
 * Adaptador de salida: traduce entre {@code academico.domain.Curso} y {@code CursoJpaEntity}.
 * Filtra explicitamente por {@code tenantId} en toda consulta (mitigacion obligatoria
 * documentada en {@code CursoRepositoryPort}), sin depender solo de la politica RLS de
 * {@code curso}.
 */
@Component
@RequiredArgsConstructor
class CursoRepositoryAdapter implements CursoRepositoryPort {

  private final CursoJpaRepository jpaRepository;

  @Override
  public Optional<Curso> buscarPorIdYTenant(CursoId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public Curso guardar(Curso curso) {
    CursoJpaEntity entity = new CursoJpaEntity(curso.getId().valor(), curso.getTenantId(), curso.getNombre());
    CursoJpaEntity guardado = jpaRepository.save(entity);
    return aDominio(guardado);
  }

  @Override
  public PageResult<Curso> listarPorTenant(UUID tenantId, CursoFiltro filtro, PageQuery pageQuery) {
    Specification<CursoJpaEntity> spec =
        CursoSpecifications.deTenant(tenantId).and(CursoSpecifications.conFiltro(filtro));
    Page<CursoJpaEntity> pagina = jpaRepository.findAll(spec, PageRequest.of(pageQuery.page(), pageQuery.size()));
    List<Curso> contenido = pagina.getContent().stream().map(this::aDominio).toList();
    return PageResult.of(contenido, pageQuery, pagina.getTotalElements());
  }

  private Curso aDominio(CursoJpaEntity entity) {
    return Curso.reconstruir(CursoId.de(entity.getId()), entity.getTenantId(), entity.getNombre());
  }
}
