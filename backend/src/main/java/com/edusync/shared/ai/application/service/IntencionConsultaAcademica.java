package com.edusync.shared.ai.application.service;

/** Intención extraída de lenguaje natural (sin UUIDs). */
public record IntencionConsultaAcademica(
        Operacion operacion,
        String estudiante,
        String materia,
        String periodo,
        String cursoParalelo,
        String profesor) {

    public enum Operacion {
        NOTAS,
        PROMEDIO,
        REPROBADOS,
        TOP,
        NOMINA,
        MATERIAS_ESTUDIANTE,
        BUSCAR_PROFESOR,
        BUSCAR_ESTUDIANTE
    }

    public boolean esBusquedaSimple() {
        return operacion == Operacion.BUSCAR_PROFESOR || operacion == Operacion.BUSCAR_ESTUDIANTE;
    }
}
