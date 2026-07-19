package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.TenantId;

/**
 * Comando de {@link CrearAdminTenantUseCase} ({@code FSD-UC-011}, paso 3 — llamada
 * REST separada de {@link RegistrarTenantCommand}, {@code DD-UC-003} &sect;2/&sect;3).
 *
 * @param password contrasena en texto plano; se hashea dentro de {@code identidad}
 *     (nunca se guarda ni se loguea en claro, {@code AGENTS.md} &sect;7)
 */
public record CrearAdminTenantCommand(TenantId tenantId, String nombreCompleto, String email, String password) {
}
