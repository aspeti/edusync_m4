package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.GestionEscolarFiltro;
import com.edusync.academico.application.port.in.ListarGestionesEscolaresUseCase;
import com.edusync.academico.application.port.out.GestionEscolarRepositoryPort;
import com.edusync.academico.domain.GestionEscolar;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarGestionesEscolaresService implements ListarGestionesEscolaresUseCase {

  private final GestionEscolarRepositoryPort gestionEscolarRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<GestionEscolar> listar(UUID tenantId, GestionEscolarFiltro filtro, PageQuery pageQuery) {
    return gestionEscolarRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery);
  }
}
