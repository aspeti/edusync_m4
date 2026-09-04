package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ObtenerMateriaVisibleUseCase;
import com.edusync.academico.domain.Materia;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ObtenerMateriaVisibleService implements ObtenerMateriaVisibleUseCase {

  private final MateriaAccesoService materiaAccesoService;

  @Override
  @Transactional(readOnly = true)
  public Materia obtener(UUID tenantId, UUID materiaId, UUID actorId, boolean veTodasLasMaterias) {
    Materia materia = materiaAccesoService.exigirMateria(tenantId, materiaId);
    materiaAccesoService.exigirLectura(materia, tenantId, actorId, veTodasLasMaterias);
    return materia;
  }
}
