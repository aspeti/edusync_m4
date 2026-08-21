package com.edusync.academico.application.port.in;

import com.edusync.academico.domain.EstadoEstudiante;
import java.util.Map;
import java.util.UUID;

/**
 * Comando de {@link CrearEstudianteUseCase} ({@code FSD-UC-020}, paso 1).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 * @param estado nulo → {@code ACTIVO}
 */
public record CrearEstudianteCommand(
    UUID tenantId,
    String rude,
    String nombreCompleto,
    EstadoEstudiante estado,
    Map<String, String> datosPersonales) {}
