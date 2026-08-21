package com.edusync.academico.application.port.in;

import com.edusync.academico.ProfesorResumen;
import java.util.UUID;

/**
 * Puerto de entrada: detalle de un profesor ({@code GET /profesores/{id}}, {@code DD-UC-014}
 * &sect;2). Incluye inactivos con rol {@code PROFESOR}.
 */
public interface ObtenerProfesorUseCase {

  /**
   * @throws com.edusync.academico.domain.ProfesorNoEncontradoException si no existe, es de otro
   *     tenant, o el usuario no tiene rol {@code PROFESOR}
   */
  ProfesorResumen obtener(UUID tenantId, UUID profesorId);
}
