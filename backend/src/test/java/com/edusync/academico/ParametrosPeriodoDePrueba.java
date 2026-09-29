package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.GuardarParametrosPeriodoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.ParametroPeriodoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.SeccionEvaluacionResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * Arma el conjunto minimo de {@code ParametroPeriodo} que exige abrir un periodo
 * ({@code DD-UC-029}): una fila por seccion vigente, rango {@code 0..nota}, regla
 * {@code PROMEDIO_SIMPLE}. No hay seed automatico al crear la gestion.
 */
public final class ParametrosPeriodoDePrueba {

  private ParametrosPeriodoDePrueba() {}

  public static void configurarPorDefecto(
      TestRestTemplate restTemplate, HttpHeaders headers, UUID gestionId, UUID periodoId) {
    ResponseEntity<List<SeccionEvaluacionResponse>> secciones = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<>() {});
    assertThat(secciones.getStatusCode().is2xxSuccessful()).isTrue();
    List<GuardarParametrosPeriodoRequest.Item> items = secciones.getBody().stream()
        .map(seccion -> new GuardarParametrosPeriodoRequest.Item(
            seccion.id(), BigDecimal.ZERO, seccion.nota(), "PROMEDIO_SIMPLE"))
        .toList();
    ResponseEntity<List<ParametroPeriodoResponse>> guardado = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/parametros",
        HttpMethod.PUT,
        new HttpEntity<>(new GuardarParametrosPeriodoRequest(items), headers),
        new ParameterizedTypeReference<>() {});
    assertThat(guardado.getStatusCode().is2xxSuccessful()).isTrue();
  }
}
