package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando de {@code PATCH /evaluaciones/{id}}. Campos nulos conservan el valor actual
 * (excepto que {@code nombre} y {@code fecha} se reenvian juntos desde la UI).
 */
public record ActualizarEvaluacionCommand(
    UUID tenantId,
    UUID actorId,
    boolean actorEsAdmin,
    UUID evaluacionId,
    String nombre,
    LocalDate fecha,
    String descripcion) {}
