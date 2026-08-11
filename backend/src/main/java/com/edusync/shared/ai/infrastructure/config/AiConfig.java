package com.edusync.shared.ai.infrastructure.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * RestClients hacia Ollama y/o Open WebUI segun {@code edusync.ai.provider}.
 * Se construyen con {@link RestClient#builder()} (sin bean {@code RestClient.Builder}).
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

  @Bean
  @ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "ollama", matchIfMissing = true)
  RestClient ollamaRestClient(AiProperties aiProperties) {
    return buildClient(
        aiProperties.getOllama().getBaseUrl(), aiProperties.getOllama().getTimeoutSeconds());
  }

  @Bean
  @ConditionalOnProperty(name = "edusync.ai.provider", havingValue = "open-webui")
  RestClient openWebUiRestClient(AiProperties aiProperties) {
    return buildClient(
        aiProperties.getOpenWebui().getBaseUrl(),
        aiProperties.getOpenWebui().getTimeoutSeconds());
  }

  private static RestClient buildClient(String baseUrl, int timeoutSeconds) {
    // HTTP/1.1: Open WebUI (uvicorn) puede responder 400 "Invalid HTTP request"
    // si el JDK HttpClient negocia HTTP/2.
    HttpClient httpClient =
        HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
    return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
  }
}
