package com.edusync.academico.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** DTO de entrada de {@code POST /api/v1/materias/{id}/asignaciones-curso}. */
public record CrearAsignacionCursoRequest(@NotNull UUID cursoId, @NotNull UUID paraleloId) {}
