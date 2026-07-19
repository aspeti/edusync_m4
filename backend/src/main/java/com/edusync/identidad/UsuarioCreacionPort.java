package com.edusync.identidad;

/**
 * Puerto publico (Open Host Service, ADR-0011) del modulo {@code identidad}. Unica via
 * permitida para que otros modulos (p. ej. {@code plataforma} en {@code DD-UC-003}) creen
 * usuarios sin importar clases internas de {@code identidad.domain}/{@code identidad.application}.
 *
 * <p>Implementado por {@code identidad.infrastructure.adapter.in.rest.UsuarioCreacionPortImpl}.
 */
public interface UsuarioCreacionPort {

  /**
   * Crea un nuevo usuario. Lanza {@code identidad.domain.InvarianteRolException} si el
   * comando viola la invariante permanente {@code tenantId == null <=> roles == {SYSADMIN}}
   * (ADR-0010), y una excepcion de negocio si el email ya esta en uso.
   */
  UsuarioId crear(CrearUsuarioCommand command);
}
