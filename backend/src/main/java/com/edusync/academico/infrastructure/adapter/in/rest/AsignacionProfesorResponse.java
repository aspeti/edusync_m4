package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de asignaciones Materia → Profesor. */
public record AsignacionProfesorResponse(
    UUID id, UUID materiaId, UUID profesorId, UUID cursoId, UUID paraleloId) {}
