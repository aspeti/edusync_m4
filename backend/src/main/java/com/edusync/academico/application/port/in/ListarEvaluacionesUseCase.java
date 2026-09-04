package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Evaluacion;
import java.util.List;
import java.util.UUID;

/**
 * Lista evaluaciones de una materia (opcionalmente filtradas por periodo). {@code veTodasLasMaterias}
 * es {@code true} para {@code ADMIN} y {@code SECRETARIA}; un {@code PROFESOR} solo ve las suyas.
 */
public interface ListarEvaluacionesUseCase {

  List<Evaluacion> listar(
      UUID tenantId, UUID materiaId, UUID periodoId, UUID actorId, boolean veTodasLasMaterias);
}
