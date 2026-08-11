package com.edusync.shared.ai.domain;

import jakarta.validation.constraints.NotBlank;

/**
 * Esquema estructurado extraido de una solicitud en texto libre del tipo "no recuerdo el
 * correo de un usuario llamado X, ¿existe en el sistema?". Solo contiene el nombre buscado:
 * ningun otro dato (email, RUDE, tenant, roles) puede salir del LLM (AGENTS.md &sect;7) —
 * ese es el unico campo que es lenguaje natural real dicho por quien pregunta.
 *
 * <p>Vive en {@code domain} (no en {@code infrastructure/adapter/in/rest}, a diferencia del
 * resto de DTOs con Bean Validation del proyecto) porque valida la <em>salida del LLM</em>,
 * no la entrada de un cliente REST: es el mismo rol que ya cumple {@link RespuestaLlm} para el
 * chat libre. Las anotaciones de Jakarta Validation no imponen comportamiento de framework en
 * runtime sobre esta clase (el motor {@code Validator} se inyecta externamente) — mismo
 * razonamiento que el *allowlist* de Lombok en dominio (`ADR-0012`). Nunca se expone
 * directamente por API (AGENTS.md &sect;5): {@code AiChatController} la traduce a
 * {@code ConsultaUsuarioResponse}.
 */
public record ConsultaUsuarioDTO(
    @NotBlank(message = "nombreBuscado es obligatorio") String nombreBuscado) {}
