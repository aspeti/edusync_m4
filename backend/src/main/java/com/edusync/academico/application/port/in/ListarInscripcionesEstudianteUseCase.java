package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Inscripcion;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: historial de inscripciones de un Estudiante (lista simple, sin paginar).
 */
public interface ListarInscripcionesEstudianteUseCase {

  List<Inscripcion> listar(UUID tenantId, UUID estudianteId);
}
