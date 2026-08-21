package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.AsignacionMateriaProfesor;
import java.util.List;
import java.util.UUID;

/** Puerto de entrada: listado simple (sin paginar) de asignaciones profesor de una Materia. */
public interface ListarAsignacionesProfesorUseCase {

  List<AsignacionMateriaProfesor> listar(UUID tenantId, UUID materiaId);
}
