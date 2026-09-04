package com.edusync.academico.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record NotaProvisionalResponse(
    List<SeccionNotaResponse> secciones,
    Integer notaPeriodo,
    int promedioGestion,
    String estado) {

  public record SeccionNotaResponse(
      UUID seccionId, String nombre, String estado, BigDecimal notaSeccion) {}
}
