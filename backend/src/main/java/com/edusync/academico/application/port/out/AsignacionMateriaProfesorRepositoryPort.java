package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.MateriaId;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link AsignacionMateriaProfesor}. Filtra
 * explicitamente por {@code tenantId}.
 */
public interface AsignacionMateriaProfesorRepositoryPort {

  AsignacionMateriaProfesor guardar(AsignacionMateriaProfesor asignacion);

  List<AsignacionMateriaProfesor> listarPorMateriaYTenant(MateriaId materiaId, UUID tenantId);

  /** Consulta inversa por profesor ({@code DD-UC-014}): lista simple, sin paginar. */
  List<AsignacionMateriaProfesor> listarPorProfesorYTenant(UUID profesorId, UUID tenantId);
}
