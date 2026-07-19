package com.edusync.identidad.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edusync.identidad.UsuarioId;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Invariante permanente de {@link Usuario} (ADR-0010): {@code tenantId == null} si y solo
 * si {@code roles == {SYSADMIN}}.
 */
class UsuarioTest {

  @Test
  void permiteSysAdminSinTenant() {
    Usuario sysAdmin = Usuario.crear(
        UsuarioId.nueva(), null, "Sys Admin", "sysadmin@edusync.local", "hash", Set.of(Rol.SYSADMIN), true);

    assertThat(sysAdmin.getTenantId()).isNull();
    assertThat(sysAdmin.getRoles()).containsExactly(Rol.SYSADMIN);
  }

  @Test
  void permiteAdminDeTenantConMultiplesRoles() {
    UUID tenantId = UUID.randomUUID();
    Usuario admin = Usuario.crear(
        UsuarioId.nueva(),
        tenantId,
        "Admin Tenant",
        "admin@colegio.edu.bo",
        "hash",
        Set.of(Rol.ADMIN, Rol.PROFESOR),
        true);

    assertThat(admin.getTenantId()).isEqualTo(tenantId);
    assertThat(admin.getRoles()).containsExactlyInAnyOrder(Rol.ADMIN, Rol.PROFESOR);
  }

  @Test
  void rechazaSysAdminConTenantId() {
    assertThatThrownBy(() -> Usuario.crear(
        UsuarioId.nueva(), UUID.randomUUID(), "X", "x@x.com", "hash", Set.of(Rol.SYSADMIN), true))
        .isInstanceOf(InvarianteRolException.class)
        .satisfies(ex -> assertThat(((InvarianteRolException) ex).getErrorCode()).isEqualTo("E_INVARIANTE_ROL_VIOLADA"));
  }

  @Test
  void rechazaTenantNuloSinRolSysAdmin() {
    assertThatThrownBy(() -> Usuario.crear(
        UsuarioId.nueva(), null, "X", "x@x.com", "hash", Set.of(Rol.ADMIN), true))
        .isInstanceOf(InvarianteRolException.class);
  }

  @Test
  void rechazaSysAdminCombinadoConRolDeTenant() {
    assertThatThrownBy(() -> Usuario.crear(
        UsuarioId.nueva(),
        UUID.randomUUID(),
        "X",
        "x@x.com",
        "hash",
        Set.of(Rol.SYSADMIN, Rol.ADMIN),
        true))
        .isInstanceOf(InvarianteRolException.class);
  }

  @Test
  void rechazaUsuarioSinRoles() {
    assertThatThrownBy(() -> Usuario.crear(UsuarioId.nueva(), null, "X", "x@x.com", "hash", Set.of(), true))
        .isInstanceOf(InvarianteRolException.class);
  }
}
