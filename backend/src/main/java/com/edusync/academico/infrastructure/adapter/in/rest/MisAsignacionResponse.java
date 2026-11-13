package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de {@code GET /profesores/me/asignaciones} ({@code DD-UC-033}). */
public record MisAsignacionResponse(
    UUID asignacionId,
    UUID materiaId,
    String materiaNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre,
    String cursoEtiqueta) {}
