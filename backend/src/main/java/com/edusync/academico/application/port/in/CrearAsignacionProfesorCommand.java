package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Comando de {@link CrearAsignacionProfesorUseCase} ({@code FSD-UC-018}, paso 3).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearAsignacionProfesorCommand(
    UUID tenantId, UUID materiaId, UUID profesorId, UUID cursoId, UUID paraleloId) {}
