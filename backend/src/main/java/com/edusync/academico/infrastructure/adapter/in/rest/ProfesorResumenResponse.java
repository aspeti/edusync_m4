package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida del catalogo de profesores disponibles ({@code DD-UC-012}). */
public record ProfesorResumenResponse(UUID id, String nombreCompleto) {}
