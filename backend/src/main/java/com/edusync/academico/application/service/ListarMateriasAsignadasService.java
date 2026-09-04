package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarMateriasAsignadasUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.Materia;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarMateriasAsignadasService implements ListarMateriasAsignadasUseCase {

  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;
  private final MateriaRepositoryPort materiaRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Materia> listar(UUID tenantId, UUID profesorId) {
    LinkedHashSet<UUID> materiaIds = new LinkedHashSet<>();
    for (AsignacionMateriaProfesor asignacion :
        asignacionMateriaProfesorRepositoryPort.listarPorProfesorYTenant(profesorId, tenantId)) {
      materiaIds.add(asignacion.getMateriaId().valor());
    }
    List<Materia> materias = new ArrayList<>();
    for (UUID materiaId : materiaIds) {
      materiaRepositoryPort.buscarPorIdYTenant(
              com.edusync.academico.domain.MateriaId.de(materiaId), tenantId)
          .ifPresent(materias::add);
    }
    return materias;
  }
}
