package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de {@code ProfesorController} ({@code DD-UC-014}). Distinto de {@link ProfesorResumenResponse}. */
public record ProfesorResponse(UUID id, String nombreCompleto, boolean activo) {}
