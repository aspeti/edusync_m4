package com.edusync.shared.ai.domain;

import com.edusync.shared.exception.DomainException;

/** HTTP 502 cuando Ollama no responde. */
public class LlmNoDisponibleException extends DomainException {

  public LlmNoDisponibleException(String detalle) {
    super("E_LLM_NO_DISPONIBLE", "No se pudo obtener respuesta del LLM: " + detalle);
  }

  public LlmNoDisponibleException(String detalle, Throwable cause) {
    super("E_LLM_NO_DISPONIBLE", "No se pudo obtener respuesta del LLM: " + detalle);
    initCause(cause);
  }
}
