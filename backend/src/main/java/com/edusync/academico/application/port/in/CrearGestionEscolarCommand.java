package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando de {@link CrearGestionEscolarUseCase} ({@code FSD-UC-012}, pasos 1-2). Uso
 * interno del modulo (nada fuera de {@code academico} crea Gestiones Escolares hoy).
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearGestionEscolarCommand(UUID tenantId, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
