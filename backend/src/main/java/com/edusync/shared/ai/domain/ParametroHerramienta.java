package com.edusync.shared.ai.domain;

/**
 * Un parametro declarado por una herramienta descubierta en el catalogo OpenAPI.
 * Ver ADR-0018 y DD-UC-023 seccion 2.
 */
public record ParametroHerramienta(
        String nombre,
        String tipo,
        boolean requerido,
        Ubicacion ubicacion
) {
    public enum Ubicacion { PATH, QUERY, BODY }
}
