package com.edusync.identidad.application.port.out;

import com.edusync.identidad.UsuarioId;

/**
 * Puerto de salida: notificacion al usuario. Implementado hoy por un adaptador
 * <em>log-only</em> (DD-UC-005 &sect;1): EduSync no tiene todavia un proveedor de email
 * decidido (no aparece en la tabla de stack de AGENTS.md &sect;4). Migrar a un adaptador
 * real (p. ej. AWS SES) es un cambio de infraestructura acotado que no toca el dominio ni
 * la capa de aplicacion.
 */
public interface NotificacionPort {

  void notificarRestablecimientoPassword(UsuarioId usuarioId, String email, String tokenPlano);
}
