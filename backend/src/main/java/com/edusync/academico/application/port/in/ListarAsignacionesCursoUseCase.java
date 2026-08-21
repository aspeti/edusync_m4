package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.AsignacionMateriaCurso;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: listado simple (sin paginar) de asignaciones curso de una Materia. */
public interface ListarAsignacionesCursoUseCase {

  List<AsignacionMateriaCurso> listar(UUID tenantId, UUID materiaId);
}
