package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-012}): vinculo de una {@link Materia} a un
 * {@link Curso}/{@link Paralelo} concretos. No esta embebido en {@link Materia} para que
 * una misma materia de catalogo (ej. "Matematicas") pueda asignarse a N paralelos.
 *
 * <p>El llamador (capa de aplicacion) valida previamente que Materia, Curso y Paralelo
 * existan y pertenezcan al tenant, y que el Paralelo pertenezca al Curso.
 */
@Getter
public final class AsignacionMateriaCurso {

  private final AsignacionMateriaCursoId id;
  private final UUID tenantId;
  private final MateriaId materiaId;
  private final CursoId cursoId;
  private final ParaleloId paraleloId;

  private AsignacionMateriaCurso(
      AsignacionMateriaCursoId id,
      UUID tenantId,
      MateriaId materiaId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
  }

  public static AsignacionMateriaCurso crear(
      AsignacionMateriaCursoId id,
      UUID tenantId,
      MateriaId materiaId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(materiaId, "materiaId no puede ser nulo");
    Objects.requireNonNull(cursoId, "cursoId no puede ser nulo");
    Objects.requireNonNull(paraleloId, "paraleloId no puede ser nulo");
    return new AsignacionMateriaCurso(id, tenantId, materiaId, cursoId, paraleloId);
  }

  public static AsignacionMateriaCurso reconstruir(
      AsignacionMateriaCursoId id,
      UUID tenantId,
      MateriaId materiaId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    return new AsignacionMateriaCurso(id, tenantId, materiaId, cursoId, paraleloId);
  }
}
