package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.Materia;
import java.util.UUID;

/**
 * Detalle de materia con visibilidad de {@code PROFESOR}: si {@code veTodasLasMaterias} es
 * {@code false}, el actor debe estar asignado o se responde 404.
 */
public interface ObtenerMateriaVisibleUseCase {

  Materia obtener(UUID tenantId, UUID materiaId, UUID actorId, boolean veTodasLasMaterias);
}
