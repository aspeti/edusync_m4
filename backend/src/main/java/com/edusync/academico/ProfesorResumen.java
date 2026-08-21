package com.edusync.academico;

import java.util.UUID;

/**
 * Vista minima de un profesor ({@code DD-UC-012}/{@code DD-UC-014}). No es un Aggregate: solo
 * el recorte que {@link ProfesorConsultaPort} expone hacia {@code academico} sin filtrar PII
 * extra. {@code activo} se anade en {@code DD-UC-014} para el detalle de la consola; el DTO
 * REST de {@code GET /materias/profesores-disponibles} sigue exponiendo solo
 * {@code {id, nombreCompleto}}.
 */
public record ProfesorResumen(UUID id, String nombreCompleto, boolean activo) {}
