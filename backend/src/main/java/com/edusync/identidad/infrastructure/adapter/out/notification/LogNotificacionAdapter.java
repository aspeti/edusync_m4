package com.edusync.identidad.infrastructure.adapter.out.notification;

import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.out.NotificacionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Implementacion placeholder de {@link NotificacionPort} (DD-UC-005 &sect;1): EduSync no
 * tiene todavia un proveedor de email decidido. Solo registra que el flujo se inicio, sin
 * exponer el email ni el token en el log (AGENTS.md &sect;7 — solo referencias por id
 * interno). Migrar a un adaptador real (p. ej. AWS SES) no requiere tocar el dominio ni la
 * capa de aplicacion.
 */
@Component
public class LogNotificacionAdapter implements NotificacionPort {

  private static final Logger LOG = LoggerFactory.getLogger(LogNotificacionAdapter.class);

  @Override
  public void notificarRestablecimientoPassword(UsuarioId usuarioId, String email, String tokenPlano) {
    LOG.info(
        "Restablecimiento de password iniciado para usuario id={}. Entrega real pendiente "
            + "(placeholder log-only, DD-UC-005 seccion 1).",
        usuarioId.valor());
  }
}
