/** DTOs de PeriodoEvaluacion (DD-UC-015 / FSD-UC-013). */
export interface PeriodoEvaluacionResponse {
  id: string;
  gestionEscolarId: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  orden: number;
  estado: 'PENDIENTE' | 'ABIERTO' | 'CERRADO';
}
