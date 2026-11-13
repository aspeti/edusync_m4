package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Fila de {@code GET /profesores/me/asignaciones} ({@code DD-UC-033} / {@code FSD-UC-022}).
 */
public record MisAsignacionVista(
    UUID asignacionId,
    UUID materiaId,
    String materiaNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre,
    String cursoEtiqueta) {}
