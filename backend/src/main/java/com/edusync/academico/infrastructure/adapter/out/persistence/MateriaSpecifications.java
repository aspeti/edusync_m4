package com.edusync.academico.infrastructure.adapter.out.persistence;

import com.edusync.academico.application.port.in.MateriaFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

final class MateriaSpecifications {

  private MateriaSpecifications() {}

  static Specification<MateriaJpaEntity> deTenant(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  static Specification<MateriaJpaEntity> conFiltro(MateriaFiltro filtro) {
    return (root, query, cb) -> {
      List<Predicate> predicados = new ArrayList<>();

      if (filtro.q() != null && !filtro.q().isBlank()) {
        predicados.add(cb.like(cb.lower(root.get("nombre")), "%" + filtro.q().toLowerCase() + "%"));
      }

      return cb.and(predicados.toArray(new Predicate[0]));
    };
  }
}
