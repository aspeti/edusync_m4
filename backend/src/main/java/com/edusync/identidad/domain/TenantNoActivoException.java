package com.edusync.identidad.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Se lanza al intentar iniciar sesion con un usuario cuyo Tenant esta {@code SUSPENDIDO}
 * o {@code VENCIDO} ({@code BR-014}, {@code FSD-UC-011}, flujo alternativo A2). Ningun
 * dato academico se elimina; solo se bloquea el acceso.
 */
public class TenantNoActivoException extends DomainException {

  public TenantNoActivoException() {
    super("E_TENANT_NO_ACTIVO", "El Tenant de este usuario esta SUSPENDIDO o VENCIDO (BR-014)");
  }
}
