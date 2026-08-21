/** DTO de respuesta de CursoController sobre Curso (DD-UC-010 §2). */
export interface CursoResponse {
  id: string;
  nombre: string;
}

/** DTO de respuesta de CursoController sobre Paralelo (DD-UC-010 §2). */
export interface ParaleloResponse {
  id: string;
  cursoId: string;
  nombre: string;
}

/**
 * Filtro opcional de {@code GET /api/v1/cursos} (DD-UC-007, patron reutilizable).
 * A diferencia de {@code GestionEscolarFiltro}, solo tiene `q`: `Curso` no tiene
 * estado (DD-UC-010 §2).
 */
export interface CursoFiltro {
  q?: string;
}
