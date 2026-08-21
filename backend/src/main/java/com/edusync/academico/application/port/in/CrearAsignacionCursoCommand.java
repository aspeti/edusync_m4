package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Comando de {@link CrearAsignacionCursoUseCase} ({@code FSD-UC-018}, paso 2).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearAsignacionCursoCommand(UUID tenantId, UUID materiaId, UUID cursoId, UUID paraleloId) {}
