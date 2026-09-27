package com.edusync.academico.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de salida de inscripciones. Los nombres son etiquetas de lectura (no un grafo de
 * agregados); pueden ser {@code null} si la referencia estuviera huerfana.
 */
public record InscripcionResponse(
    UUID id,
    UUID estudianteId,
    UUID gestionEscolarId,
    String gestionNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre,
    LocalDate fechaInscripcion,
    String estado) {}
