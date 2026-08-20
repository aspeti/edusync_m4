package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.EstadoGestionEscolar;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
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
 * Adaptador de salida: traduce entre {@code academico.domain.GestionEscolar} y
 * {@code GestionEscolarJpaEntity}. Filtra explicitamente por {@code tenantId} en toda
 * consulta (mitigacion obligatoria documentada en {@code GestionEscolarRepositoryPort}),
 * sin depender solo de la politica RLS de {@code gestion_escolar}.
 */
@Component
@RequiredArgsConstructor
class GestionEscolarRepositoryAdapter implements GestionEscolarRepositoryPort {

  private final GestionEscolarJpaRepository jpaRepository;

  @Override
  public Optional<GestionEscolar> buscarPorIdYTenant(GestionEscolarId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public GestionEscolar guardar(GestionEscolar gestionEscolar) {
    GestionEscolarJpaEntity entity = new GestionEscolarJpaEntity(
        gestionEscolar.getId().valor(),
        gestionEscolar.getTenantId(),
        gestionEscolar.getNombre(),
        gestionEscolar.getFechaInicio(),
        gestionEscolar.getFechaFin(),
        gestionEscolar.getEstado().name());
    GestionEscolarJpaEntity guardado = jpaRepository.save(entity);
    return aDominio(guardado);
  }

  @Override
  public PageResult<GestionEscolar> listarPorTenant(UUID tenantId, GestionEscolarFiltro filtro, PageQuery pageQuery) {
    Specification<GestionEscolarJpaEntity> spec =
        GestionEscolarSpecifications.deTenant(tenantId).and(GestionEscolarSpecifications.conFiltro(filtro));
    Page<GestionEscolarJpaEntity> pagina =
        jpaRepository.findAll(spec, PageRequest.of(pageQuery.page(), pageQuery.size()));
    List<GestionEscolar> contenido = pagina.getContent().stream().map(this::aDominio).toList();
    return PageResult.of(contenido, pageQuery, pagina.getTotalElements());
  }

  private GestionEscolar aDominio(GestionEscolarJpaEntity entity) {
    return GestionEscolar.reconstruir(
        GestionEscolarId.de(entity.getId()),
        entity.getTenantId(),
        entity.getNombre(),
        entity.getFechaInicio(),
        entity.getFechaFin(),
        EstadoGestionEscolar.valueOf(entity.getEstado()));
  }
}
