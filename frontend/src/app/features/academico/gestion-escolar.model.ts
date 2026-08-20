/** DTO de respuesta de GestionEscolarController (DD-UC-008 §2). */
export interface GestionEscolarResponse {
  id: string;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  estado: 'PLANIFICACION' | 'ACTIVA' | 'CERRADA';
}

/**
 * Filtros opcionales de {@code GET /api/v1/gestiones-escolares} (DD-UC-007, patron
 * reutilizable). Campo vacio/`undefined` = sin filtro.
 */
export interface GestionEscolarFiltro {
  q?: string;
  estado?: 'PLANIFICACION' | 'ACTIVA' | 'CERRADA';
}
