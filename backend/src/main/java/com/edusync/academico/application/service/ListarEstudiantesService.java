package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.EstudianteFiltro;
import com.edusync.academico.application.port.in.ListarEstudiantesUseCase;
import com.edusync.academico.application.port.out.EstudianteRepositoryPort;
import com.edusync.academico.domain.Estudiante;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarEstudiantesService implements ListarEstudiantesUseCase {

  private final EstudianteRepositoryPort estudianteRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<Estudiante> listar(UUID tenantId, EstudianteFiltro filtro, PageQuery pageQuery) {
    return estudianteRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery);
  }
}
