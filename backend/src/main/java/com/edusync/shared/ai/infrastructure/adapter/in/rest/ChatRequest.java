package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @Schema(example = "Explica en una frase que es EduSync")
    @NotBlank(message = "prompt es obligatorio")
    @Size(max = 8000, message = "prompt no puede superar 8000 caracteres")
    String prompt) {}
