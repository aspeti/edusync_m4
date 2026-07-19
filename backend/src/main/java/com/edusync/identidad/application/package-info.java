/**
 * Capa de aplicacion del modulo {@code identidad}: puertos {@code port/in}, {@code port/out}
 * y servicios de orquestacion (login, creacion de usuarios). Poblado por {@code DD-UC-002}
 * (docs/design/DD-UC-002.md). {@code AutenticarUsuarioService} consulta
 * {@code identidad.TenantConsultaPort} (API publica del propio modulo, implementada por
 * {@code plataforma}) para aplicar {@code BR-014} ({@code DD-UC-003}).
 */
package com.edusync.identidad.application;
