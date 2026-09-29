package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.ParametroPeriodo;
import java.util.List;
import java.util.UUID;

public interface ListarParametrosPeriodoUseCase {

  List<ParametroPeriodo> listar(UUID tenantId, UUID periodoId);
}
