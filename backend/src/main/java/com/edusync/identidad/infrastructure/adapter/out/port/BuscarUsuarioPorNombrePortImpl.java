package com.edusync.identidad.infrastructure.adapter.out.port;

import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.ai.application.port.out.BuscarUsuarioPorNombrePort;
import com.edusync.shared.ai.domain.UsuarioResumen;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementacion de {@code shared.ai.BuscarUsuarioPorNombrePort} (Open Host Service,
 * {@code ADR-0011}). Filtra explicitamente por {@code tenantId} en la capa de aplicacion
 * (mismo patron mitigador de {@code DD-UC-002} &sect;2 / {@code DD-UC-005} &sect;2): nunca
 * expone usuarios de otro tenant ni las filas {@code SYSADMIN}. Traduce {@code Usuario} a
 * {@link UsuarioResumen}, que nunca incluye {@code passwordHash}.
 */
@Component
@RequiredArgsConstructor
class BuscarUsuarioPorNombrePortImpl implements BuscarUsuarioPorNombrePort {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  @Override
  public List<UsuarioResumen> buscarPorNombre(UUID tenantId, String nombreBuscado) {
    String termino = nombreBuscado.toLowerCase();
    return usuarioRepositoryPort.listarPorTenant(tenantId).stream()
        .filter(u -> u.getNombreCompleto().toLowerCase().contains(termino))
        .map(this::aResumen)
        .toList();
  }

  private UsuarioResumen aResumen(Usuario usuario) {
    return new UsuarioResumen(
        usuario.getNombreCompleto(),
        usuario.getEmail(),
        usuario.getRoles().stream().map(Rol::name).collect(Collectors.toUnmodifiableSet()),
        usuario.isActivo());
  }
}
