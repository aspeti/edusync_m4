package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearEvaluacionUseCase;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoAbiertoException;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import com.edusync.academico.domain.SeccionNoEncontradaException;
import com.edusync.academico.domain.SeccionNoPerteneceAGestionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrearEvaluacionService implements CrearEvaluacionUseCase {

  private final MateriaAccesoService materiaAccesoService;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;
  private final EvaluacionRepositoryPort evaluacionRepositoryPort;

  @Override
  @Transactional
  public Evaluacion crear(CrearEvaluacionCommand command) {
    Materia materia = materiaAccesoService.exigirMateria(command.tenantId(), command.materiaId());
    materiaAccesoService.exigirEscritura(materia, command.tenantId(), command.actorId(), command.actorEsAdmin());

    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(command.periodoEvaluacionId()), command.tenantId())
        .orElseThrow(PeriodoNoEncontradoException::new);
    if (periodo.getEstado() != EstadoPeriodoEvaluacion.ABIERTO) {
      throw new PeriodoNoAbiertoException();
    }

    SeccionEvaluacion seccion = seccionEvaluacionRepositoryPort
        .buscarPorIdYTenant(SeccionEvaluacionId.de(command.seccionEvaluacionId()), command.tenantId())
        .orElseThrow(SeccionNoEncontradaException::new);
    if (!periodo.getGestionEscolarId().equals(seccion.getGestionEscolarId())) {
      throw new SeccionNoPerteneceAGestionException();
    }

    Evaluacion evaluacion = Evaluacion.crear(
        EvaluacionId.nueva(),
        command.tenantId(),
        materia.getId(),
        periodo.getId(),
        seccion.getId(),
        command.nombre(),
        command.fecha(),
        seccion.getNota(),
        command.descripcion());
    return evaluacionRepositoryPort.guardar(evaluacion);
  }
}
