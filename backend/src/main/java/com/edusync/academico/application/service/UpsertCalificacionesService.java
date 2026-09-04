package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.UpsertCalificacionesCommand;
import com.edusync.academico.application.port.in.UpsertCalificacionesUseCase;
import com.edusync.academico.application.port.out.CalificacionEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.CalificacionEvaluacion;
import com.edusync.academico.domain.CalificacionEvaluacionId;
import com.edusync.academico.domain.EstadoEvaluacion;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.EstudianteId;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.EvaluacionNoActivaException;
import com.edusync.academico.domain.EvaluacionNoEncontradaException;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoNoAbiertoException;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpsertCalificacionesService implements UpsertCalificacionesUseCase {

  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final CalificacionEvaluacionRepositoryPort calificacionEvaluacionRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;
  private final NominaMateriaService nominaMateriaService;

  @Override
  @Transactional
  public List<CalificacionEvaluacion> upsert(UpsertCalificacionesCommand command) {
    Evaluacion evaluacion =
        evaluacionRepositoryPort
            .buscarPorIdYTenant(EvaluacionId.de(command.evaluacionId()), command.tenantId())
            .orElseThrow(EvaluacionNoEncontradaException::new);

    Materia materia =
        materiaAccesoService.exigirMateria(command.tenantId(), evaluacion.getMateriaId().valor());
    materiaAccesoService.exigirEscritura(
        materia, command.tenantId(), command.actorId(), command.actorEsAdmin());

    PeriodoEvaluacion periodo =
        periodoEvaluacionRepositoryPort
            .buscarPorIdYTenant(evaluacion.getPeriodoEvaluacionId(), command.tenantId())
            .orElseThrow(PeriodoNoEncontradoException::new);
    if (periodo.getEstado() != EstadoPeriodoEvaluacion.ABIERTO) {
      throw new PeriodoNoAbiertoException();
    }
    if (evaluacion.getEstado() != EstadoEvaluacion.ACTIVA) {
      throw new EvaluacionNoActivaException();
    }

    List<CalificacionEvaluacion> resultado = new ArrayList<>();
    for (UpsertCalificacionesCommand.Item item : command.items()) {
      EstudianteId estudianteId = EstudianteId.de(item.estudianteId());
      nominaMateriaService.exigirEnNomina(
          materia.getId(), periodo.getGestionEscolarId(), command.tenantId(), estudianteId);

      Optional<CalificacionEvaluacion> existente =
          calificacionEvaluacionRepositoryPort.buscarPorEvaluacionEstudianteYTenant(
              evaluacion.getId(), estudianteId, command.tenantId());
      if (existente.isPresent()) {
        CalificacionEvaluacion actual = existente.get();
        actual.actualizarValor(item.valor(), evaluacion.getPuntajeMaximo());
        resultado.add(calificacionEvaluacionRepositoryPort.guardar(actual));
      } else {
        CalificacionEvaluacion nueva =
            CalificacionEvaluacion.crear(
                CalificacionEvaluacionId.nueva(),
                command.tenantId(),
                evaluacion.getId(),
                estudianteId,
                item.valor(),
                evaluacion.getPuntajeMaximo());
        resultado.add(calificacionEvaluacionRepositoryPort.guardar(nueva));
      }
    }
    return List.copyOf(resultado);
  }
}
