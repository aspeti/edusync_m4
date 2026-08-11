package com.edusync.shared.ai.domain;

import com.edusync.shared.exception.DomainException;

/** HTTP 503 cuando {@code edusync.ai.enabled=false}. */
public class AiDeshabilitadoException extends DomainException {

  public AiDeshabilitadoException() {
    super("E_AI_DESHABILITADO", "El consumo de LLM esta deshabilitado en este entorno");
  }
}
