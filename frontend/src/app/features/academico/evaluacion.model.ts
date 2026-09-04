/** DTOs de Evaluacion (DD-UC-017 / FSD-UC-015). */
export interface EvaluacionResponse {
  id: string;
  materiaId: string;
  periodoEvaluacionId: string;
  seccionEvaluacionId: string;
  nombre: string;
  fecha: string;
  puntajeMaximo: number;
  descripcion: string | null;
  estado: 'ACTIVA' | 'ANULADA';
}
