package com.edusync.identidad.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de la tabla de relacion {@code usuario_rol} (N:M, ADR-0010). Lombok sin
 * restriccion en {@code infrastructure/} (ADR-0012).
 */
@Entity
@Table(name = "usuario_rol")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsuarioRolJpaEntity {

  @Id
  private UUID id;

  @Getter(AccessLevel.NONE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private UsuarioJpaEntity usuario;

  @Column(name = "rol", nullable = false)
  private String rol;

  public UsuarioRolJpaEntity(UUID id, UsuarioJpaEntity usuario, String rol) {
    this.id = id;
    this.usuario = usuario;
    this.rol = rol;
  }
}
