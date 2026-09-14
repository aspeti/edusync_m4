package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.application.port.in.EjecutarConsultaAgenteUseCase;
import com.edusync.shared.ai.domain.HerramientaNoDisponibleException;
import com.edusync.shared.ai.domain.LimiteTurnosAlcanzadoException;
import com.edusync.shared.ai.domain.RespuestaAgente;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/v1/ai/agente — contrato nuevo del agente de tool calling
 * multipaso (ADR-0018). No modifica ni reemplaza AiChatController
 * (POST /api/v1/ai/chat, ADR-0017).
 *
 * El JWT se toma del header Authorization de la request actual y se
 * propaga tal cual al caso de uso — es el mismo JWT que ya autorizo
 * esta request contra el filtro de seguridad de identidad. Nunca se
 * sustituye por una cuenta tecnica (ADR-0018 seccion 3).
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AgenteController {

    private final EjecutarConsultaAgenteUseCase ejecutarConsultaAgenteUseCase;

    public AgenteController(EjecutarConsultaAgenteUseCase ejecutarConsultaAgenteUseCase) {
        this.ejecutarConsultaAgenteUseCase = ejecutarConsultaAgenteUseCase;
    }

    @PostMapping("/agente")
    public ResponseEntity<AgenteResponse> consultar(
            @RequestBody AgenteRequest request,
            HttpServletRequest httpRequest) {

        String authorization = httpRequest.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String jwt = authorization.substring("Bearer ".length());

        try {
            RespuestaAgente respuesta = ejecutarConsultaAgenteUseCase.consultar(request.pregunta(), jwt);
            return ResponseEntity.ok(AgenteResponse.desde(respuesta));
        } catch (LimiteTurnosAlcanzadoException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (HerramientaNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}
