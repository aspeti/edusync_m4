package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO enriquecido de {@code GET /profesores/{id}/asignaciones} ({@code DD-UC-014}). */
public record AsignacionProfesorVistaResponse(
    UUID id,
    UUID materiaId,
    String materiaNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre) {}
