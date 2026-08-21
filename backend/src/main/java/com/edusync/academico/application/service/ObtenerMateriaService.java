package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerMateriaUseCase;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerMateriaService implements ObtenerMateriaUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public Materia obtener(UUID tenantId, UUID materiaId) {
    return materiaRepositoryPort
        .buscarPorIdYTenant(MateriaId.de(materiaId), tenantId)
        .orElseThrow(MateriaNoEncontradaException::new);
  }
}
