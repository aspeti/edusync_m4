package com.edusync.plataforma.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de {@code tenant} (tabla SIN {@code tenant_id} ni politica RLS propia: es la
 * tabla que define los tenants, {@code V3__plataforma_tenant.sql}). Nunca se expone
 * directamente por API (AGENTS.md &sect;5): {@code TenantRepositoryAdapter} la traduce
 * a/desde {@code plataforma.domain.Tenant}. Lombok sin restriccion en {@code infrastructure/}
 * ({@code ADR-0012}).
 */
@Entity
@Table(name = "tenant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenantJpaEntity {

  @Id
  private UUID id;

  @Column(name = "nombre", nullable = false)
  private String nombre;

  @Column(name = "fecha_inicio_suscripcion", nullable = false)
  private LocalDate fechaInicioSuscripcion;

  @Column(name = "fecha_vencimiento_suscripcion", nullable = false)
  private LocalDate fechaVencimientoSuscripcion;

  @Column(name = "estado", nullable = false)
  private String estado;

  public TenantJpaEntity(
      UUID id,
      String nombre,
      LocalDate fechaInicioSuscripcion,
      LocalDate fechaVencimientoSuscripcion,
      String estado) {
    this.id = id;
    this.nombre = nombre;
    this.fechaInicioSuscripcion = fechaInicioSuscripcion;
    this.fechaVencimientoSuscripcion = fechaVencimientoSuscripcion;
    this.estado = estado;
  }
}
