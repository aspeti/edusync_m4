package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;

/** DTO de entrada de {@code PATCH /api/v1/gestiones-escolares/{id}} ({@code DD-UC-019}). */
public record ActualizarGestionEscolarRequest(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
}
