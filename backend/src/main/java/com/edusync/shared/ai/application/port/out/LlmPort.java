package com.edusync.shared.ai.application.port.out;

import com.edusync.shared.ai.domain.RespuestaLlm;

public interface LlmPort {
  RespuestaLlm completar(String prompt);
}
