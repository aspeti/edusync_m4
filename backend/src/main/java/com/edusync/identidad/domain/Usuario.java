package com.edusync.identidad.domain;

import com.edusync.identidad.UsuarioId;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Aggregate Root del modulo {@code identidad}.
 *
 * <p>Invariante permanente (ADR-0010, no transitoria de <em>bootstrap</em>):
 * {@code tenantId == null} si y solo si {@code roles} es exactamente {@code {SYSADMIN}}.
 * Validada aqui, en el dominio, no solo en la capa REST.
 */
public final class Usuario {

  private final UsuarioId id;
  private final UUID tenantId;
  private final String nombreCompleto;
  private final String email;
  private final String passwordHash;
  private final Set<UsuarioRol> roles;
  private final boolean activo;

  private Usuario(
      UsuarioId id,
      UUID tenantId,
      String nombreCompleto,
      String email,
      String passwordHash,
      Set<UsuarioRol> roles,
      boolean activo) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombreCompleto = nombreCompleto;
    this.email = email;
    this.passwordHash = passwordHash;
    this.roles = Set.copyOf(roles);
    this.activo = activo;
  }

  /**
   * Factory principal: valida la invariante de rol antes de construir el aggregate.
   *
   * @throws InvarianteRolException si {@code tenantId}/{@code roles} violan la invariante
   *     permanente {@code tenantId == null <=> roles == {SYSADMIN}}
   */
  public static Usuario crear(
      UsuarioId id,
      UUID tenantId,
      String nombreCompleto,
      String email,
      String passwordHash,
      Set<Rol> roles,
      boolean activo) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(nombreCompleto, "nombreCompleto no puede ser nulo");
    Objects.requireNonNull(email, "email no puede ser nulo");
    Objects.requireNonNull(passwordHash, "passwordHash no puede ser nulo");
    Objects.requireNonNull(roles, "roles no puede ser nulo");
    if (roles.isEmpty()) {
      throw new InvarianteRolException("Un usuario debe tener al menos un rol");
    }

    boolean esSoloSysAdmin = roles.equals(Set.of(Rol.SYSADMIN));
    if (tenantId == null && !esSoloSysAdmin) {
      throw new InvarianteRolException(
          "tenantId nulo solo es valido si roles == {SYSADMIN}; roles recibidos: " + roles);
    }
    if (tenantId != null && roles.contains(Rol.SYSADMIN)) {
      throw new InvarianteRolException(
          "SYSADMIN nunca se combina con un tenantId ni con roles de tenant (ADR-0010)");
    }

    Set<UsuarioRol> usuarioRoles =
        roles.stream().map(UsuarioRol::new).collect(Collectors.toUnmodifiableSet());
    return new Usuario(id, tenantId, nombreCompleto, email, passwordHash, usuarioRoles, activo);
  }

  /** Reconstruye un {@link Usuario} ya persistido (misma invariante que {@link #crear}). */
  public static Usuario reconstruir(
      UsuarioId id,
      UUID tenantId,
      String nombreCompleto,
      String email,
      String passwordHash,
      Set<Rol> roles,
      boolean activo) {
    return crear(id, tenantId, nombreCompleto, email, passwordHash, roles, activo);
  }

  public UsuarioId id() {
    return id;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public String nombreCompleto() {
    return nombreCompleto;
  }

  public String email() {
    return email;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public Set<Rol> roles() {
    return roles.stream().map(UsuarioRol::rol).collect(Collectors.toUnmodifiableSet());
  }

  public boolean tieneRol(Rol rol) {
    return roles().contains(rol);
  }

  public boolean activo() {
    return activo;
  }
}
