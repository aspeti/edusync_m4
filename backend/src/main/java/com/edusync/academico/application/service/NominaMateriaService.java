package com.edusync.academico.application.service;

import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort.CursoParaleloPar;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoInscritoException;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaSinCursoException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resuelve la nomina de una materia: Inscripcion ACTIVA ∩ asignaciones curso/paralelo
 * ({@code DD-UC-018} §2).
 */
@Component
@RequiredArgsConstructor
class NominaMateriaService {

  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;

  List<Inscripcion> listarNomina(MateriaId materiaId, GestionEscolarId gestionId, UUID tenantId) {
    List<AsignacionMateriaCurso> asignaciones =
        asignacionMateriaCursoRepositoryPort.listarPorMateriaYTenant(materiaId, tenantId);
    if (asignaciones.isEmpty()) {
      throw new MateriaSinCursoException();
    }
    Set<CursoParaleloPar> pares =
        asignaciones.stream()
            .map(a -> new CursoParaleloPar(a.getCursoId(), a.getParaleloId()))
            .collect(Collectors.toSet());
    return inscripcionRepositoryPort.listarActivasPorGestionYParesCursoParalelo(
        gestionId, tenantId, pares);
  }

  void exigirEnNomina(
      MateriaId materiaId,
      GestionEscolarId gestionId,
      UUID tenantId,
      EstudianteId estudianteId) {
    boolean enNomina =
        listarNomina(materiaId, gestionId, tenantId).stream()
            .anyMatch(i -> i.getEstudianteId().equals(estudianteId));
    if (!enNomina) {
      throw new EstudianteNoInscritoException();
    }
  }
}
