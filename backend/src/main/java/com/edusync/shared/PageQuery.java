package com.edusync.shared;

/**
 * Parametros de paginacion normalizados para cualquier listado {@code GetAll} del backend
 * (patron reutilizable, ver {@code DD-UC-007}). Framework-free: vive en {@code shared} (modulo
 * {@code OPEN}, ADR-0011) para que tanto los puertos de aplicacion como los adaptadores JPA lo
 * usen sin acoplarse a {@code org.springframework.data.domain.Pageable} en la capa de
 * aplicacion.
 *
 * @param page indice de pagina, base 0
 * @param size tamano de pagina, entre 1 y {@value #TAMANO_MAXIMO}
 */
public record PageQuery(int page, int size) {

  private static final int TAMANO_POR_DEFECTO = 20;
  private static final int TAMANO_MAXIMO = 100;

  public PageQuery {
    if (page < 0) {
      throw new IllegalArgumentException("page no puede ser negativo");
    }
    if (size < 1 || size > TAMANO_MAXIMO) {
      throw new IllegalArgumentException("size debe estar entre 1 y " + TAMANO_MAXIMO);
    }
  }

  /**
   * Normaliza valores posiblemente nulos (query params opcionales de un {@code GetAll}):
   * {@code page} nulo o negativo &rarr; 0; {@code size} nulo, menor a 1 o mayor a
   * {@value #TAMANO_MAXIMO} &rarr; se clampa al rango valido.
   */
  public static PageQuery of(Integer page, Integer size) {
    int paginaNormalizada = (page == null || page < 0) ? 0 : page;
    int tamanoNormalizado =
        (size == null) ? TAMANO_POR_DEFECTO : Math.clamp(size, 1, TAMANO_MAXIMO);
    return new PageQuery(paginaNormalizada, tamanoNormalizado);
  }
}
