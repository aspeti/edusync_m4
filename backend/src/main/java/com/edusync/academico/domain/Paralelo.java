package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate del modulo {@code academico} ({@code DD-UC-010}, {@code FSD-UC-017}): subdivision
 * de un {@link Curso} (ej. "A", "B"). Entidad independiente con su propio repositorio, no una
 * coleccion embebida dentro de {@link Curso} (ver justificacion en {@code DD-UC-010} &sect;2):
 * {@code Materia}/{@code Inscripcion}/{@code Usuario.curso_asignado_id} (Design Docs futuros)
 * referencian un {@link Paralelo} por id sin necesidad de cargar el {@link Curso} completo.
 *
 * <p>Sin estado ni transiciones, igual que {@link Curso}. Inmutable (constructor privado +
 * factory {@link #crear}), Lombok bajo el *allowlist* de dominio ({@code ADR-0012}).
 */
@Getter
public final class Paralelo {

  private final ParaleloId id;
  private final UUID tenantId;
  private final CursoId cursoId;
  private final String nombre;

  private Paralelo(ParaleloId id, UUID tenantId, CursoId cursoId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.cursoId = cursoId;
    this.nombre = nombre;
  }

  /**
   * Factory de alta ({@code FSD-UC-017}, paso 2). El llamador (capa de aplicacion) es
   * responsable de validar previamente que el {@link Curso} padre exista y pertenezca a
   * {@code tenantId} ({@code CursoNoEncontradoException} si no, ver {@code DD-UC-010} &sect;2).
   */
  public static Paralelo crear(ParaleloId id, UUID tenantId, CursoId cursoId, String nombre) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(cursoId, "cursoId no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    return new Paralelo(id, tenantId, cursoId, nombre);
  }

  /** Reconstruye un {@link Paralelo} ya persistido (sin repetir las validaciones de alta). */
  public static Paralelo reconstruir(ParaleloId id, UUID tenantId, CursoId cursoId, String nombre) {
    return new Paralelo(id, tenantId, cursoId, nombre);
  }
}
