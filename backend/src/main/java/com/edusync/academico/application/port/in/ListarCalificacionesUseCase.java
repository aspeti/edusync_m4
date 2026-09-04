package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.Estudiante;
import java.util.List;
import java.util.UUID;

/**
 * Lista la nomina de la evaluacion con calificaciones existentes ({@code DD-UC-018}).
 */
public interface ListarCalificacionesUseCase {

  Resultado listar(
      UUID tenantId, UUID evaluacionId, UUID actorId, boolean veTodasLasMaterias);

  record Fila(Estudiante estudiante, CalificacionEvaluacion calificacion) {}

  record Resultado(List<Fila> filas) {}
}
