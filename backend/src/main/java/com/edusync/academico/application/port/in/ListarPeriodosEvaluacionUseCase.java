package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.PeriodoEvaluacion;
import java.util.List;
import java.util.UUID;

/**
 * Lista los periodos de una Gestion Escolar por id. Exclusivo {@code ADMIN}
 * ({@code DD-UC-021}); el resto de roles resuelve la gestion actual con
 * {@code GET /gestiones-escolares/activa/periodos} (mismo puerto, id resuelto server-side).
 */
public interface ListarPeriodosEvaluacionUseCase {

  List<PeriodoEvaluacion> listar(UUID tenantId, UUID gestionEscolarId);
}
