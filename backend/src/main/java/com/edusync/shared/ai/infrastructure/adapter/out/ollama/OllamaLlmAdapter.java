package com.edusync.shared.ai.infrastructure.adapter.out.ollama;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adaptador {@link LlmPort} → Ollama via Spring AI {@link ChatClient}.
 * Activo cuando {@code edusync.ai.provider=ollama} (default).
 * MUST NOT loguear prompt ni respuesta completa (AGENTS.md &sect;7).
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaLlmAdapter implements LlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(OllamaLlmAdapter.class);

  private final ChatClient chatClient;
  private final AiProperties aiProperties;

  public OllamaLlmAdapter(ChatClient chatClient, AiProperties aiProperties) {
    this.chatClient = chatClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public RespuestaLlm completar(String prompt) {
    String modelFallback = aiProperties.getOllama().getModel();
    try {
      ChatResponse response = chatClient.prompt().user(prompt).call().chatResponse();
      String texto = textoDe(response);
      if (texto == null || texto.isBlank()) {
        throw new LlmNoDisponibleException("respuesta vacia del proveedor");
      }
      String modeloUsado = modeloDe(response, modelFallback);
      LOG.debug("Ollama OK (modelo={}, chars={})", modeloUsado, texto.length());
      return new RespuestaLlm(texto, modeloUsado);
    } catch (LlmNoDisponibleException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      LOG.warn("Fallo al llamar a Ollama: {}", ex.getClass().getSimpleName());
      throw new LlmNoDisponibleException("error de red", ex);
    }
  }

  private static String textoDe(ChatResponse response) {
    if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
      return null;
    }
    return response.getResult().getOutput().getText();
  }

  private static String modeloDe(ChatResponse response, String fallback) {
    if (response != null
        && response.getMetadata() != null
        && response.getMetadata().getModel() != null
        && !response.getMetadata().getModel().isBlank()) {
      return response.getMetadata().getModel();
    }
    return fallback;
  }
}
