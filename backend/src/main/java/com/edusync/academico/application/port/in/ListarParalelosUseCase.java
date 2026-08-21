package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Paralelo;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada: listado simple (sin paginar) de los Paralelos de un Curso
 * ({@code DD-UC-010} &sect;2: cardinalidad acotada, sin caso de uso real para paginar hoy).
 */
public interface ListarParalelosUseCase {

  /**
   * @throws com.edusync.academico.domain.CursoNoEncontradoException si {@code cursoId} no
   *     existe o pertenece a un tenant distinto de {@code tenantId}
   */
  List<Paralelo> listar(UUID tenantId, UUID cursoId);
}
