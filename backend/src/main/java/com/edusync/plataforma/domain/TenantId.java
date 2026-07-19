package com.edusync.plataforma.domain;

import java.util.Objects;
import java.util.UUID;

/** Identidad de un {@link Tenant}. Interno al modulo (sin uso publico documentado hoy). */
public record TenantId(UUID valor) {

  public TenantId {
    Objects.requireNonNull(valor, "valor no puede ser nulo");
  }

  public static TenantId nueva() {
    return new TenantId(UUID.randomUUID());
  }

  public static TenantId de(UUID valor) {
    return new TenantId(valor);
  }
}
