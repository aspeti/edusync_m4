package com.edusync.academico.domain;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

/**
 * Aggregate independiente ({@code DD-UC-012}): vinculo de un profesor ({@code Usuario} con
 * rol {@code PROFESOR}) a una {@link Materia} en un {@link Curso}/{@link Paralelo}.
 *
 * <p>{@code profesorId} es el {@code Usuario.id}; no hay entidad {@code Profesor} propia
 * ({@code FSD-UC-019} paso 1 ya cubierto por {@code FSD-UC-021}). El llamador valida A1
 * ({@link MateriaSinCursoException}) y que el usuario sea profesor activo del tenant
 * <em>antes</em> de persistir.
 */
@Getter
public final class AsignacionMateriaProfesor {

  private final AsignacionMateriaProfesorId id;
  private final UUID tenantId;
  private final MateriaId materiaId;
  private final UUID profesorId;
  private final CursoId cursoId;
  private final ParaleloId paraleloId;

  private AsignacionMateriaProfesor(
      AsignacionMateriaProfesorId id,
      UUID tenantId,
      MateriaId materiaId,
      UUID profesorId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    this.id = id;
    this.tenantId = tenantId;
    this.materiaId = materiaId;
    this.profesorId = profesorId;
    this.cursoId = cursoId;
    this.paraleloId = paraleloId;
  }

  public static AsignacionMateriaProfesor crear(
      AsignacionMateriaProfesorId id,
      UUID tenantId,
      MateriaId materiaId,
      UUID profesorId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    Objects.requireNonNull(id, "id no puede ser nulo");
    Objects.requireNonNull(tenantId, "tenantId no puede ser nulo");
    Objects.requireNonNull(materiaId, "materiaId no puede ser nulo");
    Objects.requireNonNull(profesorId, "profesorId no puede ser nulo");
    Objects.requireNonNull(cursoId, "cursoId no puede ser nulo");
    Objects.requireNonNull(paraleloId, "paraleloId no puede ser nulo");
    return new AsignacionMateriaProfesor(id, tenantId, materiaId, profesorId, cursoId, paraleloId);
  }

  public static AsignacionMateriaProfesor reconstruir(
      AsignacionMateriaProfesorId id,
      UUID tenantId,
      MateriaId materiaId,
      UUID profesorId,
      CursoId cursoId,
      ParaleloId paraleloId) {
    return new AsignacionMateriaProfesor(id, tenantId, materiaId, profesorId, cursoId, paraleloId);
  }
}
