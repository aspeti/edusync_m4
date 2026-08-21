package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Inscripcion;
import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida: persistencia de {@link Inscripcion}. Filtra explicitamente por
 * {@code tenantId}.
 */
public interface InscripcionRepositoryPort {

  Inscripcion guardar(Inscripcion inscripcion);

  List<Inscripcion> listarPorEstudianteYTenant(EstudianteId estudianteId, UUID tenantId);

  boolean existePorEstudianteGestionYTenant(
      EstudianteId estudianteId, GestionEscolarId gestionEscolarId, UUID tenantId);
}
