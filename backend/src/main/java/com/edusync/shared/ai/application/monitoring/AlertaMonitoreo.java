package com.edusync.shared.ai.application.monitoring;

public record AlertaMonitoreo(
        String severidad,
        String metrica,
        String observado,
        String umbral,
        String causa,
        String accion) {
}
