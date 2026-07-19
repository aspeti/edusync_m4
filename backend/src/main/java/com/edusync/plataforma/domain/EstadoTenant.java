package com.edusync.plataforma.domain;

/**
 * Ciclo de vida de un {@link Tenant} (BR-013/BR-014, {@code FSD-UC-011}).
 *
 * <p>{@code ACTIVO} permite el login de sus usuarios; {@code SUSPENDIDO}/{@code VENCIDO}
 * lo bloquean (BR-014) sin eliminar ningun dato academico del tenant.
 */
public enum EstadoTenant {
  ACTIVO,
  SUSPENDIDO,
  VENCIDO
}
