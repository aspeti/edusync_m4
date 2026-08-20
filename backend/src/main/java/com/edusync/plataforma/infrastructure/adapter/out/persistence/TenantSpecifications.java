package com.edusync.plataforma.infrastructure.adapter.out.persistence;

import com.edusync.plataforma.application.port.in.TenantFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados JPA Criteria para {@code GET /api/v1/plataforma/tenants} (DD-UC-007).
 * Package-private: solo la usa {@link TenantRepositoryAdapter}.
 */
final class TenantSpecifications {

  private TenantSpecifications() {}

  static Specification<TenantJpaEntity> conFiltro(TenantFiltro filtro) {
    return (root, query, cb) -> {
      List<Predicate> predicados = new ArrayList<>();

      if (filtro.q() != null && !filtro.q().isBlank()) {
        predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.q().toLowerCase() + "%"));
      }

      if (filtro.estado() != null) {
        predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
      }

      return cb.and(predicados.toArray(new Predicate[0]));
    };
  }
}
