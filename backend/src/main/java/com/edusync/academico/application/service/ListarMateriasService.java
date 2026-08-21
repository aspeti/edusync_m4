package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.ListarMateriasUseCase;
import com.edusync.academico.application.port.in.MateriaFiltro;
import com.edusync.academico.application.port.out.MateriaRepositoryPort;
import com.edusync.academico.domain.Materia;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarMateriasService implements ListarMateriasUseCase {

  private final MateriaRepositoryPort materiaRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<Materia> listar(UUID tenantId, MateriaFiltro filtro, PageQuery pageQuery) {
    return materiaRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery);
  }
}
