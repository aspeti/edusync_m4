package com.edusync.shared.ai.infrastructure.adapter.out.openwebui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

class OpenWebUiLlmAdapterTest {

  @Test
  void fallaSiNoHayApiKey() {
    AiProperties props = new AiProperties();
    props.getOpenWebui().setApiKey("  ");
    OpenWebUiLlmAdapter adapter =
        new OpenWebUiLlmAdapter(ChatClient.create(mock(ChatModel.class)), props);

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("OPEN_WEBUI_API_KEY");
  }

  @Test
  void happyPathDevuelveTextoYModelo() {
    ChatModel chatModel = chatModelStub();
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta("ok", "llama3.1:latest"));
    AiProperties props = new AiProperties();
    props.getOpenWebui().setApiKey("test-key");
    OpenWebUiLlmAdapter adapter = new OpenWebUiLlmAdapter(ChatClient.create(chatModel), props);

    RespuestaLlm result = adapter.completar("hola");

    assertThat(result.texto()).isEqualTo("ok");
    assertThat(result.modelo()).isEqualTo("llama3.1:latest");
  }

  @Test
  void respuestaVaciaLanzaLlmNoDisponible() {
    ChatModel chatModel = chatModelStub();
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta("  ", "llama3.1:latest"));
    AiProperties props = new AiProperties();
    props.getOpenWebui().setApiKey("test-key");
    OpenWebUiLlmAdapter adapter = new OpenWebUiLlmAdapter(ChatClient.create(chatModel), props);

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("respuesta vacia");
  }

  @Test
  void errorDeRedLanzaLlmNoDisponible() {
    ChatModel chatModel = chatModelStub();
    when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("boom"));
    AiProperties props = new AiProperties();
    props.getOpenWebui().setApiKey("test-key");
    OpenWebUiLlmAdapter adapter = new OpenWebUiLlmAdapter(ChatClient.create(chatModel), props);

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("error de red");
  }

  private static ChatModel chatModelStub() {
    ChatModel chatModel = mock(ChatModel.class);
    when(chatModel.getOptions())
        .thenReturn(ChatOptions.builder().model("llama3.1:latest").build());
    return chatModel;
  }

  private static ChatResponse respuesta(String texto, String modelo) {
    return ChatResponse.builder()
        .generations(List.of(new Generation(new AssistantMessage(texto))))
        .metadata(ChatResponseMetadata.builder().model(modelo).build())
        .build();
  }
}
