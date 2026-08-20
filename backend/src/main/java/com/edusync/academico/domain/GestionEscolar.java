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
 * ciclo de vida ({@link #cambiarEstado}), el resto de campos permanecen finales. La
 * precondicion de periodos/secciones configurados antes de transicionar a {@code ACTIVA}
 * ({@code FSD-UC-012}, paso 3) esta deliberadamente diferida (no es una excepcion
 * bloqueante en el FSD): se puede activar con 0 periodos configurados.
 */
@Getter
public final class GestionEscolar {

  private final GestionEscolarId id;
  private final UUID tenantId;
  private final String nombre;
  private final LocalDate fechaInicio;
  private final LocalDate fechaFin;
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
   * Transicion de estado ({@code FSD-UC-012}, pasos 3-4: {@code PATCH .../estado}). Solo
   * permite {@code PLANIFICACION -> ACTIVA}, {@code ACTIVA -> CERRADA} y
   * {@code ACTIVA -> PLANIFICACION} (reabrir planificacion); {@code CERRADA} es terminal en
   * este slice. No valida periodos/secciones configurados (diferido, ver Javadoc de clase).
   *
   * @throws EstadoGestionEscolarInvalidoException si la transicion solicitada no esta permitida
   */
  public void cambiarEstado(EstadoGestionEscolar nuevoEstado) {
    Objects.requireNonNull(nuevoEstado, "nuevoEstado no puede ser nulo");
    boolean transicionValida = switch (estado) {
      case PLANIFICACION -> nuevoEstado == EstadoGestionEscolar.ACTIVA;
      case ACTIVA ->
          nuevoEstado == EstadoGestionEscolar.CERRADA || nuevoEstado == EstadoGestionEscolar.PLANIFICACION;
      case CERRADA -> false;
    };
    if (!transicionValida) {
      throw new EstadoGestionEscolarInvalidoException(estado, nuevoEstado);
    }
    this.estado = nuevoEstado;
  }
}
