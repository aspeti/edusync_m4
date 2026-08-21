package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia de {@link SeccionEvaluacion}. Filtra explicitamente por {@code tenantId}
 * (mitigacion RLS, {@code DD-UC-016} &sect;2).
 */
public interface SeccionEvaluacionRepositoryPort {

  SeccionEvaluacion guardar(SeccionEvaluacion seccion);

  Optional<SeccionEvaluacion> buscarPorIdYTenant(SeccionEvaluacionId id, UUID tenantId);

  List<SeccionEvaluacion> listarPorGestionYTenant(GestionEscolarId gestionEscolarId, UUID tenantId);

  /**
   * Reemplazo atomico de la plantilla: borra las secciones actuales de la gestion
   * y persiste {@code nuevas} (ya validadas en aplicacion).
   */
  List<SeccionEvaluacion> reemplazarPlantilla(
      GestionEscolarId gestionEscolarId, UUID tenantId, List<SeccionEvaluacion> nuevas);
}
