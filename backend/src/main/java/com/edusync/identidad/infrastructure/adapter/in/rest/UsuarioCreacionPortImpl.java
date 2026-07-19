package com.edusync.identidad.infrastructure.adapter.in.rest;

import com.edusync.identidad.CrearUsuarioCommand;
import com.edusync.identidad.UsuarioCreacionPort;
import com.edusync.identidad.UsuarioId;
import com.edusync.identidad.application.port.in.CrearUsuarioUseCase;
import org.springframework.stereotype.Component;

/**
 * Implementacion del puerto publico {@link UsuarioCreacionPort} (Open Host Service,
 * ADR-0011). A pesar de vivir en el subpaquete {@code adapter.in.rest} (misma ubicacion
 * que los controladores REST, siguiendo el arbol de componentes de {@code DD-UC-002} &sect;2),
 * NO expone un endpoint HTTP propio: es un adaptador "in" en el sentido de que otros
 * modulos (p. ej. {@code plataforma} en {@code DD-UC-003}) "entran" al modulo
 * {@code identidad} a traves de esta clase, via llamada Java directa (no HTTP).
 */
@Component
class UsuarioCreacionPortImpl implements UsuarioCreacionPort {

  private final CrearUsuarioUseCase crearUsuarioUseCase;

  UsuarioCreacionPortImpl(CrearUsuarioUseCase crearUsuarioUseCase) {
    this.crearUsuarioUseCase = crearUsuarioUseCase;
  }

  @Override
  public UsuarioId crear(CrearUsuarioCommand command) {
    return crearUsuarioUseCase.crear(command);
  }
}
