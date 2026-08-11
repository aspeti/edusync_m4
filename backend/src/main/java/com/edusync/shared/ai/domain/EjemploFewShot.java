package com.edusync.shared.ai.domain;

/**
 * Ejemplo few-shot (entrada de texto libre &rarr; salida JSON esperada) usado para guiar al
 * LLM en una extraccion estructurada. Reutilizable por cualquier esquema nuevo que consuma
 * {@link com.edusync.shared.ai.application.service.LlmStructuredExtractor}.
 */
public record EjemploFewShot(String entrada, String salidaJson) {}
