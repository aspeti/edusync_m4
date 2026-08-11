package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.application.port.in.ChatConLlmUseCase;
import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatConLlmService implements ChatConLlmUseCase {

  private final LlmPort llmPort;
  private final boolean enabled;

  public ChatConLlmService(
      LlmPort llmPort, @Value("${edusync.ai.enabled:false}") boolean enabled) {
    this.llmPort = llmPort;
    this.enabled = enabled;
  }

  @Override
  public RespuestaLlm chatear(String prompt) {
    if (!enabled) {
      throw new AiDeshabilitadoException();
    }
    return llmPort.completar(prompt);
  }
}
