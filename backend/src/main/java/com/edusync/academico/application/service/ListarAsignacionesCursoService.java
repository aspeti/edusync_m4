package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarAsignacionesCursoUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarAsignacionesCursoService implements ListarAsignacionesCursoUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;
  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<AsignacionMateriaCurso> listar(UUID tenantId, UUID materiaId) {
    MateriaId id = MateriaId.de(materiaId);
    materiaRepositoryPort.buscarPorIdYTenant(id, tenantId).orElseThrow(MateriaNoEncontradaException::new);
    return asignacionMateriaCursoRepositoryPort.listarPorMateriaYTenant(id, tenantId);
  }
}
