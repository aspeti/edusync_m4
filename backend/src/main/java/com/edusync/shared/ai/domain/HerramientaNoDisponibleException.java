package com.edusync.shared.ai.domain;

import com.edusync.shared.exception.DomainException;

/**
 * El modelo eligio una herramienta que no existe en el catalogo
 * descubierto. Nunca se ejecuta nada en este caso: failure mode
 * E_HERRAMIENTA_NO_DISPONIBLE (ADR-0018, PR-IMPL-023).
 */
public class HerramientaNoDisponibleException extends DomainException {
    public HerramientaNoDisponibleException(String nombreHerramienta) {
        super("E_HERRAMIENTA_NO_DISPONIBLE", "Herramienta no disponible en el catalogo: " + nombreHerramienta);
    }
}
