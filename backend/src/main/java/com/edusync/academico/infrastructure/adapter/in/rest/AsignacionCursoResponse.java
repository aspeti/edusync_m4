package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/**
 * DTO de salida de asignaciones Materia → Curso/Paralelo. Nombres de lectura; {@code null}
 * si la referencia estuviera huerfana.
 */
public record AsignacionCursoResponse(
    UUID id, UUID materiaId, UUID cursoId, String cursoNombre, UUID paraleloId, String paraleloNombre) {}
