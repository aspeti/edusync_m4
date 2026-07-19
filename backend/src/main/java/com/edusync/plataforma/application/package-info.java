/**
 * Capa de aplicacion del modulo {@code plataforma}: puertos {@code port/in}, {@code port/out}
 * y servicios de orquestacion (alta de Tenants, cambio de estado, alta de admin,
 * scheduler de vencimiento). {@code CrearAdminTenantService} es el primer consumidor
 * real de {@code identidad.UsuarioCreacionPort} (API publica de {@code identidad}).
 * Poblado por {@code DD-UC-003} (docs/design/DD-UC-003.md).
 */
package com.edusync.plataforma.application;
