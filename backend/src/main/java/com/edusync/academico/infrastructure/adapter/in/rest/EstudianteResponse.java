package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.Map;
import java.util.UUID;

/** DTO de salida de las operaciones de {@code EstudianteController} sobre {@code Estudiante}. */
public record EstudianteResponse(
    UUID id, String rude, String nombreCompleto, String estado, Map<String, String> datosPersonales) {}
