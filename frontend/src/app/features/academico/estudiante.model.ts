/** DTO de respuesta de EstudianteController (DD-UC-013 §2). */
export interface EstudianteResponse {
  id: string;
  rude: string;
  nombreCompleto: string;
  estado: 'ACTIVO' | 'INACTIVO';
  datosPersonales: Record<string, string> | null;
}

/** DTO de respuesta de InscripcionController / GET historial (DD-UC-013 §2). */
export interface InscripcionResponse {
  id: string;
  estudianteId: string;
  gestionEscolarId: string;
  gestionNombre?: string | null;
  cursoId: string;
  cursoNombre?: string | null;
  paraleloId: string;
  paraleloNombre?: string | null;
  fechaInscripcion: string;
  estado: 'ACTIVA' | 'RETIRADA' | 'TRANSFERIDA';
}
