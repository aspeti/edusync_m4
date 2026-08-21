package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarAsignacionesProfesorUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaProfesorRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaProfesor;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarAsignacionesProfesorService implements ListarAsignacionesProfesorUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;
  private final AsignacionMateriaProfesorRepositoryPort asignacionMateriaProfesorRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<AsignacionMateriaProfesor> listar(UUID tenantId, UUID materiaId) {
    MateriaId id = MateriaId.de(materiaId);
    materiaRepositoryPort.buscarPorIdYTenant(id, tenantId).orElseThrow(MateriaNoEncontradaException::new);
    return asignacionMateriaProfesorRepositoryPort.listarPorMateriaYTenant(id, tenantId);
  }
}
