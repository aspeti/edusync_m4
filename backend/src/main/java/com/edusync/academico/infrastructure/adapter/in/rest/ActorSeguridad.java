package com.edusync.academico.infrastructure.adapter.in.rest;

import java.util.UUID;
import org.springframework.security.core.Authentication;

/** Lectura del JWT principal y roles para {@code DD-UC-017} (nunca del body). */
final class ActorSeguridad {

  private ActorSeguridad() {}

  static UUID id(Authentication authentication) {
    return (UUID) authentication.getPrincipal();
  }

  static boolean veTodasLasMaterias(Authentication authentication) {
    return tieneRol(authentication, "ROLE_ADMIN") || tieneRol(authentication, "ROLE_SECRETARIA");
  }

  static boolean esAdmin(Authentication authentication) {
    return tieneRol(authentication, "ROLE_ADMIN");
  }

  private static boolean tieneRol(Authentication authentication, String role) {
    return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
  }
}
