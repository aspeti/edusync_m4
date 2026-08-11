package com.edusync.shared.ai.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatConLlmServiceTest {

  @Mock private LlmPort llmPort;

  @Test
  void delegaAlPuertoCuandoAiEstaHabilitado() {
    ChatConLlmService service = new ChatConLlmService(llmPort, true);
    when(llmPort.completar("hola")).thenReturn(new RespuestaLlm("mundo", "llama3.1:latest"));

    RespuestaLlm respuesta = service.chatear("hola");

    assertThat(respuesta.texto()).isEqualTo("mundo");
    assertThat(respuesta.modelo()).isEqualTo("llama3.1:latest");
    verify(llmPort).completar("hola");
  }

  @Test
  void rechazaCuandoAiEstaDeshabilitado() {
    ChatConLlmService service = new ChatConLlmService(llmPort, false);

    assertThatThrownBy(() -> service.chatear("hola"))
        .isInstanceOf(AiDeshabilitadoException.class)
        .extracting(ex -> ((AiDeshabilitadoException) ex).getErrorCode())
        .isEqualTo("E_AI_DESHABILITADO");
  }
}
