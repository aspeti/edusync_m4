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
