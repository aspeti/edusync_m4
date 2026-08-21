package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearInscripcionCommand;
import com.edusync.academico.application.port.in.CrearInscripcionUseCase;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.InscripcionRepositoryPort;
import com.edusync.academico.application.port.out.ParaleloRepositoryPort;
import com.edusync.academico.domain.CursoId;
import com.edusync.academico.domain.CursoNoEncontradoException;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.EstudianteNoEncontradoException;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.Inscripcion;
import com.edusync.academico.domain.InscripcionDuplicadaException;
import com.edusync.academico.domain.InscripcionId;
import com.edusync.academico.domain.Paralelo;
import com.edusync.academico.domain.ParaleloId;
import com.edusync.academico.domain.ParaleloNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa el alta de Inscripciones ({@code FSD-UC-020}, pasos 2-3). Valida Estudiante,
 * GestionEscolar, Curso y Paralelo (pertenencia paralelo→curso) ANTES de persistir
 * ({@code DD-UC-013} &sect;2). A1: unicidad {@code (estudiante, gestionEscolar)} del tenant.
 */
@Service
@RequiredArgsConstructor
public class CrearInscripcionService implements CrearInscripcionUseCase {

  private final EstudianteRepositoryPort estudianteRepositoryPort;
  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final CursoRepositoryPort cursoRepositoryPort;
  private final ParaleloRepositoryPort paraleloRepositoryPort;
  private final InscripcionRepositoryPort inscripcionRepositoryPort;

  @Override
  @Transactional
  public Inscripcion crear(CrearInscripcionCommand command) {
    EstudianteId estudianteId = EstudianteId.de(command.estudianteId());
    GestionEscolarId gestionEscolarId = GestionEscolarId.de(command.gestionEscolarId());
    CursoId cursoId = CursoId.de(command.cursoId());
    ParaleloId paraleloId = ParaleloId.de(command.paraleloId());

    estudianteRepositoryPort
        .buscarPorIdYTenant(estudianteId, command.tenantId())
        .orElseThrow(EstudianteNoEncontradoException::new);
    gestionEscolarRepositoryPort
        .buscarPorIdYTenant(gestionEscolarId, command.tenantId())
        .orElseThrow(GestionEscolarNoEncontradaException::new);
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

    if (inscripcionRepositoryPort.existePorEstudianteGestionYTenant(
        estudianteId, gestionEscolarId, command.tenantId())) {
      throw new InscripcionDuplicadaException();
    }

    Inscripcion inscripcion =
        Inscripcion.crear(
            InscripcionId.nueva(),
            command.tenantId(),
            estudianteId,
            gestionEscolarId,
            cursoId,
            paraleloId,
            command.fechaInscripcion());
    return inscripcionRepositoryPort.guardar(inscripcion);
  }
}
