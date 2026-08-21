package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Estudiante;
import java.util.UUID;

/**
 * Puerto de entrada: detalle de un {@link Estudiante} ({@code GET /estudiantes/{id}},
 * {@code DD-UC-013} &sect;2).
 */
public interface ObtenerEstudianteUseCase {

  /**
   * @throws com.edusync.academico.domain.EstudianteNoEncontradoException si no existe o es de
   *     otro tenant
   */
  Estudiante obtener(UUID tenantId, UUID estudianteId);
}
