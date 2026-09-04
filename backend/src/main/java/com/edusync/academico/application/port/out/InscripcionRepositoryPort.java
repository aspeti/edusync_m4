package com.edusync.academico.application.port.out;

import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.ParaleloId;
import java.util.List;
import java.util.Set;
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

  /**
   * Nomina de una materia ({@code DD-UC-018}): inscripciones {@code ACTIVA} de la gestion
   * cuyo {@code (cursoId, paraleloId)} esta en {@code paresCursoParalelo}.
   */
  List<Inscripcion> listarActivasPorGestionYParesCursoParalelo(
      GestionEscolarId gestionEscolarId,
      UUID tenantId,
      Set<CursoParaleloPar> paresCursoParalelo);

  /** Par curso/paralelo para filtrar la nomina sin acoplar a JPA. */
  record CursoParaleloPar(CursoId cursoId, ParaleloId paraleloId) {}
}
