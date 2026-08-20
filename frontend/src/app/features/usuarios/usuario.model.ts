/** DTO de respuesta de UsuarioController (DD-UC-006 §2). */
export interface UsuarioResponse {
  id: string;
  nombreCompleto: string;
  email: string;
  roles: string[];
  activo: boolean;
}

/**
 * Filtros opcionales de {@code GET /api/v1/usuarios} (DD-UC-007, patron reutilizable).
 * Campo vacio/`undefined` = sin filtro.
 */
export interface UsuarioFiltro {
  q?: string;
  activo?: boolean;
  rol?: string;
}
