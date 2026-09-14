package com.edusync.shared.ai.infrastructure.config;

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

  @Value("${edusync.ai.agente.model:llama3.1:8b}")
  private String modeloAgente;

  @Value("${edusync.ai.agente.ollama-base-url:${edusync.ai.ollama.base-url:http://localhost:11434}}")
  private String ollamaBaseUrl;

  @Value("${edusync.ai.agente.timeout-seconds:${edusync.ai.ollama.timeout-seconds:120}}")
  private int timeoutSeconds;

  @Bean(name = "agenteChatClient")
  public ChatClient agenteChatClient() {
    OllamaApi ollamaApi =
        OllamaApi.builder()
            .baseUrl(ollamaBaseUrl)
            .restClientBuilder(AiConfig.http11RestClientBuilder(timeoutSeconds))
            .webClientBuilder(WebClient.builder())
            .build();
    OllamaChatModel chatModel =
        OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .options(OllamaChatOptions.builder().model(modeloAgente).build())
            .build();
    return ChatClient.builder(chatModel).build();
  }
}
