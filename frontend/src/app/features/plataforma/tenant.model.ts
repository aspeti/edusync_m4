/** DTO de respuesta de TenantController (DD-UC-004 §2). */
export interface TenantResponse {
  id: string;
  nombre: string;
  fechaInicioSuscripcion: string;
  fechaVencimientoSuscripcion: string;
  estado: 'ACTIVO' | 'SUSPENDIDO' | 'VENCIDO';
}

/** DTO de respuesta al crear el primer ADMIN de un Tenant. */
export interface AdminCreadoResponse {
  usuarioId: string;
  email: string;
}

/**
 * Filtros opcionales de {@code GET /api/v1/plataforma/tenants} (DD-UC-007, patron
 * reutilizable). Campo vacio/`undefined` = sin filtro.
 */
export interface TenantFiltro {
  q?: string;
  estado?: 'ACTIVO' | 'SUSPENDIDO' | 'VENCIDO';
}
