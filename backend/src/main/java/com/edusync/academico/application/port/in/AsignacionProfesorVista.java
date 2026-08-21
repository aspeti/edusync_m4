package com.edusync.academico.application.port.in;

import java.util.UUID;

/**
 * Vista enriquecida de una {@code AsignacionMateriaProfesor} para
 * {@code GET /profesores/{id}/asignaciones} ({@code DD-UC-014} &sect;2). Los nombres pueden ser
 * {@code null} si la referencia estuviera huerfana (caso defensivo: no hay {@code DELETE} de
 * Materia/Curso en slices previos).
 */
public record AsignacionProfesorVista(
    UUID id,
    UUID materiaId,
    String materiaNombre,
    UUID cursoId,
    String cursoNombre,
    UUID paraleloId,
    String paraleloNombre) {}
