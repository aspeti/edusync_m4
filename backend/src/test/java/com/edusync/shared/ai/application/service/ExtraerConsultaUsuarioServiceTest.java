package com.edusync.shared.ai.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.ConsultaUsuarioDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtraerConsultaUsuarioServiceTest {

  @Mock private LlmStructuredExtractor extractor;

  @Test
  void delegaEnElExtractorCuandoAiEstaHabilitado() {
    ExtraerConsultaUsuarioService service = new ExtraerConsultaUsuarioService(extractor, true);
    when(extractor.extraer(anyString(), anyList(), eq("busco a Roberto"), eq(ConsultaUsuarioDTO.class)))
        .thenReturn(new ConsultaUsuarioDTO("Roberto"));

    ConsultaUsuarioDTO resultado = service.extraer("busco a Roberto");

    assertThat(resultado.nombreBuscado()).isEqualTo("Roberto");
    verify(extractor).extraer(anyString(), anyList(), eq("busco a Roberto"), eq(ConsultaUsuarioDTO.class));
  }

  @Test
  void rechazaCuandoAiEstaDeshabilitadoSinLlamarAlExtractor() {
    ExtraerConsultaUsuarioService service = new ExtraerConsultaUsuarioService(extractor, false);

    assertThatThrownBy(() -> service.extraer("busco a Roberto"))
        .isInstanceOf(AiDeshabilitadoException.class)
        .extracting(ex -> ((AiDeshabilitadoException) ex).getErrorCode())
        .isEqualTo("E_AI_DESHABILITADO");
    verifyNoInteractions(extractor);
  }
}
