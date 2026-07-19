package com.edusync.plataforma.domain;

import java.time.LocalDate;
import java.util.Objects;
import lombok.Getter;

/**
 * Aggregate Root del modulo {@code plataforma} ({@code DD-UC-003}, {@code FSD-UC-011}).
 *
 * <p>A diferencia de {@code identidad.domain.Usuario} (POJO totalmente inmutable,
 * {@code ADR-0012}), {@code estado} SI muta a lo largo del ciclo de vida del Tenant (alta
 * en {@code ACTIVO}, luego {@code SUSPENDIDO}/{@code VENCIDO}/{@code ACTIVO} via
 * {@link #cambiarEstado} o el scheduler de vencimiento, {@link #marcarVencidoSiCorresponde}):
 * es inherente a {@code BR-013}/{@code BR-014} que el estado de un Tenant tenga ciclo de
 * vida. El resto de campos (id/nombre/fechas) permanecen finales; la mutacion de
 * {@code estado} pasa siempre por metodos de dominio con validacion, nunca por un
 * {@code @Setter} publico de Lombok (allowlist de {@code ADR-0012}).
 */
@Getter
public final class Tenant {

  private final TenantId id;
  private final String nombre;
  private final LocalDate fechaInicioSuscripcion;
  private final LocalDate fechaVencimientoSuscripcion;
  private EstadoTenant estado;

  private Tenant(
      TenantId id,
      String nombre,
      LocalDate fechaInicioSuscripcion,
      LocalDate fechaVencimientoSuscripcion,
      EstadoTenant estado) {
    this.id = id;
    this.nombre = nombre;
    this.fechaInicioSuscripcion = fechaInicioSuscripcion;
    this.fechaVencimientoSuscripcion = fechaVencimientoSuscripcion;
    this.estado = estado;
  }

  /**
   * Factory de alta ({@code FSD-UC-011}, paso 1): siempre nace en {@code ACTIVO}.
   *
   * @throws SuscripcionIncompletaException si {@code fechaVencimientoSuscripcion} es nula
   *     ({@code FSD-UC-011}, flujo alternativo A1)
   */
  public static Tenant crear(
      TenantId id, String nombre, LocalDate fechaInicioSuscripcion, LocalDate fechaVencimientoSuscripcion) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    Objects.requireNonNull(fechaInicioSuscripcion, "fechaInicioSuscripcion no puede ser nula");
    if (fechaVencimientoSuscripcion == null) {
      throw new SuscripcionIncompletaException();
    }
    return new Tenant(id, nombre, fechaInicioSuscripcion, fechaVencimientoSuscripcion, EstadoTenant.ACTIVO);
  }

  /** Reconstruye un {@link Tenant} ya persistido (sin repetir las validaciones de alta). */
  public static Tenant reconstruir(
      TenantId id,
      String nombre,
      LocalDate fechaInicioSuscripcion,
      LocalDate fechaVencimientoSuscripcion,
      EstadoTenant estado) {
    return new Tenant(id, nombre, fechaInicioSuscripcion, fechaVencimientoSuscripcion, estado);
  }

  /** Transicion manual de estado ({@code FSD-UC-011}, paso 4: {@code PATCH .../estado}). */
  public void cambiarEstado(EstadoTenant nuevoEstado) {
    this.estado = Objects.requireNonNull(nuevoEstado, "nuevoEstado no puede ser nulo");
  }

  /**
   * Transicion automatica del scheduler de vencimiento ({@code FSD-UC-011}, paso 5).
   * Idempotente y segura de invocar sobre cualquier Tenant: no hace nada si ya esta
   * {@code VENCIDO} o si la suscripcion todavia no vencio segun {@code fechaReferencia}.
   */
  public void marcarVencidoSiCorresponde(LocalDate fechaReferencia) {
    if (estado != EstadoTenant.VENCIDO && fechaVencimientoSuscripcion.isBefore(fechaReferencia)) {
      estado = EstadoTenant.VENCIDO;
    }
  }

  /** Usado por {@code TenantConsultaPort} ({@code identidad}, {@code BR-014}). */
  public boolean estaActivo() {
    return estado == EstadoTenant.ACTIVO;
  }
}
