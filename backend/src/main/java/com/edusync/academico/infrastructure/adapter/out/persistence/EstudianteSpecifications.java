package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.EstudianteFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class EstudianteSpecifications {

  private EstudianteSpecifications() {}

  static Specification<EstudianteJpaEntity> deTenant(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  static Specification<EstudianteJpaEntity> conFiltro(EstudianteFiltro filtro) {
    return (root, query, cb) -> {
      List<Predicate> predicados = new ArrayList<>();

      if (filtro.q() != null && !filtro.q().isBlank()) {
        String termino = filtro.q().toLowerCase();
        predicados.add(
            cb.or(
                cb.like(cb.lower(root.get("nombreCompleto")), "%" + termino + "%"),
                cb.equal(cb.lower(root.get("rude")), termino)));
      }

      if (filtro.estado() != null) {
        predicados.add(cb.equal(root.get("estado"), filtro.estado().name()));
      }

      return cb.and(predicados.toArray(new Predicate[0]));
    };
  }
}
