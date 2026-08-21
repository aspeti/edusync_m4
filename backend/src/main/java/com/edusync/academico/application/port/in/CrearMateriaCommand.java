package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Comando de {@link CrearMateriaUseCase} ({@code FSD-UC-018}, paso 1).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearMateriaCommand(UUID tenantId, String nombre) {}
