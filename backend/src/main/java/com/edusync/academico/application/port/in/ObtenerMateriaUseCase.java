package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Materia;
import java.util.UUID;

/**
 * Puerto de entrada: detalle de una {@link Materia} ({@code GET /materias/{id}},
 * {@code DD-UC-012} &sect;2).
 */
public interface ObtenerMateriaUseCase {

  /**
   * @throws com.edusync.academico.domain.MateriaNoEncontradaException si no existe o es de
   *     otro tenant
   */
  Materia obtener(UUID tenantId, UUID materiaId);
}
