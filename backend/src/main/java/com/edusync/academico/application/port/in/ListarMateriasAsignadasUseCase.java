package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Materia;
import java.util.List;
import java.util.UUID;

/** Materias asignadas al {@code profesorId} del JWT ({@code GET /materias/mias}). */
public interface ListarMateriasAsignadasUseCase {

  List<Materia> listar(UUID tenantId, UUID profesorId);
}
