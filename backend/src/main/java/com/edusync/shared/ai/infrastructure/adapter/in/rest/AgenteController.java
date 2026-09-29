package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.application.port.in.EjecutarConsultaAgenteUseCase;
import com.edusync.shared.ai.domain.AiDeshabilitadoException;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.LlmNoDisponibleException;
import com.edusync.shared.ai.domain.RespuestaAgente;
import com.edusync.shared.web.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/v1/ai/agente — contrato de producto del asistente (ADR-0018,
 * ADR-0019, DD-UC-024/025). KEYWORD (0 turnos) o ReAct; no reemplaza
 * {@code POST /api/v1/ai/chat} ni {@code /consultar-usuario}.
 */
@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI", description = "Asistente de consulta con tool calling (Ollama)")
@SecurityRequirement(name = "bearerAuth")
public class AgenteController {

    private final EjecutarConsultaAgenteUseCase ejecutarConsultaAgenteUseCase;

    public AgenteController(EjecutarConsultaAgenteUseCase ejecutarConsultaAgenteUseCase) {
        this.ejecutarConsultaAgenteUseCase = ejecutarConsultaAgenteUseCase;
    }

    @PostMapping("/agente")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar el sistema academico en lenguaje natural (ReAct + tools de solo lectura)")
    public ResponseEntity<AgenteResponse> consultar(
            @Valid @RequestBody AgenteRequest request,
            HttpServletRequest httpRequest) {

        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        String jwt = authorization.substring("Bearer ".length());
        boolean confirmed = Boolean.TRUE.equals(request.confirmed());
        RespuestaAgente respuesta = ejecutarConsultaAgenteUseCase.consultar(
                request.pregunta(),
                jwt,
                confirmed,
                request.history() == null ? java.util.List.of() : request.history(),
                request.contexto());
        return ResponseEntity.ok(AgenteResponse.desde(respuesta));
    }

    @ExceptionHandler(LimiteTurnosAlcanzadoException.class)
    public ResponseEntity<ErrorResponse> alManejarLimiteTurnos(LimiteTurnosAlcanzadoException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(HerramientaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> alManejarHerramienta(HerramientaNoDisponibleException ex) {
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
