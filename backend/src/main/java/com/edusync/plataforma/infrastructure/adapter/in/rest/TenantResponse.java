package com.edusync.plataforma.infrastructure.adapter.in.rest;

import java.time.LocalDate;
import java.util.UUID;

/** DTO de salida de las operaciones de {@code TenantController} sobre un Tenant. */
public record TenantResponse(
    UUID id,
    String nombre,
    LocalDate fechaInicioSuscripcion,
    LocalDate fechaVencimientoSuscripcion,
    String estado) {
}
