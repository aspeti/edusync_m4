package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.UUID;

/**
 * Lista las secciones de una Gestion Escolar por id. Exclusivo {@code ADMIN}
 * ({@code DD-UC-021}); el resto de roles resuelve la gestion actual con
 * {@code GET /gestiones-escolares/activa/secciones} (mismo puerto, id resuelto server-side).
 */
public interface ListarSeccionesEvaluacionUseCase {

  List<SeccionEvaluacion> listar(UUID tenantId, UUID gestionEscolarId);
}
