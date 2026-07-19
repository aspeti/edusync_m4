package com.edusync.identidad;

import java.util.Set;
import java.util.UUID;

/**
 * Comando publico de {@link UsuarioCreacionPort}. Los roles se expresan como
 * {@code String} (no como {@code identidad.domain.Rol}) deliberadamente: el enum de
 * dominio es un detalle interno del modulo {@code identidad} y no se expone a otros
 * modulos (p. ej. {@code plataforma} en DD-UC-003), que solo conocen nombres de rol.
 *
 * @param tenantId nulo unicamente si {@code roles} es exactamente {@code {"SYSADMIN"}}
 *     (invariante permanente, ADR-0010)
 * @param passwordPlano contrasena en texto plano; se hashea con BCrypt antes de persistir,
 *     nunca se guarda ni se loguea en claro (AGENTS.md &sect;7)
 * @param roles nombres de {@code identidad.domain.Rol} (no sensibles a mayusculas/minusculas)
 */
public record CrearUsuarioCommand(
    UUID tenantId,
    String nombreCompleto,
    String email,
    String passwordPlano,
    Set<String> roles) {
}
