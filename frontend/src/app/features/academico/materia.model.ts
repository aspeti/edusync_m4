/** DTO de respuesta de MateriaController sobre Materia (DD-UC-012 §2). */
export interface MateriaResponse {
  id: string;
  nombre: string;
}

export interface AsignacionCursoResponse {
  id: string;
  materiaId: string;
  cursoId: string;
  paraleloId: string;
}

export interface AsignacionProfesorResponse {
  id: string;
  materiaId: string;
  profesorId: string;
  cursoId: string;
  paraleloId: string;
}

export interface ProfesorResumenResponse {
  id: string;
  nombreCompleto: string;
}
