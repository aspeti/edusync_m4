/** DTOs de FSD-UC-022 / DD-UC-033 — planilla Profesor. */

export interface MisAsignacionResponse {
  asignacionId: string;
  materiaId: string;
  materiaNombre: string;
  cursoId: string;
  cursoNombre: string;
  paraleloId: string;
  paraleloNombre: string;
  cursoEtiqueta: string;
}

export interface PlanillaColumna {
  evaluacionId: string;
  nombre: string;
  seccionEvaluacionId: string;
  seccionNombre: string | null;
  seccionOrden: number;
  puntajeMaximo: number;
  fecha: string;
}

export interface PlanillaFila {
  estudianteId: string;
  nombreCompleto: string;
  rude: string;
  /** Claves = evaluacionId; omitidas = sin nota. */
  notas: Record<string, number>;
  notaPeriodo: number | null;
}

export interface PlanillaMateriaResponse {
  materiaId: string;
  materiaNombre: string;
  cursoId: string;
  cursoNombre: string;
  paraleloId: string;
  paraleloNombre: string;
  cursoEtiqueta: string;
  periodoId: string;
  periodoNombre: string;
  periodoEstado: string;
  editable: boolean;
  columnas: PlanillaColumna[];
  filas: PlanillaFila[];
}
