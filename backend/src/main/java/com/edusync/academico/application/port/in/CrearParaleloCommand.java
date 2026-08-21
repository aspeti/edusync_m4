package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Comando de {@link CrearParaleloUseCase} ({@code FSD-UC-017}, paso 2).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 * @param cursoId curso padre; debe existir y pertenecer a {@code tenantId}
 *     ({@code CursoNoEncontradoException} si no)
 */
public record CrearParaleloCommand(UUID tenantId, UUID cursoId, String nombre) {
}
