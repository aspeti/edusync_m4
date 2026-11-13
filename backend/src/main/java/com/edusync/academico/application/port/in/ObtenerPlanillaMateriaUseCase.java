package com.edusync.academico.application.port.in;

import java.util.UUID;

/** {@code GET /materias/{id}/planilla} ({@code DD-UC-033}). */
public interface ObtenerPlanillaMateriaUseCase {

  PlanillaMateriaVista obtener(
      UUID tenantId,
      UUID materiaId,
      UUID cursoId,
      UUID paraleloId,
      UUID periodoId,
      UUID actorId,
      boolean veTodasLasMaterias,
      boolean puedeEscribir);
}
