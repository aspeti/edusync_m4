package com.edusync.plataforma.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO de entrada de {@code POST /api/v1/plataforma/tenants} ({@code FSD-UC-011}, paso 1).
 *
 * <p>{@code fechaVencimientoSuscripcion} deliberadamente SIN {@code @NotNull}: su ausencia
 * es un caso de negocio con codigo propio ({@code E_SUSCRIPCION_INCOMPLETA}, HTTP 422,
 * flujo alternativo A1), no un error de validacion generico (HTTP 400).
 */
public record RegistrarTenantRequest(
    @Schema(example = "Unidad Educativa Ejemplo")
    @NotBlank(message = "nombre es obligatorio")
    String nombre,

    @Schema(example = "2026-01-01")
    @NotNull(message = "fechaInicioSuscripcion es obligatoria")
    LocalDate fechaInicioSuscripcion,

    @Schema(example = "2026-12-31")
    LocalDate fechaVencimientoSuscripcion) {
}
