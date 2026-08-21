package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando de {@link CrearInscripcionUseCase} ({@code FSD-UC-020}, pasos 2-3).
 * No incluye {@code estado}: la inscripcion nace siempre {@code ACTIVA}.
 *
 * @param tenantId siempre proviene de {@code TenantContextProvider}, nunca del cliente
 */
public record CrearInscripcionCommand(
    UUID tenantId,
    UUID estudianteId,
    UUID gestionEscolarId,
    UUID cursoId,
    UUID paraleloId,
    LocalDate fechaInscripcion) {}
