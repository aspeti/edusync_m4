package com.edusync.shared.ai.infrastructure.adapter.out.ollama;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

class OllamaLlmAdapterTest {

  private ChatModel chatModel;
  private OllamaLlmAdapter adapter;

  @BeforeEach
  void setUp() {
    chatModel = mock(ChatModel.class);
    when(chatModel.getOptions())
        .thenReturn(ChatOptions.builder().model("llama3.1:latest").build());
    AiProperties props = new AiProperties();
    props.getOllama().setModel("llama3.1:latest");
    adapter = new OllamaLlmAdapter(ChatClient.create(chatModel), props);
  }

  @Test
  void happyPathDevuelveTextoYModelo() {
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta("mundo", "llama3.1:latest"));

    RespuestaLlm result = adapter.completar("hola");

    assertThat(result.texto()).isEqualTo("mundo");
    assertThat(result.modelo()).isEqualTo("llama3.1:latest");
  }

  @Test
  void respuestaVaciaLanzaLlmNoDisponible() {
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta("   ", "llama3.1:latest"));

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("respuesta vacia");
  }

  @Test
  void errorDeRedLanzaLlmNoDisponible() {
    when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("boom"));

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("error de red");
  }

  private static ChatResponse respuesta(String texto, String modelo) {
    return ChatResponse.builder()
        .generations(List.of(new Generation(new AssistantMessage(texto))))
        .metadata(ChatResponseMetadata.builder().model(modelo).build())
        .build();
  }
}
