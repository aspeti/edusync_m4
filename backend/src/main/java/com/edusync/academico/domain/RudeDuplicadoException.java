package com.edusync.academico.domain;

import com.edusync.shared.exception.DomainException;

/**
 * Conflicto de unicidad {@code (tenant_id, rude)} ({@code DD-UC-013} &sect;2). HTTP 409
 * {@code E_RUDE_DUPLICADO}. El mensaje <strong>no</strong> interpola el codigo RUDE
 * ({@code AGENTS.md} &sect;7).
 */
public class RudeDuplicadoException extends DomainException {

  public RudeDuplicadoException() {
    super("E_RUDE_DUPLICADO", "Ya existe un estudiante con ese codigo RUDE en esta institucion");
  }
}
