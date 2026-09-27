package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/**
 * DTO de salida de asignaciones Materia → Profesor. Nombres de curso/paralelo de lectura;
 * {@code null} si la referencia estuviera huerfana.
 */
public record AsignacionProfesorResponse(
    UUID id,
    UUID materiaId,
    UUID profesorId,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre) {}
