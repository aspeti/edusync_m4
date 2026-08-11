package com.edusync.shared.ai.infrastructure.adapter.out.openwebui;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.infrastructure.config.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class OpenWebUiLlmAdapterTest {

  @Test
  void fallaSiNoHayApiKey() {
    AiProperties props = new AiProperties();
    props.getOpenWebui().setApiKey("  ");
    OpenWebUiLlmAdapter adapter = new OpenWebUiLlmAdapter(mock(RestClient.class), props);

    assertThatThrownBy(() -> adapter.completar("hola"))
        .isInstanceOf(LlmNoDisponibleException.class)
        .hasMessageContaining("OPEN_WEBUI_API_KEY");
  }
}
