package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de asignaciones Materia → Curso/Paralelo. */
public record AsignacionCursoResponse(UUID id, UUID materiaId, UUID cursoId, UUID paraleloId) {}
