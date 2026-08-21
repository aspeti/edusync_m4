package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearParaleloCommand;
import com.edusync.academico.application.port.in.CrearParaleloUseCase;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa el alta de Paralelos ({@code FSD-UC-017}, paso 2). Valida que el Curso padre
 * exista y pertenezca al tenant actual ANTES de crear el Paralelo ({@code DD-UC-010} &sect;2).
 */
@Service
@RequiredArgsConstructor
public class CrearParaleloService implements CrearParaleloUseCase {

  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;

  @Override
  @Transactional
  public Paralelo crear(CrearParaleloCommand command) {
    CursoId cursoId = CursoId.de(command.cursoId());
    cursoRepositoryPort
        .buscarPorIdYTenant(cursoId, command.tenantId())
        .orElseThrow(CursoNoEncontradoException::new);

    Paralelo paralelo = Paralelo.crear(ParaleloId.nueva(), command.tenantId(), cursoId, command.nombre());
    return paraleloRepositoryPort.guardar(paralelo);
  }
}
