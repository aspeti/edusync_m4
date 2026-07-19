package com.edusync.identidad.domain;

import java.util.Objects;

/**
 * Relacion N:M entre {@link Usuario} y {@link Rol} (ADR-0010). En memoria de dominio basta
 * con el {@link Rol}: la identidad del {@code Usuario} propietario la determina la coleccion
 * que lo contiene; la clave foranea explicita ({@code usuario_id}) es un detalle de
 * persistencia resuelto por {@code UsuarioRolJpaEntity}.
 */
public record UsuarioRol(Rol rol) {

  public UsuarioRol {
    Objects.requireNonNull(rol, "rol no puede ser nulo");
  }
}
