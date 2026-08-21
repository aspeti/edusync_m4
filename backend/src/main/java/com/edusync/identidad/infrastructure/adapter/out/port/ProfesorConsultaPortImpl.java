package com.edusync.identidad.infrastructure.adapter.out.port;

import com.edusync.academico.ProfesorConsultaPort;
import com.edusync.academico.ProfesorResumen;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.UsuarioFiltro;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import com.edusync.shared.PageQuery;
import com.edusync.shared.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Implementacion real de {@code academico.ProfesorConsultaPort} (Open Host Service para
 * {@code academico}, {@code DD-UC-012}/{@code DD-UC-014}). Vive en {@code identidad.infrastructure}
 * — no en {@code academico} — porque quien conoce el {@code Usuario} y sus roles es este modulo.
 * Arista {@code identidad -> academico} ya existente; no genera ciclo.
 *
 * <p>No loguea {@code nombreCompleto}, {@code q} ni otro PII ({@code AGENTS.md} &sect;7).
 */
@Component
@RequiredArgsConstructor
class ProfesorConsultaPortImpl implements ProfesorConsultaPort {

  private final UsuarioRepositoryPort usuarioRepositoryPort;

  @Override
  public boolean esProfesorActivoDelTenant(UUID usuarioId, UUID tenantId) {
    return usuarioRepositoryPort
        .buscarPorId(UsuarioId.de(usuarioId))
        .filter(usuario -> tenantId.equals(usuario.getTenantId()))
        .filter(Usuario::isActivo)
        .filter(usuario -> usuario.tieneRol(Rol.PROFESOR))
        .isPresent();
  }

  @Override
  public List<ProfesorResumen> listarActivosDelTenant(UUID tenantId) {
    return usuarioRepositoryPort.listarPorTenant(tenantId).stream()
        .filter(Usuario::isActivo)
        .filter(usuario -> usuario.tieneRol(Rol.PROFESOR))
        .map(this::aResumen)
        .toList();
  }

  @Override
  public Optional<ProfesorResumen> buscarPorIdYTenant(UUID usuarioId, UUID tenantId) {
    return usuarioRepositoryPort
        .buscarPorId(UsuarioId.de(usuarioId))
        .filter(usuario -> tenantId.equals(usuario.getTenantId()))
        .filter(usuario -> usuario.tieneRol(Rol.PROFESOR))
        .map(this::aResumen);
  }

  @Override
  public PageResult<ProfesorResumen> listarDelTenant(
      UUID tenantId, String q, Boolean activo, PageQuery pageQuery) {
    UsuarioFiltro filtro = new UsuarioFiltro(q, activo, Rol.PROFESOR);
    return usuarioRepositoryPort.listarPorTenant(tenantId, filtro, pageQuery).map(this::aResumen);
  }

  private ProfesorResumen aResumen(Usuario usuario) {
    return new ProfesorResumen(usuario.getId().valor(), usuario.getNombreCompleto(), usuario.isActivo());
  }
}
