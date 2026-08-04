package com.edusync.identidad.application.service;

import com.edusync.identidad.application.port.in.ListarUsuariosUseCase;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Usuario;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarUsuariosService implements ListarUsuariosUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  @Override
  @Transactional(readOnly = true)
  public List<Usuario> listar(UUID tenantId) {
    return usuarioRepositoryPort.listarPorTenant(tenantId);
  }
}
