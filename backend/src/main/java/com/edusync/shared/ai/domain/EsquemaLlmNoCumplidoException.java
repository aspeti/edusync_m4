package com.edusync.shared.ai.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza cuando el LLM no produce una salida que cumpla el esquema esperado tras el numero
 * maximo de reintentos ({@link com.edusync.shared.ai.application.service.LlmStructuredExtractor}).
 * Falla controlada: HTTP 502, igual que {@link LlmNoDisponibleException} (el proveedor
 * "respondio" pero no de forma utilizable).
 */
public class EsquemaLlmNoCumplidoException extends DomainException {

  public EsquemaLlmNoCumplidoException(String esquema, int intentos) {
    super(
        "E_ESQUEMA_LLM_NO_CUMPLIDO",
        "El LLM no produjo una salida valida para " + esquema + " tras " + intentos + " intentos");
  }
}
