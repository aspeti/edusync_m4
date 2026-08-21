package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.UUID;

/** DTO de salida de las operaciones sobre {@code Inscripcion}. */
public record InscripcionResponse(
    UUID id,
    UUID estudianteId,
    UUID gestionEscolarId,
    UUID cursoId,
    UUID paraleloId,
    LocalDate fechaInscripcion,
    String estado) {}
