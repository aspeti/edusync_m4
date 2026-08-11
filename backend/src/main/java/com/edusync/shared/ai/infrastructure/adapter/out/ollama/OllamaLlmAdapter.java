package com.edusync.shared.ai.infrastructure.adapter.out.ollama;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Adaptador {@link LlmPort} → Ollama {@code POST /api/generate} (sin streaming).
 * Activo cuando {@code edusync.ai.provider=ollama} (default).
 * MUST NOT loguear prompt ni respuesta completa (AGENTS.md &sect;7).
 */
@Component
@ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaLlmAdapter implements LlmPort {

  private static final Logger LOG = LoggerFactory.getLogger(OllamaLlmAdapter.class);

  private final RestClient ollamaRestClient;
  private final AiProperties aiProperties;

  public OllamaLlmAdapter(
      @Qualifier("ollamaRestClient") RestClient ollamaRestClient, AiProperties aiProperties) {
    this.ollamaRestClient = ollamaRestClient;
    this.aiProperties = aiProperties;
  }

  @Override
  public RespuestaLlm completar(String prompt) {
    String model = aiProperties.getOllama().getModel();
    try {
      OllamaGenerateResponse body =
          ollamaRestClient
              .post()
              .uri("/api/generate")
              .contentType(MediaType.APPLICATION_JSON)
              .body(new OllamaGenerateRequest(model, prompt, false))
              .retrieve()
              .body(OllamaGenerateResponse.class);

      if (body == null || body.response() == null || body.response().isBlank()) {
        throw new LlmNoDisponibleException("respuesta vacia del proveedor");
      }

      String modeloUsado = body.model() != null && !body.model().isBlank() ? body.model() : model;
      LOG.debug("Ollama OK (modelo={}, chars={})", modeloUsado, body.response().length());
      return new RespuestaLlm(body.response(), modeloUsado);
    } catch (LlmNoDisponibleException ex) {
      throw ex;
    } catch (RestClientException ex) {
      LOG.warn("Fallo al llamar a Ollama: {}", ex.getClass().getSimpleName());
      throw new LlmNoDisponibleException(
          ex.getMessage() != null ? ex.getMessage() : "error de red", ex);
    }
  }

  record OllamaGenerateRequest(String model, String prompt, boolean stream) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  record OllamaGenerateResponse(
      String model, @JsonProperty("response") String response, boolean done) {}
}
