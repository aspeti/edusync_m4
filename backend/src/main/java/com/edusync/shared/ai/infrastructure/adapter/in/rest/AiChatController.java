package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.application.port.in.ChatConLlmUseCase;
import com.edusync.shared.ai.application.port.in.ConsultarUsuarioUseCase;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.EsquemaLlmNoCumplidoException;
import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaLlm;
import com.edusync.shared.ai.domain.UsuarioResumen;
import com.edusync.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "Spike de consumo de LLM (Ollama local, llama3.1:latest)")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

  private final ChatConLlmUseCase chatConLlmUseCase;
  private final ConsultarUsuarioUseCase consultarUsuarioUseCase;

  @PostMapping("/chat")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Enviar un prompt al LLM y obtener la respuesta")
  @ApiResponse(responseCode = "200", description = "Respuesta del modelo")
  @ApiResponse(responseCode = "400", description = "Validacion de entrada fallida")
  @ApiResponse(responseCode = "401", description = "Sin autenticacion")
  @ApiResponse(responseCode = "502", description = "Ollama no disponible (E_LLM_NO_DISPONIBLE)")
  @ApiResponse(responseCode = "503", description = "AI deshabilitado (E_AI_DESHABILITADO)")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    RespuestaLlm respuesta = chatConLlmUseCase.chatear(request.prompt());
    return ResponseEntity.ok(new ChatResponse(respuesta.texto(), respuesta.modelo()));
  }

  @PostMapping("/consultar-usuario")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Consultar usuarios del tenant a partir de una pregunta en texto libre",
      description =
          "Extrae el nombre buscado (generar -> validar -> reintentar -> fallar controlado) y "
              + "busca coincidencias en el tenant del usuario autenticado. Nunca expone "
              + "passwordHash ni datos de otro tenant; cualquier usuario autenticado del tenant "
              + "puede invocarlo.")
  @ApiResponse(responseCode = "200", description = "Coincidencias encontradas (puede ser una lista vacia)")
  @ApiResponse(responseCode = "400", description = "Validacion de entrada fallida")
  @ApiResponse(responseCode = "401", description = "Sin autenticacion")
  @ApiResponse(responseCode = "502", description = "LLM no disponible o no cumplio el esquema tras varios intentos")
  @ApiResponse(responseCode = "503", description = "AI deshabilitado (E_AI_DESHABILITADO)")
  public ResponseEntity<List<UsuarioResumenResponse>> consultarUsuario(
      @Valid @RequestBody TextoLibreRequest request) {
    List<UsuarioResumen> resultados = consultarUsuarioUseCase.consultar(request.prompt());
    List<UsuarioResumenResponse> respuesta =
        resultados.stream()
            .map(u -> new UsuarioResumenResponse(u.nombreCompleto(), u.email(), u.roles(), u.activo()))
            .toList();
    return ResponseEntity.ok(respuesta);
  }

  @ExceptionHandler(EsquemaLlmNoCumplidoException.class)
  public ResponseEntity<ErrorResponse> alManejarEsquemaLlmNoCumplido(EsquemaLlmNoCumplidoException ex) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  @ExceptionHandler(AiDeshabilitadoException.class)
  public ResponseEntity<ErrorResponse> alManejarAiDeshabilitado(AiDeshabilitadoException ex) {
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }

  @ExceptionHandler(LlmNoDisponibleException.class)
  public ResponseEntity<ErrorResponse> alManejarLlmNoDisponible(LlmNoDisponibleException ex) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
  }
}
