package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ResultadoGuardrailSalida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Guardrail de salida (ADR-0021): corre antes de entregar al usuario.
 * Oleada 1: sin exigencia de citas RAG ({@code requiereCita=false}).
 */
@Component
public class GuardrailSalidaAgente {

    private static final Logger log = LoggerFactory.getLogger(GuardrailSalidaAgente.class);

    static final String MENSAJE_SEGURO = "No pude generar una respuesta segura. Reformula la pregunta.";
    static final String MENSAJE_LEAK = "No puedo revelar instrucciones internas del sistema.";

    private static final Pattern TARJETA = Pattern.compile("\\b(?:\\d[ -]?){13,19}\\b");
    private static final Pattern RUDE = Pattern.compile("\\b\\d{12}\\b");
    private static final Pattern CI = Pattern.compile("\\b\\d{6,8}\\s?[A-Z]{2}\\b");
    private static final Pattern LEAK = Pattern.compile(
            "system prompt|instrucciones del sistema|instrucciones internas",
            Pattern.CASE_INSENSITIVE);

    public ResultadoGuardrailSalida revisar(String respuesta) {
        if (respuesta == null || respuesta.isBlank()) {
            log.debug("Guardrail salida problema=respuesta_vacia");
            return ResultadoGuardrailSalida.redactado(MENSAJE_SEGURO, "respuesta_vacia");
        }
        if (LEAK.matcher(respuesta).find()) {
            log.debug("Guardrail salida problema=system_prompt_leak");
            return ResultadoGuardrailSalida.redactado(MENSAJE_LEAK, "system_prompt_leak");
        }
        boolean fuga = false;
        String limpio = respuesta;
        if (TARJETA.matcher(limpio).find()) {
            limpio = TARJETA.matcher(limpio).replaceAll("[TARJETA]");
            fuga = true;
        }
        if (CI.matcher(limpio).find()) {
            limpio = CI.matcher(limpio).replaceAll("[CI]");
            fuga = true;
        }
        if (RUDE.matcher(limpio).find()) {
            limpio = RUDE.matcher(limpio).replaceAll("[RUDE]");
            fuga = true;
        }
        if (fuga) {
            log.debug("Guardrail salida problema=fuga_pii");
            return ResultadoGuardrailSalida.redactado(limpio, "fuga_pii");
        }
        return ResultadoGuardrailSalida.ok(limpio);
    }
}
