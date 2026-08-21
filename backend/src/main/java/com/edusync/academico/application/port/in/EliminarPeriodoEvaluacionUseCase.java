package com.edusync.academico.application.port.in;

import java.util.UUID;

public interface EliminarPeriodoEvaluacionUseCase {

  void eliminar(UUID tenantId, UUID periodoId);
}
