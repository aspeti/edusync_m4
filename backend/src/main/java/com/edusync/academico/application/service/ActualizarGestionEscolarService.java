package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ActualizarGestionEscolarCommand;
import com.edusync.academico.application.port.in.ActualizarGestionEscolarUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@code DD-UC-019}: sin restriccion de estado (endpoint exclusivamente {@code ADMIN}). */
@Service
@RequiredArgsConstructor
public class ActualizarGestionEscolarService implements ActualizarGestionEscolarUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional
  public GestionEscolar actualizar(ActualizarGestionEscolarCommand command) {
    GestionEscolar gestionEscolar = gestionEscolarRepositoryPort
        .buscarPorIdYTenant(GestionEscolarId.de(command.gestionId()), command.tenantId())
        .orElseThrow(GestionEscolarNoEncontradaException::new);

    gestionEscolar.actualizarDatos(command.nombre(), command.fechaInicio(), command.fechaFin());
    return gestionEscolarRepositoryPort.guardar(gestionEscolar);
  }
}
