package com.edusync.shared.ai.application.port.out;

import com.edusync.shared.ai.domain.LlamadaHerramienta;

/**
 * Ejecuta una LlamadaHerramienta contra el backend real. Debe
 * propagar el JWT del usuario que pregunto al agente (ADR-0018:
 * "Identidad de ejecucion"), nunca una cuenta tecnica fija. Nunca
 * lanza: en cualquier fallo (red, HTTP no-2xx, timeout) devuelve un
 * JSON de error como String, igual que EjecutorTools.ejecutar en la
 * referencia Python.
 */
public interface EjecutorHerramientaPort {
    String ejecutar(LlamadaHerramienta llamada, String jwtUsuario);
}
