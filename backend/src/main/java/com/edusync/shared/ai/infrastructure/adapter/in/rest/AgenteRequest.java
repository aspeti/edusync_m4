package com.edusync.shared.ai.infrastructure.adapter.in.rest;

import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import com.edusync.shared.ai.domain.TurnoHistorialAgente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AgenteRequest(
        @Schema(example = "¿Cuántos cursos hay en esta gestión?")
        @NotBlank(message = "pregunta es obligatoria")
        @Size(max = 4000, message = "pregunta no puede superar 4000 caracteres")
        String pregunta,
        @Schema(description = "Si true, ejecuta una tool write pendiente. Default false.")
        Boolean confirmed,
        List<TurnoHistorialAgente> history,
        ContextoConsultaAgente contexto) {
}
