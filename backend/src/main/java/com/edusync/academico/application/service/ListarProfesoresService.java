package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.ListarProfesoresUseCase;
import com.edusync.academico.application.port.in.ProfesorFiltro;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarProfesoresService implements ListarProfesoresUseCase {

  private final ProfesorConsultaPort profesorConsultaPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<ProfesorResumen> listar(UUID tenantId, ProfesorFiltro filtro, PageQuery pageQuery) {
    ProfesorFiltro efectivo = filtro == null ? ProfesorFiltro.VACIO : filtro;
    return profesorConsultaPort.listarDelTenant(tenantId, efectivo.q(), efectivo.activo(), pageQuery);
  }
}
