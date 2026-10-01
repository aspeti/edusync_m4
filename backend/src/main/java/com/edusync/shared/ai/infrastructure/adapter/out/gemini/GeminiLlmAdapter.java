package com.edusync.shared.ai.infrastructure.adapter.out.gemini;

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
 * Adaptador {@link LlmPort} hacia Gemini por el endpoint compatible con OpenAI (ADR-0023).
 * Activo cuando {@code edusync.ai.provider=gemini}.
 *
 * <p>La API key sale solo de {@code GEMINI_API_KEY}. MUST NOT loguear la key, el prompt ni la
 * respuesta completa. No enviar notas ni RUDE reales al proveedor.
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "gemini")
public class GeminiLlmAdapter implements LlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(GeminiLlmAdapter.class);

  private final ChatClient chatClient;
  private final AiProperties aiProperties;

  public GeminiLlmAdapter(ChatClient chatClient, AiProperties aiProperties) {
    this.chatClient = chatClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public RespuestaLlm completar(String prompt) {
    String apiKey = aiProperties.getGemini().getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      throw new LlmNoDisponibleException(
          "GEMINI_API_KEY no configurada (definir env var, no commitear el valor)");
    }

    String modelFallback = aiProperties.getGemini().getModel();
    try {
      ChatResponse response = chatClient.prompt().user(prompt).call().chatResponse();
      String texto = textoDe(response);
      if (texto == null || texto.isBlank()) {
        throw new LlmNoDisponibleException("respuesta vacia de Gemini");
      }
      String modeloUsado = modeloDe(response, modelFallback);
      LOG.debug("Gemini OK (modelo={}, chars={})", modeloUsado, texto.length());
      return new RespuestaLlm(texto, modeloUsado);
    } catch (LlmNoDisponibleException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      LOG.warn("Fallo al llamar a Gemini: {}", ex.getClass().getSimpleName());
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
