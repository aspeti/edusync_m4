package com.edusync.shared.ai.domain;

/**
 * Un turno de la conversacion interna del agente. No es el mensaje
 * REST expuesto al usuario: es el historial que se le pasa de vuelta
 * al modelo en cada turno del bucle ReAct (ver
 * EjecutarConsultaAgenteService).
 */
public record MensajeAgente(Rol rol, String contenido, LlamadaHerramienta llamada) {

    public enum Rol { USUARIO, ASISTENTE, HERRAMIENTA }

    public static MensajeAgente pregunta(String texto) {
        return new MensajeAgente(Rol.USUARIO, texto, null);
    }

    public static MensajeAgente respuestaFinal(String texto) {
        return new MensajeAgente(Rol.ASISTENTE, texto, null);
    }

    public static MensajeAgente decisionHerramienta(LlamadaHerramienta llamada) {
        return new MensajeAgente(Rol.ASISTENTE, null, llamada);
    }

    public static MensajeAgente observacion(String jsonResultado) {
        return new MensajeAgente(Rol.HERRAMIENTA, jsonResultado, null);
    }
}
