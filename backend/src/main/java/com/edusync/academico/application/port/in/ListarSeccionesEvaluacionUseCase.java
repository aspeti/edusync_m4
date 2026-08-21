package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;
import java.util.UUID;

public interface ListarSeccionesEvaluacionUseCase {

  List<SeccionEvaluacion> listar(UUID tenantId, UUID gestionEscolarId);
}
