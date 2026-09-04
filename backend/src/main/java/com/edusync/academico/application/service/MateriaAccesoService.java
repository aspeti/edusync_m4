package com.edusync.academico.application.service;

import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import com.edusync.academico.domain.MateriaSinProfesorException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Politica de visibilidad y alta de evaluaciones sobre una {@link Materia}
 * ({@code DD-UC-017} &sect;2). {@code academico} no importa {@code identidad}.
 */
@Component
@RequiredArgsConstructor
class MateriaAccesoService {

  private final MateriaRepositoryPort materiaRepositoryPort;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;

  Materia exigirMateria(UUID tenantId, UUID materiaId) {
    return materiaRepositoryPort
        .buscarPorIdYTenant(MateriaId.de(materiaId), tenantId)
        .orElseThrow(MateriaNoEncontradaException::new);
  }

  void exigirLectura(Materia materia, UUID tenantId, UUID actorId, boolean veTodasLasMaterias) {
    if (veTodasLasMaterias) {
      return;
    }
    if (!estaAsignado(materia.getId(), tenantId, actorId)) {
      throw new MateriaNoEncontradaException();
    }
  }

  /**
   * Alta/edicion/anular: la materia debe tener al menos un profesor ({@code 409}); un
   * {@code PROFESOR} sin override Admin debe figurar en las asignaciones ({@code 404}).
   */
  void exigirEscritura(Materia materia, UUID tenantId, UUID actorId, boolean actorEsAdmin) {
    List<AsignacionMateriaProfesor> asignaciones =
        asignacionMateriaProfesorRepositoryPort.listarPorMateriaYTenant(materia.getId(), tenantId);
    if (asignaciones.isEmpty()) {
      throw new MateriaSinProfesorException();
    }
    if (!actorEsAdmin && asignaciones.stream().noneMatch(a -> a.getProfesorId().equals(actorId))) {
      throw new MateriaNoEncontradaException();
    }
  }

  private boolean estaAsignado(MateriaId materiaId, UUID tenantId, UUID actorId) {
    return asignacionMateriaProfesorRepositoryPort.listarPorMateriaYTenant(materiaId, tenantId).stream()
        .anyMatch(a -> a.getProfesorId().equals(actorId));
  }
}
