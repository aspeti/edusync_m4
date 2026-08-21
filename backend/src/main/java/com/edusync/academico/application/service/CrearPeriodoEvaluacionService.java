package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearPeriodoEvaluacionUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.PeriodoEvaluacion;
import com.edusync.academico.domain.PeriodoEvaluacionId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrearPeriodoEvaluacionService implements CrearPeriodoEvaluacionUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;

  @Override
  @Transactional
  public PeriodoEvaluacion crear(CrearPeriodoEvaluacionCommand command) {
    GestionEscolarId gestionId = GestionEscolarId.de(command.gestionEscolarId());
    gestionEscolarRepositoryPort
        .buscarPorIdYTenant(gestionId, command.tenantId())
        .orElseThrow(GestionEscolarNoEncontradaException::new);

    List<PeriodoEvaluacion> existentes =
        periodoEvaluacionRepositoryPort.listarPorGestionYTenant(gestionId, command.tenantId());
    PeriodoEvaluacionPolitica.exigirMutables(existentes);

    int orden = existentes.size() + 1;
    PeriodoEvaluacion periodo = PeriodoEvaluacion.crear(
        PeriodoEvaluacionId.nueva(),
        command.tenantId(),
        gestionId,
        command.nombre(),
        command.fechaInicio(),
        command.fechaFin(),
        orden);
    PeriodoEvaluacionPolitica.exigirSinSolape(existentes, periodo);
    return periodoEvaluacionRepositoryPort.guardar(periodo);
  }
}
