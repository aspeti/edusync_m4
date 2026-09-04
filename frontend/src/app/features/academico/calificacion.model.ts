/** DTOs de calificaciones y nota provisional (DD-UC-018 / FSD-UC-016). */

export interface CalificacionFilaResponse {
  estudianteId: string;
  nombreCompleto: string;
  rude: string;
  valor: number | null;
}

export interface CalificacionResponse {
  id: string;
  evaluacionId: string;
  estudianteId: string;
  valor: number;
}

export interface UpsertCalificacionesRequest {
  items: { estudianteId: string; valor: number }[];
}

export interface SeccionNotaResponse {
  seccionId: string;
  nombre: string;
  estado: 'COMPLETO' | 'INCOMPLETO';
  notaSeccion: number | null;
}

export interface NotaProvisionalResponse {
  secciones: SeccionNotaResponse[];
  notaPeriodo: number | null;
  promedioGestion: number;
  estado: string;
}
