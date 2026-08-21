package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ReemplazarSeccionesEvaluacionCommand;
import com.edusync.academico.application.port.in.ReemplazarSeccionesEvaluacionUseCase;
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
public class ReemplazarSeccionesEvaluacionService implements ReemplazarSeccionesEvaluacionUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;
  private final PeriodoEvaluacionRepositoryPort periodoEvaluacionRepositoryPort;
  private final SeccionEvaluacionRepositoryPort seccionEvaluacionRepositoryPort;

  @Override
  @Transactional
  public List<SeccionEvaluacion> reemplazar(ReemplazarSeccionesEvaluacionCommand command) {
    GestionEscolarId gestionId = GestionEscolarId.de(command.gestionEscolarId());
    gestionEscolarRepositoryPort
        .buscarPorIdYTenant(gestionId, command.tenantId())
        .orElseThrow(GestionEscolarNoEncontradaException::new);

    SeccionEvaluacionPolitica.exigirMutables(
        periodoEvaluacionRepositoryPort.listarPorGestionYTenant(gestionId, command.tenantId()));

    List<ReemplazarSeccionesEvaluacionCommand.Item> items =
        command.secciones() == null ? List.of() : command.secciones();
    List<SeccionEvaluacion> nuevas = new ArrayList<>();
    int orden = 1;
    for (ReemplazarSeccionesEvaluacionCommand.Item item : items) {
      nuevas.add(SeccionEvaluacion.crear(
          SeccionEvaluacionId.nueva(),
          command.tenantId(),
          gestionId,
          item.nombre(),
          orden++,
          item.nota()));
    }
    SeccionEvaluacionPolitica.exigirSumaCien(nuevas);
    return seccionEvaluacionRepositoryPort.reemplazarPlantilla(gestionId, command.tenantId(), nuevas);
  }
}
