package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarParalelosUseCase;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.Paralelo;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa el listado de Paralelos de un Curso. Valida que el Curso exista y pertenezca al
 * tenant actual antes de listar (404, no 403, mismo criterio que {@code CrearParaleloService}).
 */
@Service
@RequiredArgsConstructor
public class ListarParalelosService implements ListarParalelosUseCase {

  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Paralelo> listar(UUID tenantId, UUID cursoId) {
    CursoId id = CursoId.de(cursoId);
    cursoRepositoryPort.buscarPorIdYTenant(id, tenantId).orElseThrow(CursoNoEncontradoException::new);
    return paraleloRepositoryPort.listarPorCursoYTenant(id, tenantId);
  }
}
