package com.edusync.shared.ai.application.port.in;

import com.edusync.shared.ai.domain.RespuestaLlm;

public interface ChatConLlmUseCase {
  RespuestaLlm chatear(String prompt);
}
