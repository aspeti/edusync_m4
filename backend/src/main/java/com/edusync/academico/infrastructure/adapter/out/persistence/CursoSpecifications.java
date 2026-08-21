package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.CursoFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados JPA Criteria para {@code GET /api/v1/cursos} (DD-UC-010, filtros/paginacion
 * DD-UC-007). Package-private: solo la usa {@link CursoRepositoryAdapter}.
 */
final class CursoSpecifications {

  private CursoSpecifications() {}

  static Specification<CursoJpaEntity> deTenant(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  static Specification<CursoJpaEntity> conFiltro(CursoFiltro filtro) {
    return (root, query, cb) -> {
      List<Predicate> predicados = new ArrayList<>();

      if (filtro.q() != null && !filtro.q().isBlank()) {
        predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.q().toLowerCase() + "%"));
      }

      return cb.and(predicados.toArray(new Predicate[0]));
    };
  }
}
