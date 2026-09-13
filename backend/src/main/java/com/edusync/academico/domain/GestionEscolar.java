package com.edusync.academico.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate Root del modulo {@code academico} ({@code DD-UC-008}, {@code FSD-UC-012}):
 * el contenedor raiz por tenant del que dependen {@code PeriodoEvaluacion}, {@code Curso}
 * e {@code Inscripcion} ({@code FSD-UC-013}..{@code FSD-UC-020}, todavia sin Design Doc
 * propio).
 *
 * <p>Mismo patron que {@code plataforma.domain.Tenant}: {@code estado} muta a lo largo del
 * ciclo de vida ({@link #cambiarEstado}); {@code nombre}/{@code fechaInicio}/{@code fechaFin}
 * mutan via {@link #actualizarDatos} ({@code DD-UC-019}, {@code PATCH /{id}}, solo
 * {@code ADMIN}). La precondicion de periodos/secciones configurados antes de transicionar
 * a {@code ACTIVA} ({@code FSD-UC-012}, paso 3) esta deliberadamente diferida (no es una
 * excepcion bloqueante en el FSD): se puede activar con 0 periodos configurados.
 *
 * <p><b>{@code DD-UC-019}</b>: {@code ADMIN} puede transicionar a <em>cualquier</em>
 * {@link EstadoGestionEscolar} desde cualquier estado (sin maquina de estados restringida):
 * el endpoint que invoca {@link #cambiarEstado} es exclusivamente {@code ADMIN}
 * ({@code @PreAuthorize("hasRole('ADMIN')")} en {@code GestionEscolarController}), por lo
 * que la restriccion de transicion previa (solo
 * {@code PLANIFICACION -> ACTIVA -> CERRADA} + reapertura) ya no aporta una barrera de
 * seguridad real, solo friccion operativa para el unico actor autorizado.
 */
@Getter
public final class GestionEscolar {

  private final GestionEscolarId id;
  private final UUID tenantId;
  private String nombre;
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private EstadoGestionEscolar estado;

  private GestionEscolar(
      GestionEscolarId id,
      UUID tenantId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      EstadoGestionEscolar estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
    this.fechaInicio = fechaInicio;
    this.fechaFin = fechaFin;
    this.estado = estado;
  }

  /**
   * Factory de alta ({@code FSD-UC-012}, pasos 1-2): siempre nace en {@code PLANIFICACION}.
   *
   * @throws FechasInvalidasException si {@code fechaFin} no es posterior a {@code fechaInicio}
   *     ({@code FSD-UC-012}, flujo alternativo A1)
   */
  public static GestionEscolar crear(
      GestionEscolarId id, UUID tenantId, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    Objects.requireNonNull(fechaInicio, "fechaInicio no puede ser nula");
    Objects.requireNonNull(fechaFin, "fechaFin no puede ser nula");
    if (!fechaFin.isAfter(fechaInicio)) {
      throw new FechasInvalidasException();
    }
    return new GestionEscolar(id, tenantId, nombre, fechaInicio, fechaFin, EstadoGestionEscolar.PLANIFICACION);
  }

  /** Reconstruye una {@link GestionEscolar} ya persistida (sin repetir las validaciones de alta). */
  public static GestionEscolar reconstruir(
      GestionEscolarId id,
      UUID tenantId,
      String nombre,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      EstadoGestionEscolar estado) {
    return new GestionEscolar(id, tenantId, nombre, fechaInicio, fechaFin, estado);
  }

  /**
   * Transicion de estado ({@code FSD-UC-012}, pasos 3-4: {@code PATCH .../estado},
   * {@code ADMIN}). Acepta cualquier {@link EstadoGestionEscolar} destino ({@code DD-UC-019},
   * ver Javadoc de clase). No valida periodos/secciones configurados (diferido, ver Javadoc
   * de clase).
   */
  public void cambiarEstado(EstadoGestionEscolar nuevoEstado) {
    Objects.requireNonNull(nuevoEstado, "nuevoEstado no puede ser nulo");
    this.estado = nuevoEstado;
  }

  /**
   * Actualiza nombre y fechas ({@code DD-UC-019}, {@code PATCH /{id}}, {@code ADMIN}). Campos
   * {@code null} conservan el valor actual (mismo patron que
   * {@link PeriodoEvaluacion#actualizar}). Sin restriccion de estado ni de periodos/secciones
   * (el endpoint es exclusivamente {@code ADMIN}).
   *
   * @throws FechasInvalidasException si la {@code fechaFin} resultante no es posterior a la
   *     {@code fechaInicio} resultante
   */
  public void actualizarDatos(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
    String nuevoNombre = nombre != null ? nombre : this.nombre;
    LocalDate nuevaFechaInicio = fechaInicio != null ? fechaInicio : this.fechaInicio;
    LocalDate nuevaFechaFin = fechaFin != null ? fechaFin : this.fechaFin;
    if (!nuevaFechaFin.isAfter(nuevaFechaInicio)) {
      throw new FechasInvalidasException();
    }
    this.nombre = nuevoNombre;
    this.fechaInicio = nuevaFechaInicio;
    this.fechaFin = nuevaFechaFin;
  }
}
