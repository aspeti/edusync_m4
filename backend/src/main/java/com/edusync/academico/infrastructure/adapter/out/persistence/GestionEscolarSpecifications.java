package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados JPA Criteria para {@code GET /api/v1/gestiones-escolares} (DD-UC-008,
 * filtros/paginacion DD-UC-007). Package-private: solo la usa
 * {@link GestionEscolarRepositoryAdapter}.
 */
final class GestionEscolarSpecifications {

  private GestionEscolarSpecifications() {}

  static Specification<GestionEscolarJpaEntity> deTenant(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  static Specification<GestionEscolarJpaEntity> conFiltro(GestionEscolarFiltro filtro) {
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
