/** DTOs de SeccionEvaluacion (DD-UC-016 / DD-UC-034 / FSD-UC-014). */
export interface SeccionEvaluacionResponse {
  id: string;
  gestionEscolarId: string;
  nombre: string;
  orden: number;
  nota: number;
  rangoMin: number;
  rangoMax: number;
}

export interface SeccionEvaluacionDraft {
  nombre: string;
  nota: number | null;
  rangoMin: number | null;
  rangoMax: number | null;
}
