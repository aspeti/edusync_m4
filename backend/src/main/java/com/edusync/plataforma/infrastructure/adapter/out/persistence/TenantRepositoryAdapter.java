package com.edusync.plataforma.infrastructure.adapter.out.persistence;

import com.edusync.plataforma.application.port.in.TenantFiltro;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.EstadoTenant;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantId;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: traduce entre {@code plataforma.domain.Tenant} y
 * {@code TenantJpaEntity}. Sin filtro de tenant (la tabla {@code tenant} no tiene
 * {@code tenant_id}, ver {@code V3__plataforma_tenant.sql}).
 */
@Component
@RequiredArgsConstructor
class TenantRepositoryAdapter implements TenantRepositoryPort {

  private final TenantJpaRepository jpaRepository;

  @Override
  public Optional<Tenant> buscarPorId(TenantId id) {
    return jpaRepository.findById(id.valor()).map(this::aDominio);
  }

  @Override
  public Tenant guardar(Tenant tenant) {
    TenantJpaEntity entity = new TenantJpaEntity(
        tenant.getId().valor(),
        tenant.getNombre(),
        tenant.getFechaInicioSuscripcion(),
        tenant.getFechaVencimientoSuscripcion(),
        tenant.getEstado().name());
    TenantJpaEntity guardado = jpaRepository.save(entity);
    return aDominio(guardado);
  }

  @Override
  public List<Tenant> listarPendientesDeVencer(LocalDate fechaReferencia) {
    return jpaRepository.findByEstadoNotAndFechaVencimientoSuscripcionBefore(
            EstadoTenant.VENCIDO.name(), fechaReferencia)
        .stream()
        .map(this::aDominio)
        .collect(Collectors.toList());
  }

  @Override
  public PageResult<Tenant> listarTodos(TenantFiltro filtro, PageQuery pageQuery) {
    Page<TenantJpaEntity> pagina = jpaRepository.findAll(
        TenantSpecifications.conFiltro(filtro), PageRequest.of(pageQuery.page(), pageQuery.size()));
    List<Tenant> contenido = pagina.getContent().stream().map(this::aDominio).collect(Collectors.toList());
    return PageResult.of(contenido, pageQuery, pagina.getTotalElements());
  }

  private Tenant aDominio(TenantJpaEntity entity) {
    return Tenant.reconstruir(
        TenantId.de(entity.getId()),
        entity.getNombre(),
        entity.getFechaInicioSuscripcion(),
        entity.getFechaVencimientoSuscripcion(),
        EstadoTenant.valueOf(entity.getEstado()));
  }
}
