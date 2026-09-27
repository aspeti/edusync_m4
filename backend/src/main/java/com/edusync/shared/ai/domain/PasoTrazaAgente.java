package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Un paso de traza del asistente (DD-UC-025). Tablas por nombre, sin filas
 * ni PII (NFR-007).
 */
public record PasoTrazaAgente(
        int paso,
        String toolId,
        List<String> tablasFuente,
        boolean exito
) {
    public PasoTrazaAgente {
        tablasFuente = List.copyOf(tablasFuente);
    }
}
