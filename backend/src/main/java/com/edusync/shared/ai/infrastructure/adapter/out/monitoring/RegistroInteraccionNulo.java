package com.edusync.shared.ai.infrastructure.adapter.out.monitoring;

import com.edusync.shared.ai.application.monitoring.RegistroInteraccionAgente;
import com.edusync.shared.ai.domain.RespuestaAgente;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "edusync.ai.monitor.registro", havingValue = "false", matchIfMissing = true)
public class RegistroInteraccionNulo implements RegistroInteraccionAgente {

    @Override
    public void registrar(String jwt, String pregunta, RespuestaAgente respuesta, long latenciaMs) {
        // Apagado: no hay bitácora.
    }
}
