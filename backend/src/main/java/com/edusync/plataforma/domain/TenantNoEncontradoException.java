package com.edusync.plataforma.domain;

import com.edusync.shared.exception.DomainException;
import java.util.UUID;

/** Se lanza cuando se referencia un {@link Tenant} inexistente (alta de admin, cambio de estado). */
public class TenantNoEncontradoException extends DomainException {

  public TenantNoEncontradoException(UUID tenantId) {
    super("E_TENANT_NO_ENCONTRADO", "No existe un Tenant con id: " + tenantId);
  }
}
