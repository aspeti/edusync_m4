package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ActualizarEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarEvaluacionUseCase;
import com.edusync.academico.application.port.out.EvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.EstadoPeriodoEvaluacion;
import com.edusync.academico.domain.Evaluacion;
import com.edusync.academico.domain.EvaluacionId;
import com.edusync.academico.domain.EvaluacionNoEncontradaException;
import com.edusync.academico.domain.PeriodoNoAbiertoException;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActualizarEvaluacionService implements ActualizarEvaluacionUseCase {

  private final EvaluacionRepositoryPort evaluacionRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final MateriaAccesoService materiaAccesoService;

  @Override
  @Transactional
  public Evaluacion actualizar(ActualizarEvaluacionCommand command) {
    Evaluacion evaluacion = evaluacionRepositoryPort
        .buscarPorIdYTenant(EvaluacionId.de(command.evaluacionId()), command.tenantId())
        .orElseThrow(EvaluacionNoEncontradaException::new);
    materiaAccesoService.exigirEscritura(
        materiaAccesoService.exigirMateria(command.tenantId(), evaluacion.getMateriaId().valor()),
        command.tenantId(),
        command.actorId(),
        command.actorEsAdmin());
    exigirPeriodoAbierto(command.tenantId(), evaluacion);

    String nombre = command.nombre() != null ? command.nombre() : evaluacion.getNombre();
    var fecha = command.fecha() != null ? command.fecha() : evaluacion.getFecha();
    String descripcion = command.descripcion() != null ? command.descripcion() : evaluacion.getDescripcion();
    evaluacion.actualizarDatos(nombre, fecha, descripcion);
    return evaluacionRepositoryPort.guardar(evaluacion);
  }

  private void exigirPeriodoAbierto(java.util.UUID tenantId, Evaluacion evaluacion) {
    var periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(evaluacion.getPeriodoEvaluacionId(), tenantId)
        .orElseThrow(PeriodoNoEncontradoException::new);
    if (periodo.getEstado() != EstadoPeriodoEvaluacion.ABIERTO) {
      throw new PeriodoNoAbiertoException();
    }
  }
}
