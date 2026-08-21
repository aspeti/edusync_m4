package com.edusync.academico.application.service;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.academico.application.port.in.ObtenerProfesorUseCase;
import com.edusync.academico.domain.ProfesorNoEncontradoException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerProfesorService implements ObtenerProfesorUseCase {

  private final ProfesorConsultaPort profesorConsultaPort;

  @Override
  @Transactional(readOnly = true)
  public ProfesorResumen obtener(UUID tenantId, UUID profesorId) {
    return profesorConsultaPort
        .buscarPorIdYTenant(profesorId, tenantId)
        .orElseThrow(ProfesorNoEncontradoException::new);
  }
}
