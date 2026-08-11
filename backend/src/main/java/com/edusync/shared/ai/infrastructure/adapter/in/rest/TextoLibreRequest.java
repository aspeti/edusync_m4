package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTO de entrada de {@code POST /api/v1/ai/extraer/consulta-usuario}. */
public record TextoLibreRequest(
    @Schema(
        example =
            "Hola, no recuerdo el correo de un profesor que se llama Roberto, "
                + "¿pueden confirmarme si existe en el sistema?")
    @NotBlank(message = "prompt es obligatorio")
    @Size(max = 2000, message = "prompt no puede superar 2000 caracteres")
    String prompt) {}
