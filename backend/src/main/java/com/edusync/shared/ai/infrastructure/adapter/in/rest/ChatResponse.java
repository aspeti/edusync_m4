package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatResponse(
    @Schema(example = "EduSync es una plataforma SaaS...") String respuesta,
    @Schema(example = "llama3.1:latest") String modelo) {}
