package com.edusync.academico.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Command de upsert batch de calificaciones ({@code DD-UC-018}). */
public record UpsertCalificacionesCommand(
    UUID tenantId,
    UUID actorId,
    boolean actorEsAdmin,
    UUID evaluacionId,
    List<Item> items) {

  public record Item(UUID estudianteId, BigDecimal valor) {}
}
