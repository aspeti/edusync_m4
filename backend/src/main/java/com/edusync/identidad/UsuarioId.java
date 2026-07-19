package com.edusync.identidad;

import java.util.Objects;
import java.util.UUID;

/**
 * Identidad de un {@code Usuario}. Vive en la raiz del modulo (paquete API de Spring
 * Modulith) porque es parte del contrato publico de {@link UsuarioCreacionPort}, y a la
 * vez es el tipo de identidad usado internamente por el aggregate
 * {@code identidad.domain.Usuario} (una dependencia intra-modulo perfectamente valida).
 */
public record UsuarioId(UUID valor) {

  public UsuarioId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static UsuarioId nueva() {
    return new UsuarioId(UUID.randomUUID());
  }

  public static UsuarioId de(UUID valor) {
    return new UsuarioId(valor);
  }
}
