package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.EstudianteFiltro;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.EstadoEstudiante;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class EstudianteRepositoryAdapter implements EstudianteRepositoryPort {

  private final EstudianteJpaRepository jpaRepository;

  @Override
  public Optional<Estudiante> buscarPorIdYTenant(EstudianteId id, UUID tenantId) {
    return jpaRepository.findById(id.valor())
        .filter(entity -> entity.getTenantId().equals(tenantId))
        .map(this::aDominio);
  }

  @Override
  public List<Estudiante> listarPorIdsYTenant(Collection<EstudianteId> ids, UUID tenantId) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    List<UUID> valores = ids.stream().map(EstudianteId::valor).toList();
    return jpaRepository.findByIdInAndTenantId(valores, tenantId).stream()
        .map(this::aDominio)
        .toList();
  }

  @Override
  public boolean existePorRudeYTenant(String rude, UUID tenantId) {
    return jpaRepository.existsByTenantIdAndRudeIgnoreCase(tenantId, rude);
  }

  @Override
  public Estudiante guardar(Estudiante estudiante) {
    Map<String, String> datos =
        estudiante.getDatosPersonales() == null || estudiante.getDatosPersonales().isEmpty()
            ? null
            : estudiante.getDatosPersonales();
    EstudianteJpaEntity entity =
        new EstudianteJpaEntity(
            estudiante.getId().valor(),
            estudiante.getTenantId(),
            estudiante.getRude(),
            estudiante.getNombreCompleto(),
            estudiante.getEstado().name(),
            datos);
    return aDominio(jpaRepository.save(entity));
  }

  @Override
  public PageResult<Estudiante> listarPorTenant(UUID tenantId, EstudianteFiltro filtro, PageQuery pageQuery) {
    Specification<EstudianteJpaEntity> spec =
        EstudianteSpecifications.deTenant(tenantId).and(EstudianteSpecifications.conFiltro(filtro));
    Page<EstudianteJpaEntity> pagina = jpaRepository.findAll(spec, PageRequest.of(pageQuery.page(), pageQuery.size()));
    List<Estudiante> contenido = pagina.getContent().stream().map(this::aDominio).toList();
    return PageResult.of(contenido, pageQuery, pagina.getTotalElements());
  }

  private Estudiante aDominio(EstudianteJpaEntity entity) {
    return Estudiante.reconstruir(
        EstudianteId.de(entity.getId()),
        entity.getTenantId(),
        entity.getRude(),
        entity.getNombreCompleto(),
        EstadoEstudiante.valueOf(entity.getEstado()),
        entity.getDatosPersonales());
  }
}
