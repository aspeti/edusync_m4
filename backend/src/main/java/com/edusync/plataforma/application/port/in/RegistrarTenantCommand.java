package com.edusync.plataforma.application.port.in;

import java.time.LocalDate;

/**
 * Comando de {@link RegistrarTenantUseCase} ({@code FSD-UC-011}, paso 1). Uso interno del
 * modulo (nada fuera de {@code plataforma} registra tenants), a diferencia de
 * {@code identidad.CrearUsuarioCommand}, que si vive en la raiz publica de su modulo.
 *
 * @param fechaVencimientoSuscripcion nula si el SysAdmin omitio el campo en la request;
 *     {@link com.edusync.plataforma.domain.Tenant#crear} la valida y lanza
 *     {@code SuscripcionIncompletaException} (422, flujo alternativo A1)
 */
public record RegistrarTenantCommand(
    String nombre, LocalDate fechaInicioSuscripcion, LocalDate fechaVencimientoSuscripcion) {
}
