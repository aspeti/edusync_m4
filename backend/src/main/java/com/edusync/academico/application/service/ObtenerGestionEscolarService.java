package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerGestionEscolarUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.academico.domain.GestionEscolarId;
import com.edusync.academico.domain.GestionEscolarNoEncontradaException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerGestionEscolarService implements ObtenerGestionEscolarUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public GestionEscolar obtener(GestionEscolarId id, UUID tenantId) {
    return gestionEscolarRepositoryPort
        .buscarPorIdYTenant(id, tenantId)
        .orElseThrow(GestionEscolarNoEncontradaException::new);
  }
}
