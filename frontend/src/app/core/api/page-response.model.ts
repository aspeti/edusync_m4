/**
 * Envelope generico de paginacion para cualquier listado {@code GetAll} (patron reutilizable,
 * DD-UC-007). Refleja {@code com.edusync.shared.web.PageResponse} del backend.
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const PAGE_SIZE_POR_DEFECTO = 20;
