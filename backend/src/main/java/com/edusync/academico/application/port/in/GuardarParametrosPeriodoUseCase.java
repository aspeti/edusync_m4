package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.ParametroPeriodo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GuardarParametrosPeriodoUseCase {

  List<ParametroPeriodo> guardar(UUID tenantId, UUID periodoId, List<Item> parametros);

  record Item(UUID seccionEvaluacionId, BigDecimal rangoMin, BigDecimal rangoMax, String reglaCombinacion) {}
}
