package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerGestionEscolarActivaUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@code DD-UC-021}: resuelve "la gestion actual" sin listar ni elegir. */
@Service
@RequiredArgsConstructor
public class ObtenerGestionEscolarActivaService implements ObtenerGestionEscolarActivaUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public GestionEscolar obtener(UUID tenantId) {
    return gestionEscolarRepositoryPort.buscarActivaPorTenant(tenantId)
        .orElseThrow(GestionEscolarNoEncontradaException::new);
  }
}
