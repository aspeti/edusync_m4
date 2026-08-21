package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearCursoCommand;
import com.edusync.academico.application.port.in.CrearCursoUseCase;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.academico.domain.CursoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el alta de Cursos ({@code FSD-UC-017}, paso 1). */
@Service
@RequiredArgsConstructor
public class CrearCursoService implements CrearCursoUseCase {

  private final CursoRepositoryPort cursoRepositoryPort;

  @Override
  @Transactional
  public Curso crear(CrearCursoCommand command) {
    Curso curso = Curso.crear(CursoId.nueva(), command.tenantId(), command.nombre());
    return cursoRepositoryPort.guardar(curso);
  }
}
