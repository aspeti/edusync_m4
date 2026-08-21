/** DTOs de SeccionEvaluacion (DD-UC-016 / FSD-UC-014). */
export interface SeccionEvaluacionResponse {
  id: string;
  gestionEscolarId: string;
  nombre: string;
  orden: number;
  nota: number;
}

export interface SeccionEvaluacionDraft {
  nombre: string;
  nota: number | null;
}
