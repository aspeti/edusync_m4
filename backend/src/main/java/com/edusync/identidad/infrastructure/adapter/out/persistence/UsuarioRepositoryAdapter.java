package com.edusync.identidad.infrastructure.adapter.out.persistence;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.UsuarioRepositoryPort;
import com.edusync.identidad.domain.Rol;
import com.edusync.identidad.domain.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: traduce entre {@code identidad.domain.Usuario} y las entidades JPA
 * ({@code UsuarioJpaEntity}/{@code UsuarioRolJpaEntity}). La politica RLS de {@code usuario}
 * (ver {@code V2__identidad_usuario.sql}) ya filtra por tenant a nivel de motor; este
 * adaptador no anade un filtro adicional en {@link #buscarPorEmail(String)} porque es el
 * propio flujo de login (ocurre antes de que exista un tenant activo, ver
 * {@code UsuarioRepositoryPort} Javadoc).
 */
@Component
@RequiredArgsConstructor
class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

  private final UsuarioJpaRepository jpaRepository;

  @Override
  public Optional<Usuario> buscarPorEmail(String email) {
    return jpaRepository.findByEmail(email).map(this::aDominio);
  }

  @Override
  public Optional<Usuario> buscarPorId(UsuarioId id) {
    return jpaRepository.findById(id.valor()).map(this::aDominio);
  }

  @Override
  public boolean existePorEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  @Override
  public long contarTodos() {
    return jpaRepository.count();
  }

  @Override
  public List<Usuario> listarPorTenant(UUID tenantId) {
    return jpaRepository.findByTenantId(tenantId).stream().map(this::aDominio).toList();
  }

  /**
   * Inserta (usuario nuevo) o actualiza (DD-UC-005: PATCH roles/estado, restablecimiento de
   * contrasena) un {@link Usuario}. Para el caso de actualizacion, reutiliza la entidad ya
   * administrada por el contexto de persistencia y muta su coleccion de roles
   * <em>in-place</em> ({@link UsuarioJpaEntity#reemplazarRoles}) en vez de reemplazarla por
   * una entidad nueva: evitar esto causaria una violacion de {@code uq_usuario_rol} cuando
   * el nuevo conjunto de roles conserva un rol ya existente (ver Javadoc de
   * {@code reemplazarRoles}).
   */
  @Override
  public Usuario guardar(Usuario usuario) {
    UsuarioJpaEntity entity = jpaRepository.findById(usuario.getId().valor())
        .orElseGet(() -> new UsuarioJpaEntity(
            usuario.getId().valor(),
            usuario.getTenantId(),
            usuario.getNombreCompleto(),
            usuario.getEmail(),
            usuario.getPasswordHash(),
            usuario.isActivo()));

    entity.actualizarDatos(
        usuario.getNombreCompleto(), usuario.getEmail(), usuario.getPasswordHash(), usuario.isActivo());
    entity.reemplazarRoles(usuario.getRoles().stream().map(Rol::name).collect(Collectors.toSet()));

    UsuarioJpaEntity guardado = jpaRepository.save(entity);
    return aDominio(guardado);
  }

  private Usuario aDominio(UsuarioJpaEntity entity) {
    Set<Rol> roles = entity.getRoles().stream()
        .map(r -> Rol.valueOf(r.getRol()))
        .collect(Collectors.toUnmodifiableSet());
    return Usuario.reconstruir(
        UsuarioId.de(entity.getId()),
        entity.getTenantId(),
        entity.getNombreCompleto(),
        entity.getEmail(),
        entity.getPasswordHash(),
        roles,
        entity.isActivo());
  }
}
