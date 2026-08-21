package com.edusync.academico.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente de {@link GestionEscolar} ({@code DD-UC-015}, {@code FSD-UC-013}):
 * un periodo de evaluacion con {@code orden} 1-based. La secuencialidad, el solape y la
 * inmutabilidad de N se validan en la capa de aplicacion (invariante de conjunto).
 *
 * <p>Lombok solo {@code @Getter} (mismo criterio que {@link GestionEscolar}).
 */
@Getter
public final class PeriodoEvaluacion {

  private final PeriodoEvaluacionId id;
  private final UUID tenantId;
  private final GestionEscolarId gestionEscolarId;
  private String nombre;
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private int orden;
  private EstadoPeriodoEvaluacion estado;

  private PeriodoEvaluacion(
      PeriodoEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      int orden,
      EstadoPeriodoEvaluacion estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.gestionEscolarId = gestionEscolarId;
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.orden = orden;
    this.estado = estado;
  }

  /**
   * Factory de alta: siempre nace en {@code PENDIENTE}.
   *
   * @throws FechasInvalidasException si {@code fechaFin} no es posterior a {@code fechaInicio}
   */
  public static PeriodoEvaluacion crear(
      PeriodoEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      int orden) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(gestionEscolarId, "gestionEscolarId no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    Objects.requireNonNull(fechaInicio, "fechaInicio no puede ser nula");
    Objects.requireNonNull(fechaFin, "fechaFin no puede ser nula");
    validarFechas(fechaInicio, fechaFin);
    if (orden < 1) {
      throw new IllegalArgumentException("orden debe ser >= 1");
    }
    return new PeriodoEvaluacion(
        id, tenantId, gestionEscolarId, nombre, fechaInicio, fechaFin, orden, EstadoPeriodoEvaluacion.PENDIENTE);
  }

  public static PeriodoEvaluacion reconstruir(
      PeriodoEvaluacionId id,
      UUID tenantId,
      GestionEscolarId gestionEscolarId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      int orden,
      EstadoPeriodoEvaluacion estado) {
    return new PeriodoEvaluacion(
        id, tenantId, gestionEscolarId, nombre, fechaInicio, fechaFin, orden, estado);
  }

  public void actualizar(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    Objects.requireNonNull(fechaInicio, "fechaInicio no puede ser nula");
    Objects.requireNonNull(fechaFin, "fechaFin no puede ser nula");
    validarFechas(fechaInicio, fechaFin);
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
  }

  public void reasignarOrden(int nuevoOrden) {
    if (nuevoOrden < 1) {
      throw new IllegalArgumentException("orden debe ser >= 1");
    }
    this.orden = nuevoOrden;
  }

  /**
   * {@code PENDIENTE -> ABIERTO} y {@code ABIERTO -> CERRADO}. La secuencialidad se enforcea
   * en {@code CambiarEstadoPeriodoService}.
   */
  public void cambiarEstado(EstadoPeriodoEvaluacion nuevoEstado) {
    Objects.requireNonNull(nuevoEstado, "nuevoEstado no puede ser nulo");
    boolean transicionValida = switch (estado) {
      case PENDIENTE -> nuevoEstado == EstadoPeriodoEvaluacion.ABIERTO;
      case ABIERTO -> nuevoEstado == EstadoPeriodoEvaluacion.CERRADO;
      case CERRADO -> false;
    };
    if (!transicionValida) {
      throw new EstadoPeriodoEvaluacionInvalidoException(estado, nuevoEstado);
    }
    this.estado = nuevoEstado;
  }

  /** Intervalos inclusivos: solapan si cada uno empieza antes o el mismo dia que el otro termina. */
  public boolean solapaCon(PeriodoEvaluacion otro) {
    Objects.requireNonNull(otro, "otro no puede ser nulo");
    return !fechaFin.isBefore(otro.fechaInicio) && !otro.fechaFin.isBefore(fechaInicio);
  }

  private static void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    if (!fechaFin.isAfter(fechaInicio)) {
      throw new FechasInvalidasException();
    }
  }
}
