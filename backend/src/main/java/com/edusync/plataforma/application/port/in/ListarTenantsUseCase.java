package com.edusync.plataforma.application.port.in;

import com.edusync.plataforma.domain.Tenant;
import java.util.List;

/**
 * Puerto de entrada: devuelve todos los Tenants registrados.
 * Usado por la consola SysAdmin ({@code DD-UC-004} §2,
 * {@code GET /api/v1/plataforma/tenants}).
 */
public interface ListarTenantsUseCase {

  /** Retorna la lista completa de Tenants, sin filtro de estado. */
  List<Tenant> listar();
}
