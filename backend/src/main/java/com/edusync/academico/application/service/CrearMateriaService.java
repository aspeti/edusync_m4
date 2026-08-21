package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CrearMateriaCommand;
import com.edusync.academico.application.port.in.CrearMateriaUseCase;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import com.edusync.academico.domain.MateriaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa el alta de Materias ({@code FSD-UC-018}, paso 1). */
@Service
@RequiredArgsConstructor
public class CrearMateriaService implements CrearMateriaUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;

  @Override
  @Transactional
  public Materia crear(CrearMateriaCommand command) {
    Materia materia = Materia.crear(MateriaId.nueva(), command.tenantId(), command.nombre());
    return materiaRepositoryPort.guardar(materia);
  }
}
