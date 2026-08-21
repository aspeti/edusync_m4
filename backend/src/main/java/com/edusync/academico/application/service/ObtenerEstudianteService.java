package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerEstudianteUseCase;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.Estudiante;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerEstudianteService implements ObtenerEstudianteUseCase {

  private final EstudianteRepositoryPort estudianteRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public Estudiante obtener(UUID tenantId, UUID estudianteId) {
    return estudianteRepositoryPort
        .buscarPorIdYTenant(EstudianteId.de(estudianteId), tenantId)
        .orElseThrow(EstudianteNoEncontradoException::new);
  }
}
