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

  // ── DD-UC-005: mutaciones inmutables ─────────────────────────────────────────

  @Test
  void conRolesDevuelveNuevaInstanciaConLosRolesReemplazados() {
    UUID tenantId = UUID.randomUUID();
    Usuario original =
        Usuario.crear(UsuarioId.nueva(), tenantId, "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);

    Usuario actualizado = original.conRoles(Set.of(Rol.ADMIN, Rol.SECRETARIA));

    assertThat(actualizado.getRoles()).containsExactlyInAnyOrder(Rol.ADMIN, Rol.SECRETARIA);
    assertThat(original.getRoles()).containsExactly(Rol.PROFESOR);
    assertThat(actualizado.getId()).isEqualTo(original.getId());
  }

  @Test
  void conRolesRevalidaLaInvarianteYRechazaSysAdminCombinado() {
    UUID tenantId = UUID.randomUUID();
    Usuario original =
        Usuario.crear(UsuarioId.nueva(), tenantId, "X", "x@x.com", "hash", Set.of(Rol.ADMIN), true);

    assertThatThrownBy(() -> original.conRoles(Set.of(Rol.SYSADMIN)))
        .isInstanceOf(InvarianteRolException.class);
  }

  @Test
  void conRolesRechazaConjuntoVacio() {
    Usuario original = Usuario.crear(
        UsuarioId.nueva(), UUID.randomUUID(), "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);

    assertThatThrownBy(() -> original.conRoles(Set.of())).isInstanceOf(InvarianteRolException.class);
  }

  @Test
  void activarYDesactivarDevuelvenNuevaInstanciaSinMutarLaOriginal() {
    Usuario activo = Usuario.crear(
        UsuarioId.nueva(), UUID.randomUUID(), "X", "x@x.com", "hash", Set.of(Rol.PROFESOR), true);

    Usuario desactivado = activo.desactivar();
    assertThat(desactivado.isActivo()).isFalse();
    assertThat(activo.isActivo()).isTrue();

    Usuario reactivado = desactivado.activar();
    assertThat(reactivado.isActivo()).isTrue();
  }

  @Test
  void conPasswordHashDevuelveNuevaInstanciaConElHashReemplazado() {
    Usuario original = Usuario.crear(
        UsuarioId.nueva(), UUID.randomUUID(), "X", "x@x.com", "hash-viejo", Set.of(Rol.PROFESOR), true);

    Usuario actualizado = original.conPasswordHash("hash-nuevo");

    assertThat(actualizado.getPasswordHash()).isEqualTo("hash-nuevo");
    assertThat(original.getPasswordHash()).isEqualTo("hash-viejo");
  }
}
