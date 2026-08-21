package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarInscripcionesEstudianteUseCase;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import com.edusync.academico.domain.Inscripcion;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarInscripcionesEstudianteService implements ListarInscripcionesEstudianteUseCase {

  private final EstudianteRepositoryPort estudianteRepositoryPort;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Inscripcion> listar(UUID tenantId, UUID estudianteId) {
    EstudianteId id = EstudianteId.de(estudianteId);
    estudianteRepositoryPort.buscarPorIdYTenant(id, tenantId).orElseThrow(EstudianteNoEncontradoException::new);
    return inscripcionRepositoryPort.listarPorEstudianteYTenant(id, tenantId);
  }
}
