package com.edusync.shared.ai.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ChatClient del camino AGENTE (tool calling multipaso, ADR-0018). Separada de
 * {@link AiConfig} (chat v0, ADR-0017) para no tocar el contrato de {@code LlmPort}.
 *
 * <p>Spring AI 2.0: {@code OllamaChatOptions} (no {@code OllamaOptions}) y
 * {@code OllamaChatModel.builder().options(...)} (no {@code defaultOptions}).
 */
@Configuration
public class AgenteAiConfig {

  private static final Logger LOG = LoggerFactory.getLogger(AgenteAiConfig.class);

  @Value("${edusync.ai.agente.provider:${edusync.ai.provider:ollama}}")
  private String proveedorAgente;

  @Value("${edusync.ai.agente.model:llama3.1:8b}")
  private String modeloAgente;

  @Value("${edusync.ai.agente.ollama-base-url:${edusync.ai.ollama.base-url:http://localhost:11434}}")
  private String ollamaBaseUrl;

  @Value("${edusync.ai.agente.timeout-seconds:${edusync.ai.ollama.timeout-seconds:120}}")
  private int timeoutSeconds;

  @Value("${edusync.ai.agente.temperature:0}")
  private Double temperature;

  @Value("${edusync.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta/openai}")
  private String geminiBaseUrl;

  @Value("${edusync.ai.gemini.api-key:}")
  private String geminiApiKey;

  @Value("${edusync.ai.gemini.model:gemini-2.0-flash}")
  private String geminiModel;

  @Bean(name = "agenteChatClient")
  public ChatClient agenteChatClient() {
    if ("gemini".equalsIgnoreCase(proveedorAgente)) {
      if (geminiApiKey == null || geminiApiKey.isBlank()) {
        LOG.warn("GEMINI_API_KEY vacia; el asistente fallara hasta definirla en el entorno");
      }
      String modelo = AiConfig.modeloParaAgente(proveedorAgente, modeloAgente, geminiModel);
      return AiConfig.openAiCompatible(
          AiConfig.geminiChatBaseUrl(geminiBaseUrl), geminiApiKey, modelo, timeoutSeconds, temperature);
    }
    OllamaApi ollamaApi =
        OllamaApi.builder()
            .baseUrl(ollamaBaseUrl)
            .restClientBuilder(AiConfig.http11RestClientBuilder(timeoutSeconds))
            .webClientBuilder(WebClient.builder())
            .build();
    OllamaChatModel chatModel =
        OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .options(OllamaChatOptions.builder()
                    .model(modeloAgente)
                    .temperature(temperature)
                    .build())
            .build();
    return ChatClient.builder(chatModel).build();
  }
}
