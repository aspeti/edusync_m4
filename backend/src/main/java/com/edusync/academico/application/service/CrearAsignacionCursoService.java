package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearAsignacionCursoCommand;
import com.edusync.academico.application.port.in.CrearAsignacionCursoUseCase;
import com.edusync.academico.application.port.out.AsignacionMateriaCursoRepositoryPort;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.AsignacionMateriaCurso;
import com.edusync.academico.domain.AsignacionMateriaCursoId;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.MateriaId;
import com.edusync.academico.domain.MateriaNoEncontradaException;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParaleloNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa la asignacion Materia → Curso/Paralelo ({@code FSD-UC-018}, paso 2). Valida
 * Materia, Curso y Paralelo (pertenencia paralelo→curso) ANTES de persistir
 * ({@code DD-UC-012} &sect;2).
 */
@Service
@RequiredArgsConstructor
public class CrearAsignacionCursoService implements CrearAsignacionCursoUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final AsignacionMateriaCursoRepositoryPort asignacionMateriaCursoRepositoryPort;

  @Override
  @Transactional
  public AsignacionMateriaCurso crear(CrearAsignacionCursoCommand command) {
    MateriaId materiaId = MateriaId.de(command.materiaId());
    CursoId cursoId = CursoId.de(command.cursoId());
    ParaleloId paraleloId = ParaleloId.de(command.paraleloId());

    materiaRepositoryPort
        .buscarPorIdYTenant(materiaId, command.tenantId())
        .orElseThrow(MateriaNoEncontradaException::new);
    cursoRepositoryPort
        .buscarPorIdYTenant(cursoId, command.tenantId())
        .orElseThrow(CursoNoEncontradoException::new);
    Paralelo paralelo =
        paraleloRepositoryPort
            .buscarPorIdYTenant(paraleloId, command.tenantId())
            .orElseThrow(ParaleloNoEncontradoException::new);
    if (!paralelo.getCursoId().equals(cursoId)) {
      throw new ParaleloNoEncontradoException();
    }

    AsignacionMateriaCurso asignacion =
        AsignacionMateriaCurso.crear(
            AsignacionMateriaCursoId.nueva(), command.tenantId(), materiaId, cursoId, paraleloId);
    return asignacionMateriaCursoRepositoryPort.guardar(asignacion);
  }
}
