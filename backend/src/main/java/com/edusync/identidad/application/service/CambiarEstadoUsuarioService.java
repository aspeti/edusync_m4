package com.edusync.identidad.application.service;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.CambiarEstadoUsuarioUseCase;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Activa/desactiva un usuario de tenant (DD-UC-005 &sect;2), con el mismo filtro explicito
 * de tenant que {@link ActualizarRolesUsuarioService}. */
@Service
@RequiredArgsConstructor
public class CambiarEstadoUsuarioService implements CambiarEstadoUsuarioUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  @Override
  @Transactional
  public Usuario cambiarEstado(UsuarioId usuarioId, UUID tenantIdActor, boolean activo) {
    Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId)
        .filter(u -> Objects.equals(u.getTenantId(), tenantIdActor))
        .orElseThrow(UsuarioNoEncontradoException::new);

    Usuario actualizado = activo ? usuario.activar() : usuario.desactivar();
    return usuarioRepositoryPort.guardar(actualizado);
  }
}
