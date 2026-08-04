package com.edusync.identidad.domain;

import com.edusync.identidad.UsuarioId;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Aggregate Root del modulo {@code identidad}.
 *
 * <p>Invariante permanente (ADR-0010, no transitoria de <em>bootstrap</em>):
 * {@code tenantId == null} si y solo si {@code roles} es exactamente {@code {SYSADMIN}}.
 * Validada aqui, en el dominio, no solo en la capa REST.
 *
 * <p>POJO inmutable: constructor privado + factory methods con validacion de invariante
 * ({@link #crear}/{@link #reconstruir}); sin setters ni builder publico. Los accessors de
 * los campos simples se generan con Lombok {@code @Getter} bajo el <em>allowlist</em> de
 * {@code ADR-0012} (nomenclatura JavaBean estandar: {@code getId()}, {@code isActivo()}).
 * {@link #getRoles()} se escribe a mano porque transforma el tipo del campo interno
 * ({@code Set<UsuarioRol>} &rarr; {@code Set<Rol>}), igual que {@link #tieneRol(Rol)}.
 */
@Getter
public final class Usuario {

  private final UsuarioId id;
  private final UUID tenantId;
  private final String nombreCompleto;
  private final String email;
  private final String passwordHash;

  @Getter(AccessLevel.NONE)
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

  public Set<Rol> getRoles() {
    return roles.stream().map(UsuarioRol::rol).collect(Collectors.toUnmodifiableSet());
  }

  public boolean tieneRol(Rol rol) {
    return getRoles().contains(rol);
  }

  /**
   * Devuelve una nueva instancia con el conjunto de roles reemplazado, revalidando la
   * invariante permanente de {@link #crear} (DD-UC-005 &sect;2, ADR-0010). Nunca muta esta
   * instancia.
   *
   * @throws InvarianteRolException si {@code nuevosRoles} viola la invariante
   */
  public Usuario conRoles(Set<Rol> nuevosRoles) {
    return crear(id, tenantId, nombreCompleto, email, passwordHash, nuevosRoles, activo);
  }

  /** Devuelve una nueva instancia activada (DD-UC-005 &sect;2). */
  public Usuario activar() {
    return crear(id, tenantId, nombreCompleto, email, passwordHash, getRoles(), true);
  }

  /** Devuelve una nueva instancia desactivada; no puede iniciar sesion (DD-UC-005 &sect;2). */
  public Usuario desactivar() {
    return crear(id, tenantId, nombreCompleto, email, passwordHash, getRoles(), false);
  }

  /** Devuelve una nueva instancia con el hash de contrasena reemplazado (DD-UC-005 &sect;2). */
  public Usuario conPasswordHash(String nuevoPasswordHash) {
    return crear(id, tenantId, nombreCompleto, email, nuevoPasswordHash, getRoles(), activo);
  }
}
