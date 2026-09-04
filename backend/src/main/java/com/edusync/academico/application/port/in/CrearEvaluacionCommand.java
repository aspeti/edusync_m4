package com.edusync.academico.application.port.in;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando de alta de {@link com.edusync.academico.domain.Evaluacion}. {@code tenantId} y
 * {@code actorId} nunca provienen del cliente. {@code puntajeMaximo} no viaja aqui: el
 * servicio lo copia de {@code seccion.nota}.
 *
 * @param actorEsAdmin {@code true} si el JWT tiene rol {@code ADMIN} (override operativo)
 */
public record CrearEvaluacionCommand(
    UUID tenantId,
    UUID actorId,
    boolean actorEsAdmin,
    String nombre,
    UUID materiaId,
    UUID periodoEvaluacionId,
    UUID seccionEvaluacionId,
    LocalDate fecha,
    String descripcion) {}
