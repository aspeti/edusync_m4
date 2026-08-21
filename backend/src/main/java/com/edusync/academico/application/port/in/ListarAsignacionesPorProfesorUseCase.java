package com.edusync.academico.application.port.in;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: asignaciones de un profesor (lista simple, sin paginar). Verifica al
 * profesor del tenant <em>antes</em> de listar ({@code DD-UC-014} &sect;2).
 */
public interface ListarAsignacionesPorProfesorUseCase {

  List<AsignacionProfesorVista> listar(UUID tenantId, UUID profesorId);
}
