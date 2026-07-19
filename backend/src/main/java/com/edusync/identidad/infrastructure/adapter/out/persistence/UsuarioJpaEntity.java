package com.edusync.identidad.infrastructure.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code usuario} (tabla plataforma-scoped, RLS con {@code OR tenant_id IS
 * NULL}, ver {@code V2__identidad_usuario.sql}). Nunca se expone directamente por API
 * (AGENTS.md &sect;5): {@code UsuarioRepositoryAdapter} la traduce a/desde
 * {@code identidad.domain.Usuario}. Lombok sin restriccion en {@code infrastructure/}
 * (ADR-0012): {@code @Getter} reemplaza los accessors manuales.
 */
@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioJpaEntity {

  @Id
  private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId;

  @Column(name = "nombre_completo", nullable = false)
  private String nombreCompleto;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "activo", nullable = false)
  private boolean activo;

  @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private Set<UsuarioRolJpaEntity> roles = new HashSet<>();

  public UsuarioJpaEntity(
      UUID id, UUID tenantId, String nombreCompleto, String email, String passwordHash, boolean activo) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombreCompleto = nombreCompleto;
    this.email = email;
    this.passwordHash = passwordHash;
    this.activo = activo;
  }

  public void agregarRol(String rol) {
    roles.add(new UsuarioRolJpaEntity(UUID.randomUUID(), this, rol));
  }
}
