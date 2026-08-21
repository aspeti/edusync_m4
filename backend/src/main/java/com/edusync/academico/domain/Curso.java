package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate del modulo {@code academico} ({@code DD-UC-010}, {@code FSD-UC-017}): catalogo de
 * cursos de un tenant (ej. "Primero de Primaria"), del que dependen {@link Paralelo},
 * {@code Materia} e {@code Inscripcion} ({@code FSD-UC-018}/{@code FSD-UC-020}, todavia sin
 * Design Doc propio).
 *
 * <p>A diferencia de {@code GestionEscolar}/{@code Tenant}, no tiene estado ni transiciones:
 * solo alta y listado en este slice (sin {@code PATCH}/{@code DELETE}, ver {@code DD-UC-010}
 * &sect;2/&sect;3). Inmutable (constructor privado + factory {@link #crear}), Lombok bajo el
 * *allowlist* de dominio ({@code ADR-0012}).
 */
@Getter
public final class Curso {

  private final CursoId id;
  private final UUID tenantId;
  private final String nombre;

  private Curso(CursoId id, UUID tenantId, String nombre) {
    this.id = id;
    this.tenantId = tenantId;
    this.nombre = nombre;
  }

  /** Factory de alta ({@code FSD-UC-017}, paso 1). */
  public static Curso crear(CursoId id, UUID tenantId, String nombre) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(nombre, "nombre no puede ser nulo");
    return new Curso(id, tenantId, nombre);
  }

  /** Reconstruye un {@link Curso} ya persistido (sin repetir las validaciones de alta). */
  public static Curso reconstruir(CursoId id, UUID tenantId, String nombre) {
    return new Curso(id, tenantId, nombre);
  }
}
