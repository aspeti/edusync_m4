package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de {@link PeriodoEvaluacion}. Filtra explicitamente por {@code tenantId}
 * (mitigacion RLS, {@code DD-UC-015} &sect;2).
 */
public interface PeriodoEvaluacionRepositoryPort {

  PeriodoEvaluacion guardar(PeriodoEvaluacion periodo);

  Optional<PeriodoEvaluacion> buscarPorIdYTenant(PeriodoEvaluacionId id, UUID tenantId);

  List<PeriodoEvaluacion> listarPorGestionYTenant(GestionEscolarId gestionEscolarId, UUID tenantId);

  void eliminar(PeriodoEvaluacionId id, UUID tenantId);
}
