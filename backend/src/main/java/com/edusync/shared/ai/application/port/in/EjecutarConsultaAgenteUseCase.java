package com.edusync.shared.ai.application.port.in;

import com.edusync.shared.ai.domain.RespuestaAgente;
import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import com.edusync.shared.ai.domain.TurnoHistorialAgente;

import java.util.List;

/**
 * Puerto de entrada del agente de tool calling multipaso.
 * Ver ADR-0018 (Alternativa C), ADR-0019 y ADR-0020.
 */
public interface EjecutarConsultaAgenteUseCase {

    default RespuestaAgente consultar(String pregunta, String jwtUsuario) {
        return consultar(pregunta, jwtUsuario, false);
    }

    /**
     * @param pregunta   pregunta en lenguaje natural del usuario.
     * @param jwtUsuario JWT del usuario autenticado (ADR-0018: identidad de ejecucion).
     * @param confirmed  si es {@code true}, una tool {@code write} puede mutar; si no, solo preview.
     */
    default RespuestaAgente consultar(String pregunta, String jwtUsuario, boolean confirmed) {
        return consultar(pregunta, jwtUsuario, confirmed, List.of(), null);
    }

    RespuestaAgente consultar(
            String pregunta,
            String jwtUsuario,
            boolean confirmed,
            List<TurnoHistorialAgente> history,
            ContextoConsultaAgente contexto);
}
