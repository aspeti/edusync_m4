package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.PeriodoEvaluacion;
import java.util.List;
import java.util.UUID;

public interface ListarPeriodosEvaluacionUseCase {

  List<PeriodoEvaluacion> listar(UUID tenantId, UUID gestionEscolarId);
}
