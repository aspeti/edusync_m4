package com.edusync.academico.application.service;

import com.edusync.academico.application.port.in.CursoFiltro;
import com.edusync.academico.application.port.in.ListarCursosUseCase;
import com.edusync.academico.application.port.out.CursoRepositoryPort;
import com.edusync.academico.domain.Curso;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarCursosService implements ListarCursosUseCase {

  private final CursoRepositoryPort cursoRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public PageResult<Curso> listar(UUID tenantId, CursoFiltro filtro, PageQuery pageQuery) {
    return cursoRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery);
  }
}
