package com.edusync.shared.ai.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.application.port.out.LlmPort;
import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;
import com.edusync.shared.ai.domain.EsquemaLlmNoCumplidoException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class LlmStructuredExtractorTest {

  private LlmPort llmPort;
  private LlmStructuredExtractor extractor;

  @BeforeEach
  void setUp() {
    llmPort = mock(LlmPort.class);
    ObjectMapper objectMapper = new ObjectMapper();
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    extractor = new LlmStructuredExtractor(llmPort, objectMapper, validator);
  }

  @Test
  void devuelveElEsquemaAlPrimerIntentoSiElJsonEsValido() {
    when(llmPort.completar(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(new RespuestaLlm("{\"nombreBuscado\": \"Roberto Fernandez\"}", "llama3.1:latest"));

    ConsultaUsuarioDTO resultado =
        extractor.extraer("instrucciones", List.of(), "texto de entrada", ConsultaUsuarioDTO.class);

    assertThat(resultado.nombreBuscado()).isEqualTo("Roberto Fernandez");
    verify(llmPort, times(1)).completar(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void reintentaConElErrorComoFeedbackYTerminaAceptandoLaSegundaRespuesta() {
    when(llmPort.completar(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(new RespuestaLlm("{\"nombreBuscado\": \"\"}", "llama3.1:latest"))
        .thenReturn(new RespuestaLlm("{\"nombreBuscado\": \"Maria Rojas\"}", "llama3.1:latest"));

    ConsultaUsuarioDTO resultado =
        extractor.extraer("instrucciones", List.of(), "texto de entrada", ConsultaUsuarioDTO.class);

    assertThat(resultado.nombreBuscado()).isEqualTo("Maria Rojas");
    verify(llmPort, times(2)).completar(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void reintentaCuandoLaRespuestaNoEsJsonValido() {
    when(llmPort.completar(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(new RespuestaLlm("esto no es json", "llama3.1:latest"))
        .thenReturn(new RespuestaLlm("{\"nombreBuscado\": \"Ana Lopez\"}", "llama3.1:latest"));

    ConsultaUsuarioDTO resultado =
        extractor.extraer("instrucciones", List.of(), "texto de entrada", ConsultaUsuarioDTO.class);

    assertThat(resultado.nombreBuscado()).isEqualTo("Ana Lopez");
  }

  @Test
  void fallaDeFormaControladaTrasAgotarLosIntentos() {
    when(llmPort.completar(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(new RespuestaLlm("{\"nombreBuscado\": \"\"}", "llama3.1:latest"));

    assertThatThrownBy(() -> extractor.extraer("instrucciones", List.of(), "texto", ConsultaUsuarioDTO.class))
        .isInstanceOf(EsquemaLlmNoCumplidoException.class)
        .extracting(ex -> ((EsquemaLlmNoCumplidoException) ex).getErrorCode())
        .isEqualTo("E_ESQUEMA_LLM_NO_CUMPLIDO");

    verify(llmPort, times(3)).completar(org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void limpiaTextoAdicionalAlrededorDelBloqueJson() {
    when(llmPort.completar(org.mockito.ArgumentMatchers.anyString()))
        .thenReturn(
            new RespuestaLlm(
                "Claro, aqui esta el JSON:\n```json\n{\"nombreBuscado\": \"Luis Vera\"}\n```",
                "llama3.1:latest"));

    ConsultaUsuarioDTO resultado =
        extractor.extraer("instrucciones", List.of(), "texto", ConsultaUsuarioDTO.class);

    assertThat(resultado.nombreBuscado()).isEqualTo("Luis Vera");
  }
}
