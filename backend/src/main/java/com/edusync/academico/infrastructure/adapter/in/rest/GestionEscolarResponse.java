package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.UUID;

/** DTO de salida de las operaciones de {@code GestionEscolarController}. */
public record GestionEscolarResponse(
    UUID id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
}
