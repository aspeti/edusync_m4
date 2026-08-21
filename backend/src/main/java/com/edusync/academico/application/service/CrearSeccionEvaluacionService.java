package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearSeccionEvaluacionCommand;
import com.edusync.academico.application.port.in.CrearSeccionEvaluacionUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.application.port.out.PeriodoEvaluacionRepositoryPort;
import com.edusync.academico.application.port.out.SeccionEvaluacionRepositoryPort;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import com.edusync.academico.domain.SeccionEvaluacion;
import com.edusync.academico.domain.SeccionEvaluacionId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrearSeccionEvaluacionService implements CrearSeccionEvaluacionUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional
  public SeccionEvaluacion crear(CrearSeccionEvaluacionCommand command) {
    GestionEscolarId gestionId = GestionEscolarId.de(command.gestionEscolarId());
    gestionEscolarRepositoryPort
        .buscarPorIdYTenant(gestionId, command.tenantId())
        .orElseThrow(GestionEscolarNoEncontradaException::new);

    SeccionEvaluacionPolitica.exigirMutables(
        periodoEvaluacionRepositoryPort.listarPorGestionYTenant(gestionId, command.tenantId()));

    List<SeccionEvaluacion> existentes =
        seccionEvaluacionRepositoryPort.listarPorGestionYTenant(gestionId, command.tenantId());
    SeccionEvaluacion seccion = SeccionEvaluacion.crear(
        SeccionEvaluacionId.nueva(),
        command.tenantId(),
        gestionId,
        command.nombre(),
        command.orden(),
        command.nota());

    List<SeccionEvaluacion> resultantes = new ArrayList<>(existentes);
    resultantes.add(seccion);
    SeccionEvaluacionPolitica.exigirSumaCien(resultantes);
    return seccionEvaluacionRepositoryPort.guardar(seccion);
  }
}
