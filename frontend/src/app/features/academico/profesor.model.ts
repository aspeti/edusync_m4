/** DTO de respuesta de ProfesorController (DD-UC-014 §2). */
export interface ProfesorResponse {
  id: string;
  nombreCompleto: string;
  activo: boolean;
}

/** DTO enriquecido de GET /profesores/{id}/asignaciones (DD-UC-014 §2). */
export interface AsignacionProfesorVistaResponse {
  id: string;
  materiaId: string;
  materiaNombre: string | null;
  cursoId: string;
  cursoNombre: string | null;
  paraleloId: string;
  paraleloNombre: string | null;
}
