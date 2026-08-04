package com.edusync.identidad.application.service;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.ActualizarRolesUsuarioUseCase;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.identidad.domain.UsuarioNoEncontradoException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modifica el conjunto de roles vigentes de un usuario de tenant (DD-UC-005 &sect;2). El
 * filtro explicito por {@code tenantIdActor} es la misma mitigacion documentada en
 * {@code DD-UC-002} &sect;2: la politica RLS de {@code usuario} por si sola no basta.
 */
@Service
@RequiredArgsConstructor
public class ActualizarRolesUsuarioService implements ActualizarRolesUsuarioUseCase {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  @Override
  @Transactional
  public Usuario actualizarRoles(UsuarioId usuarioId, UUID tenantIdActor, Set<String> nombresRoles) {
    Usuario usuario = usuarioRepositoryPort.buscarPorId(usuarioId)
        .filter(u -> Objects.equals(u.getTenantId(), tenantIdActor))
        .orElseThrow(UsuarioNoEncontradoException::new);

    Set<Rol> roles = nombresRoles.stream()
        .map(nombre -> Rol.valueOf(nombre.trim().toUpperCase()))
        .collect(Collectors.toUnmodifiableSet());

    Usuario actualizado = usuario.conRoles(roles);
    return usuarioRepositoryPort.guardar(actualizado);
  }
}
