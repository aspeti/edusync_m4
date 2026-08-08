/** DTO de respuesta de UsuarioController (DD-UC-006 §2). */
export interface UsuarioResponse {
  id: string;
  nombreCompleto: string;
  email: string;
  roles: string[];
  activo: boolean;
}
