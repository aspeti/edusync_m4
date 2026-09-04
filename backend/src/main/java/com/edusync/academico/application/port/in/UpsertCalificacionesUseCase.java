package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.CalificacionEvaluacion;
import java.util.List;

public interface UpsertCalificacionesUseCase {

  List<CalificacionEvaluacion> upsert(UpsertCalificacionesCommand command);
}
