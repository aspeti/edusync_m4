package com.edusync.identidad.application.port.in;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioId;

/**
 * Puerto de entrada: creacion de usuarios. Implementado por {@code CrearUsuarioService}.
 *
 * <p>Uso interno del modulo {@code identidad} (seed del primer {@code SYSADMIN} en
 * {@code DD-UC-002}; alta de usuarios de tenant desde {@code UsuarioController} en
 * {@code DD-UC-005}); expuesto a otros modulos unicamente a traves de
 * {@code identidad.UsuarioCreacionPort}, nunca importando esta interfaz directamente desde
 * fuera del modulo (es un paquete interno, {@code application}).
 */
public interface CrearUsuarioUseCase {

  /**
   * @throws com.edusync.identidad.domain.InvarianteRolException si el comando viola la
   *     invariante permanente {@code tenantId == null <=> roles == {SYSADMIN}}
   */
  UsuarioId crear(CrearUsuarioCommand command);
}
