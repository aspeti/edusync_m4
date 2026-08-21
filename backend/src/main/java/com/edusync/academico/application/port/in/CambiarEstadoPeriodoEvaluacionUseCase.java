package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacion;
import java.util.UUID;

public interface CambiarEstadoPeriodoEvaluacionUseCase {

  PeriodoEvaluacion cambiarEstado(UUID tenantId, UUID periodoId, EstadoPeriodoEvaluacion nuevoEstado);
}
