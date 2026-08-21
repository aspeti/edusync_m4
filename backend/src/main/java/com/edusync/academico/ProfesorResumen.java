package com.edusync.academico;

import java.util.UUID;

/**
 * Vista minima de un profesor para el catalogo de {@code FSD-UC-018} ({@code DD-UC-012}
 * &sect;2). No es un Aggregate: solo el recorte que {@link ProfesorConsultaPort} expone
 * hacia {@code academico} sin filtrar PII extra.
 */
public record ProfesorResumen(UUID id, String nombreCompleto) {}
