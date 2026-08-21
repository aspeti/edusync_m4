package com.edusync.academico.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** DTO de entrada de {@code POST /api/v1/inscripciones} ({@code FSD-UC-020}, paso 2). */
public record CrearInscripcionRequest(
    @NotNull UUID estudianteId,
    @NotNull UUID gestionEscolarId,
    @NotNull UUID cursoId,
    @NotNull UUID paraleloId,
    @NotNull LocalDate fechaInscripcion) {}
