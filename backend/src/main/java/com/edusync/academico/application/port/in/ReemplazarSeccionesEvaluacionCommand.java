package com.edusync.academico.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Reemplazo atomico de la plantilla. {@code orden} = indice 1-based del array.
 *
 * @param tenantId siempre de {@code TenantContextProvider}
 */
public record ReemplazarSeccionesEvaluacionCommand(
    UUID tenantId, UUID gestionEscolarId, List<Item> secciones) {

  public record Item(String nombre, BigDecimal nota) {}
}
