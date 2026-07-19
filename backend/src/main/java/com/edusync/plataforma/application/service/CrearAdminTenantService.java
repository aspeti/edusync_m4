package com.edusync.plataforma.application.service;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioCreacionPort;
import com.edusync.identidad.UsuarioId;
import com.edusync.plataforma.application.port.in.CrearAdminTenantCommand;
import com.edusync.plataforma.application.port.in.CrearAdminTenantUseCase;
import com.edusync.plataforma.application.port.out.TenantRepositoryPort;
import com.edusync.plataforma.domain.Tenant;
import com.edusync.plataforma.domain.TenantNoEncontradoException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta el alta del primer {@code ADMIN} de un Tenant ({@code FSD-UC-011}, paso 3):
 * verifica que el Tenant exista (dentro de {@code plataforma}) y delega la creacion del
 * {@code Usuario} a {@code identidad.UsuarioCreacionPort} (API publica de
 * {@code identidad}, {@code ADR-0011}) — unica via permitida, sin importar
 * {@code identidad.domain}/{@code identidad.application} (internos a ese modulo).
 */
@Service
@RequiredArgsConstructor
public class CrearAdminTenantService implements CrearAdminTenantUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UsuarioCreacionPort usuarioCreacionPort;

  @Override
  @Transactional
  public UsuarioId crearAdmin(CrearAdminTenantCommand command) {
    Tenant tenant = tenantRepositoryPort.buscarPorId(command.tenantId())
        .orElseThrow(() -> new TenantNoEncontradoException(command.tenantId().valor()));

    return usuarioCreacionPort.crear(new CrearUsuarioCommand(
        tenant.getId().valor(), command.nombreCompleto(), command.email(), command.password(), Set.of("ADMIN")));
  }
}
