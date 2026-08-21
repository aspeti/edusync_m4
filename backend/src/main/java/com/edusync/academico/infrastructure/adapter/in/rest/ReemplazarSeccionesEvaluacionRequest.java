package com.edusync.academico.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record ReemplazarSeccionesEvaluacionRequest(
    @Schema(description = "Plantilla completa; orden = posicion 1-based del array")
    @NotNull(message = "secciones es obligatorio")
    @Valid
    List<Item> secciones) {

  public record Item(
      @NotBlank(message = "nombre es obligatorio") String nombre,
      @NotNull(message = "nota es obligatoria") BigDecimal nota) {
  }
}
