package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.ListarProfesoresDisponiblesUseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarProfesoresDisponiblesService implements ListarProfesoresDisponiblesUseCase {

  private final ProfesorConsultaPort profesorConsultaPort;

  @Override
  @Transactional(readOnly = true)
  public List<ProfesorResumen> listar(UUID tenantId) {
    return profesorConsultaPort.listarActivosDelTenant(tenantId);
  }
}
