package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.SeccionEvaluacion;
import java.util.List;

public interface ReemplazarSeccionesEvaluacionUseCase {

  List<SeccionEvaluacion> reemplazar(ReemplazarSeccionesEvaluacionCommand command);
}
