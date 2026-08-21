package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Comando de {@link CrearCursoUseCase} ({@code FSD-UC-017}, paso 1).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearCursoCommand(UUID tenantId, String nombre) {
}
