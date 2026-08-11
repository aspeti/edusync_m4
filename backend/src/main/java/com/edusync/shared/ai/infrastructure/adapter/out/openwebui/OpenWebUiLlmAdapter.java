package com.edusync.shared.ai.infrastructure.adapter.out.openwebui;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador {@link LlmPort} → Open WebUI (API compatible OpenAI:
 * {@code POST /api/chat/completions}). Activo cuando
 * {@code edusync.ai.provider=open-webui}.
 *
 * <p>La API key sale solo de {@code OPEN_WEBUI_API_KEY} / {@code edusync.ai.open-webui.api-key}.
 * MUST NOT loguear la key, el prompt ni la respuesta completa.
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "open-webui")
public class OpenWebUiLlmAdapter implements LlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(OpenWebUiLlmAdapter.class);

  private final RestClient openWebUiRestClient;
  private final AiProperties aiProperties;

  public OpenWebUiLlmAdapter(
      @Qualifier("openWebUiRestClient") RestClient openWebUiRestClient,
      AiProperties aiProperties) {
    this.openWebUiRestClient = openWebUiRestClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public RespuestaLlm completar(String prompt) {
    String apiKey = aiProperties.getOpenWebui().getApiKey();
    if (apiKey == null || apiKey.isBlank()) {
      throw new LlmNoDisponibleException(
          "OPEN_WEBUI_API_KEY no configurada (definir env var, no commitear el valor)");
    }

    String model = aiProperties.getOpenWebui().getModel();
    try {
      ChatCompletionsResponse body =
          openWebUiRestClient
              .post()
              .uri("/api/chat/completions")
              .contentType(MediaType.APPLICATION_JSON)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
              .body(
                  new ChatCompletionsRequest(
                      model, List.of(new ChatMessage("user", prompt)), false))
              .retrieve()
              .body(ChatCompletionsResponse.class);

      if (body == null
          || body.choices() == null
          || body.choices().isEmpty()
          || body.choices().getFirst().message() == null
          || body.choices().getFirst().message().content() == null
          || body.choices().getFirst().message().content().isBlank()) {
        throw new LlmNoDisponibleException("respuesta vacia de Open WebUI");
      }

      String texto = body.choices().getFirst().message().content();
      String modeloUsado = body.model() != null && !body.model().isBlank() ? body.model() : model;
      LOG.debug("Open WebUI OK (modelo={}, chars={})", modeloUsado, texto.length());
      return new RespuestaLlm(texto, modeloUsado);
    } catch (LlmNoDisponibleException ex) {
      throw ex;
    } catch (RestClientException ex) {
      LOG.warn("Fallo al llamar a Open WebUI: {}", ex.getClass().getSimpleName());
      throw new LlmNoDisponibleException(
          ex.getMessage() != null ? ex.getMessage() : "error de red", ex);
    }
  }

  record ChatMessage(String role, String content) {}

  record ChatCompletionsRequest(String model, List<ChatMessage> messages, boolean stream) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record ChatCompletionsResponse(String model, List<Choice> choices) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record Choice(ChatMessage message) {}
}
