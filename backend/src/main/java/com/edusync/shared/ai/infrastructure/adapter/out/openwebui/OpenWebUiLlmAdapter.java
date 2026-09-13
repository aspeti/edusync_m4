package com.edusync.shared.ai.infrastructure.adapter.out.openwebui;

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
 * Adaptador {@link LlmPort} → Open WebUI via Spring AI (API OpenAI-compatible).
 * Activo cuando {@code edusync.ai.provider=open-webui}.
 *
 * <p>La API key sale solo de {@code OPEN_WEBUI_API_KEY} / {@code edusync.ai.open-webui.api-key}.
 * MUST NOT loguear la key, el prompt ni la respuesta completa.
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "open-webui")
public class OpenWebUiLlmAdapter implements LlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(OpenWebUiLlmAdapter.class);

  private final ChatClient chatClient;
  private final AiProperties aiProperties;

  public OpenWebUiLlmAdapter(ChatClient chatClient, AiProperties aiProperties) {
    this.chatClient = chatClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public RespuestaLlm completar(String prompt) {
    String apiKey = aiProperties.getOpenWebui().getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      throw new LlmNoDisponibleException(
          "OPEN_WEBUI_API_KEY no configurada (definir env var, no commitear el valor)");
    }

    String modelFallback = aiProperties.getOpenWebui().getModel();
    try {
      ChatResponse response = chatClient.prompt().user(prompt).call().chatResponse();
      String texto = textoDe(response);
      if (texto == null || texto.isBlank()) {
        throw new LlmNoDisponibleException("respuesta vacia de Open WebUI");
      }
      String modeloUsado = modeloDe(response, modelFallback);
      LOG.debug("Open WebUI OK (modelo={}, chars={})", modeloUsado, texto.length());
      return new RespuestaLlm(texto, modeloUsado);
    } catch (LlmNoDisponibleException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      LOG.warn("Fallo al llamar a Open WebUI: {}", ex.getClass().getSimpleName());
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
