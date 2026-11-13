package com.edusync.academico.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpsertCalificacionesRequest(@NotEmpty @Valid List<Item> items) {

  /**
   * {@code valor} nullable: {@code null} borra la calificación de la celda ({@code DD-UC-033}).
   */
  public record Item(@NotNull UUID estudianteId, BigDecimal valor) {}
}
