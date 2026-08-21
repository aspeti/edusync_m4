package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;

/** DTO de salida de las operaciones de {@code MateriaController} sobre {@code Materia}. */
public record MateriaResponse(UUID id, String nombre) {}
