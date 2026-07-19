package com.edusync.identidad.application.port.in;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioId;

/**
 * Puerto de entrada: creacion de usuarios. Implementado por {@code CrearUsuarioService}.
 *
 * <p>Uso interno del modulo en este Design Doc (seed del primer {@code SYSADMIN}); expuesto
 * a otros modulos unicamente a traves de {@code identidad.UsuarioCreacionPort}, nunca
 * importando esta interfaz directamente (es un paquete interno, {@code application}).
 */
public interface CrearUsuarioUseCase {

  /**
   * @throws com.edusync.identidad.domain.InvarianteRolException si el comando viola la
   *     invariante permanente {@code tenantId == null <=> roles == {SYSADMIN}}
   */
  UsuarioId crear(CrearUsuarioCommand command);
}
