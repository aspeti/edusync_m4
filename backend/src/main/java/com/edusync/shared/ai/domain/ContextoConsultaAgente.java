package com.edusync.shared.ai.domain;

/**
 * Slots ya resueltos de la conversación del asistente (ADR-0020). IDs en texto UUID;
 * el cliente los reenvía; el backend los revalida por tenant.
 */
public record ContextoConsultaAgente(
        String estudianteId,
        String estudianteEtiqueta,
        String cursoId,
        String paraleloId,
        String paraleloEtiqueta,
        String materiaId,
        String materiaEtiqueta,
        String periodoId,
        String periodoEtiqueta,
        String ultimaOperacion) {

    public static ContextoConsultaAgente vacio() {
        return new ContextoConsultaAgente(null, null, null, null, null, null, null, null, null, null);
    }

    public ContextoConsultaAgente conOperacion(String operacion) {
        return new ContextoConsultaAgente(
                estudianteId, estudianteEtiqueta, cursoId, paraleloId, paraleloEtiqueta,
                materiaId, materiaEtiqueta, periodoId, periodoEtiqueta, operacion);
    }
}
