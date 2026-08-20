package com.edusync.identidad.infrastructure.adapter.out.persistence;

import com.edusync.identidad.application.port.in.UsuarioFiltro;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Predicados JPA Criteria para {@code GET /api/v1/usuarios} (DD-UC-007). Package-private:
 * solo la usa {@link UsuarioRepositoryAdapter}, nunca cruza a {@code application}.
 */
final class UsuarioSpecifications {

  private UsuarioSpecifications() {}

  static Specification<UsuarioJpaEntity> deTenant(UUID tenantId) {
    return (root, query, cb) -> cb.equal(root.get("tenantId"), tenantId);
  }

  static Specification<UsuarioJpaEntity> conFiltro(UsuarioFiltro filtro) {
    return (root, query, cb) -> {
      List<Predicate> predicados = new ArrayList<>();

      if (filtro.q() != null && !filtro.q().isBlank()) {
        String termino = "%" + filtro.q().toLowerCase() + "%";
        predicados.add(
            cb.or(
                cb.like(cb.lower(root.get("nombreCompleto")), termino),
                cb.like(cb.lower(root.get("email")), termino)));
      }

      if (filtro.activo() != null) {
        predicados.add(cb.equal(root.get("activo"), filtro.activo()));
      }

      if (filtro.rol() != null) {
        if (query != null) {
          query.distinct(true);
        }
        var joinRoles = root.join("roles");
        predicados.add(cb.equal(joinRoles.get("rol"), filtro.rol().name()));
      }

      return cb.and(predicados.toArray(new Predicate[0]));
    };
  }
}
