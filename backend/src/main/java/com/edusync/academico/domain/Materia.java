package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate del modulo {@code academico} ({@code DD-UC-012}, {@code FSD-UC-018}): catalogo
 * de materias de un tenant (ej. "Matematicas"). Nace solo con {@code nombre}; las
 * asignaciones a {@link Curso}/{@link Paralelo} y a un profesor viven en aggregates
 * independientes ({@link AsignacionMateriaCurso}, {@link AsignacionMateriaProfesor}), no
 * como FKs embebidas (ver {@code DD-UC-012} &sect;2/&sect;3).
 *
 * <p>Sin estado ni transiciones. Inmutable (constructor privado + factory {@link #crear}),
 * Lombok solo {@code @Getter} (mismo criterio que {@link Curso}).
 */
@Getter
public final class Materia {

  private final MateriaId id;
  private final UUID tenantId;
  private final String nombre;

  private Materia(MateriaId id, UUID tenantId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
  }

  /** Factory de alta ({@code FSD-UC-018}, paso 1). */
  public static Materia crear(MateriaId id, UUID tenantId, String nombre) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    return new Materia(id, tenantId, nombre);
  }

  /** Reconstruye una {@link Materia} ya persistida (sin repetir las validaciones de alta). */
  public static Materia reconstruir(MateriaId id, UUID tenantId, String nombre) {
    return new Materia(id, tenantId, nombre);
  }
}
