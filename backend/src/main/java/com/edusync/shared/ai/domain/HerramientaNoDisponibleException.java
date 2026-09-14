package com.edusync.shared.ai.domain;

/**
 * El modelo eligio una herramienta que no existe en el catalogo
 * descubierto. Nunca se ejecuta nada en este caso: failure mode
 * E_HERRAMIENTA_DESCONOCIDA (ADR-0018, PR-IMPL-023).
 */
public class HerramientaNoDisponibleException extends RuntimeException {
    public HerramientaNoDisponibleException(String nombreHerramienta) {
        super("Herramienta no disponible en el catalogo: " + nombreHerramienta);
    }
}
