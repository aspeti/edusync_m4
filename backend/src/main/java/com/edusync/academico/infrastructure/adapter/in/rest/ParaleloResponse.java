package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de las operaciones de {@code CursoController} sobre {@code Paralelo}. */
public record ParaleloResponse(UUID id, UUID cursoId, String nombre) {
}
