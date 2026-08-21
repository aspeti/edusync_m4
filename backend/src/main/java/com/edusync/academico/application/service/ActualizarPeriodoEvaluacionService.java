package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ActualizarPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.in.ActualizarPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import com.edusync.academico.domain.PeriodoNoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActualizarPeriodoEvaluacionService implements ActualizarPeriodoEvaluacionUseCase {

  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional
  public PeriodoEvaluacion actualizar(ActualizarPeriodoEvaluacionCommand command) {
    PeriodoEvaluacion periodo = periodoEvaluacionRepositoryPort
        .buscarPorIdYTenant(PeriodoEvaluacionId.de(command.periodoId()), command.tenantId())
        .orElseThrow(PeriodoNoEncontradoException::new);

    List<PeriodoEvaluacion> hermanos = periodoEvaluacionRepositoryPort.listarPorGestionYTenant(
        periodo.getGestionEscolarId(), command.tenantId());
    PeriodoEvaluacionPolitica.exigirMutables(hermanos);

    String nombre = command.nombre() != null ? command.nombre() : periodo.getNombre();
    LocalDate fechaInicio = command.fechaInicio() != null ? command.fechaInicio() : periodo.getFechaInicio();
    LocalDate fechaFin = command.fechaFin() != null ? command.fechaFin() : periodo.getFechaFin();
    periodo.actualizar(nombre, fechaInicio, fechaFin);
    PeriodoEvaluacionPolitica.exigirSinSolape(hermanos, periodo);
    return periodoEvaluacionRepositoryPort.guardar(periodo);
  }
}
