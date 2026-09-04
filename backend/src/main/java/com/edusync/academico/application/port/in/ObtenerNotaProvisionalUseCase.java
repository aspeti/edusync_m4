package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.CalculoNotas;
import java.util.UUID;

public interface ObtenerNotaProvisionalUseCase {

  CalculoNotas.NotaProvisional obtener(
      UUID tenantId,
      UUID materiaId,
      UUID estudianteId,
      UUID periodoId,
      UUID actorId,
      boolean veTodasLasMaterias);
}
