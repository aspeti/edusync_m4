package com.edusync.shared.ai.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProveedorGeminiTest {

  @Test
  void baseUrlVacioUsaElEndpointOficial() {
    assertThat(AiConfig.geminiChatBaseUrl(null))
        .isEqualTo("https://generativelanguage.googleapis.com/v1beta/openai");
    assertThat(AiConfig.geminiChatBaseUrl("https://generativelanguage.googleapis.com/v1beta/openai/"))
        .isEqualTo("https://generativelanguage.googleapis.com/v1beta/openai");
  }

  @Test
  void agenteEnGeminiNoMandaElTagDeOllama() {
    assertThat(AiConfig.modeloParaAgente("gemini", "llama3.1:8b", "gemini-2.0-flash"))
        .isEqualTo("gemini-2.0-flash");
    assertThat(AiConfig.modeloParaAgente("gemini", "gemini-2.5-flash", "gemini-2.0-flash"))
        .isEqualTo("gemini-2.5-flash");
  }

  @Test
  void ollamaConservaElModeloDelAgente() {
    assertThat(AiConfig.modeloParaAgente("ollama", "llama3.1:8b", "gemini-2.0-flash"))
        .isEqualTo("llama3.1:8b");
  }
}
