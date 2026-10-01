package com.edusync.shared.ai.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Beans {@link ChatClient} hacia Ollama u Open WebUI segun {@code edusync.ai.provider}.
 *
 * <p>No se registra un {@link RestClient.Builder} global (evita pisar otros clientes HTTP).
 * Ollama usa JDK {@link HttpClient} HTTP/1.1. Open WebUI usa el cliente OkHttp de Spring
 * AI 2.0; en HTTP claro (localhost) OkHttp no negocia HTTP/2, que era el fallo de uvicorn
 * con el JDK HttpClient.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

  @Bean
  @Primary
  @ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "ollama", matchIfMissing = true)
  ChatClient ollamaChatClient(AiProperties aiProperties) {
    AiProperties.Ollama ollama = aiProperties.getOllama();
    OllamaApi ollamaApi =
        OllamaApi.builder()
            .baseUrl(ollama.getBaseUrl())
            .restClientBuilder(http11RestClientBuilder(ollama.getTimeoutSeconds()))
            .webClientBuilder(WebClient.builder())
            .build();
    OllamaChatModel chatModel =
        OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .options(OllamaChatOptions.builder().model(ollama.getModel()).build())
            .build();
    return ChatClient.create(chatModel);
  }

  @Bean
  @Primary
  @ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "open-webui")
  ChatClient openWebUiChatClient(AiProperties aiProperties) {
    AiProperties.OpenWebUi openWebUi = aiProperties.getOpenWebui();
    String apiKey = openWebUi.getApiKey();
    OpenAiChatModel chatModel =
        OpenAiChatModel.builder()
            .options(
                OpenAiChatOptions.builder()
                    .baseUrl(openWebUiChatBaseUrl(openWebUi.getBaseUrl()))
                    .apiKey(apiKey == null || apiKey.isBlank() ? "missing" : apiKey)
                    .model(openWebUi.getModel())
                    .timeout(Duration.ofSeconds(openWebUi.getTimeoutSeconds()))
                    .build())
            .build();
    return ChatClient.create(chatModel);
  }

  @Bean
  @Primary
  @ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "gemini")
  ChatClient geminiChatClient(AiProperties aiProperties) {
    AiProperties.Gemini gemini = aiProperties.getGemini();
    if (gemini.getApiKey() == null || gemini.getApiKey().isBlank()) {
      org.slf4j.LoggerFactory.getLogger(AiConfig.class)
          .warn("GEMINI_API_KEY vacia; POST /api/v1/ai/chat fallara hasta definirla en el entorno");
    }
    return openAiCompatible(
        geminiChatBaseUrl(gemini.getBaseUrl()),
        gemini.getApiKey(),
        gemini.getModel(),
        gemini.getTimeoutSeconds(),
        null);
  }

  /**
   * El SDK concatena {@code /chat/completions}. El default de Gemini ya incluye
   * {@code /v1beta/openai}; no se le agrega {@code /api}.
   */
  static String geminiChatBaseUrl(String configured) {
    if (configured == null || configured.isBlank()) {
      return "https://generativelanguage.googleapis.com/v1beta/openai";
    }
    return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
  }

  /**
   * El default del agente es un tag de Ollama. Con proveedor Gemini ese tag no existe:
   * se usa {@code edusync.ai.gemini.model} salvo que el operador haya puesto otro modelo.
   */
  static String modeloParaAgente(String proveedor, String modeloAgente, String modeloGemini) {
    if (proveedor == null || !proveedor.equalsIgnoreCase("gemini")) {
      return modeloAgente;
    }
    if (modeloAgente == null || modeloAgente.isBlank() || modeloAgente.startsWith("llama")) {
      return modeloGemini == null || modeloGemini.isBlank() ? "gemini-2.0-flash" : modeloGemini;
    }
    return modeloAgente;
  }

  static ChatClient openAiCompatible(
      String baseUrl, String apiKey, String model, int timeoutSeconds, Double temperature) {
    OpenAiChatOptions.Builder options =
        OpenAiChatOptions.builder()
            .baseUrl(baseUrl)
            .apiKey(apiKey == null || apiKey.isBlank() ? "missing" : apiKey)
            .model(model)
            .timeout(Duration.ofSeconds(timeoutSeconds));
    if (temperature != null) {
      options.temperature(temperature);
    }
    return ChatClient.create(OpenAiChatModel.builder().options(options.build()).build());
  }

  /**
   * El SDK OpenAI de Spring AI 2.0 concatena {@code /chat/completions} al base URL (el
   * default oficial es {@code https://api.openai.com/v1}). El spike usaba {@code
   * {base}/api/chat/completions}; si el operador ya puso {@code /api} o {@code /v1}, no se
   * duplica.
   */
  static String openWebUiChatBaseUrl(String configured) {
    if (configured == null || configured.isBlank()) {
      return "http://localhost:3000/api";
    }
    String trimmed = configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
    if (trimmed.endsWith("/api") || trimmed.endsWith("/v1")) {
      return trimmed;
    }
    return trimmed + "/api";
  }

  static RestClient.Builder http11RestClientBuilder(int timeoutSeconds) {
    HttpClient httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
    return RestClient.builder().requestFactory(requestFactory);
  }
}
