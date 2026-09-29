package com.edusync.academico.infrastructure.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GuardarParametrosPeriodoRequest(@NotNull @Valid List<Item> parametros) {

  public record Item(
      @NotNull UUID seccionEvaluacionId,
      @NotNull BigDecimal rangoMin,
      @NotNull BigDecimal rangoMax,
      @NotNull String reglaCombinacion) {}
}
