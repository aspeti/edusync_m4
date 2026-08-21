package com.edusync.academico.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-013}, {@code FSD-UC-020}): vinculo de un
 * {@link Estudiante} a una {@link GestionEscolar} + {@link Curso}/{@link Paralelo}.
 * Nace siempre con {@link EstadoInscripcion#ACTIVA} (el POST no acepta {@code estado}).
 *
 * <p>El llamador (capa de aplicacion) valida previamente que Estudiante, GestionEscolar,
 * Curso y Paralelo existan y pertenezcan al tenant, y que el Paralelo pertenezca al Curso.
 */
@Getter
public final class Inscripcion {

  private final InscripcionId id;
  private final UUID tenantId;
  private final EstudianteId estudianteId;
  private final GestionEscolarId gestionEscolarId;
  private final CursoId cursoId;
  private final ParaleloId paraleloId;
  private final LocalDate fechaInscripcion;
  private final EstadoInscripcion estado;

  private Inscripcion(
      InscripcionId id,
      UUID tenantId,
      EstudianteId estudianteId,
      GestionEscolarId gestionEscolarId,
      CursoId cursoId,
      ParaleloId paraleloId,
      LocalDate fechaInscripcion,
      EstadoInscripcion estado) {
    this.id = id;
    this.tenantId = tenantId;
    this.estudianteId = estudianteId;
    this.gestionEscolarId = gestionEscolarId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
    this.fechaInscripcion = fechaInscripcion;
    this.estado = estado;
  }

  /** Factory de alta ({@code FSD-UC-020}, pasos 2-3): siempre nace {@code ACTIVA}. */
  public static Inscripcion crear(
      InscripcionId id,
      UUID tenantId,
      EstudianteId estudianteId,
      GestionEscolarId gestionEscolarId,
      CursoId cursoId,
      ParaleloId paraleloId,
      LocalDate fechaInscripcion) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(estudianteId, "estudianteId no puede ser nulo");
    Objects.requireNonNull(gestionEscolarId, "gestionEscolarId no puede ser nulo");
    Objects.requireNonNull(cursoId, "cursoId no puede ser nulo");
    Objects.requireNonNull(paraleloId, "paraleloId no puede ser nulo");
    Objects.requireNonNull(fechaInscripcion, "fechaInscripcion no puede ser nula");
    return new Inscripcion(
        id,
        tenantId,
        estudianteId,
        gestionEscolarId,
        cursoId,
        paraleloId,
        fechaInscripcion,
        EstadoInscripcion.ACTIVA);
  }

  /** Reconstruye una {@link Inscripcion} ya persistida. */
  public static Inscripcion reconstruir(
      InscripcionId id,
      UUID tenantId,
      EstudianteId estudianteId,
      GestionEscolarId gestionEscolarId,
      CursoId cursoId,
      ParaleloId paraleloId,
      LocalDate fechaInscripcion,
      EstadoInscripcion estado) {
    return new Inscripcion(
        id, tenantId, estudianteId, gestionEscolarId, cursoId, paraleloId, fechaInscripcion, estado);
  }
}
