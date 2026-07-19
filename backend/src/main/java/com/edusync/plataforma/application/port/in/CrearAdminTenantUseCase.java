package com.edusync.plataforma.application.port.in;

import com.edusync.identidad.UsuarioId;

/**
 * Puerto de entrada: alta del primer {@code ADMIN} de un {@link com.edusync.plataforma.domain.Tenant}
 * ({@code FSD-UC-011}, paso 3). Orquesta la verificacion de que el Tenant existe
 * ({@code plataforma}) y la delegacion a {@code identidad.UsuarioCreacionPort} (API
 * publica de {@code identidad}, {@code ADR-0011}) para crear el {@code Usuario}.
 */
public interface CrearAdminTenantUseCase {

  /**
   * @throws com.edusync.plataforma.domain.TenantNoEncontradoException si el tenant no existe
   * @throws RuntimeException (extiende {@code shared.exception.DomainException}) si el
   *     email ya esta en uso o si el comando delegado viola una invariante de
   *     {@code identidad} — ver {@code identidad.UsuarioCreacionPort#crear}
   */
  UsuarioId crearAdmin(CrearAdminTenantCommand command);
}
