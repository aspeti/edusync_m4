package com.edusync.academico.application.port.in;

import java.util.List;
import java.util.UUID;

/** Asignaciones del actor autenticado ({@code GET /profesores/me/asignaciones}). */
public interface ListarMisAsignacionesUseCase {

  List<MisAsignacionVista> listar(UUID tenantId, UUID profesorId);
}
