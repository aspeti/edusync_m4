package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearGestionEscolarCommand;
import com.edusync.academico.application.port.in.CrearGestionEscolarUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el alta de Gestiones Escolares ({@code FSD-UC-012}, pasos 1-2). */
@Service
@RequiredArgsConstructor
public class CrearGestionEscolarService implements CrearGestionEscolarUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional
  public GestionEscolar crear(CrearGestionEscolarCommand command) {
    GestionEscolar gestionEscolar = GestionEscolar.crear(
        GestionEscolarId.nueva(), command.tenantId(), command.nombre(), command.fechaInicio(), command.fechaFin());
    return gestionEscolarRepositoryPort.guardar(gestionEscolar);
  }
}
