package com.edusync.shared.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

/**
 * Exporta el contrato OpenAPI <b>generado desde el código</b> (springdoc) a un archivo, para
 * versionarlo como {@code openapi.json} y detectar drift en CI (Demo 2 / release 8.2.0).
 *
 * <p>No corre en {@code mvn test} normal: solo cuando se pasa {@code -Dopenapi.export.path}.
 * Requiere PostgreSQL accesible con el perfil {@code dev} (variables {@code DB_HOST},
 * {@code DB_PORT}, {@code DB_NAME}, {@code DB_USER}, {@code DB_PASSWORD}); en CI se usa un
 * service container, localmente basta {@code infra/docker-compose.yml}.
 *
 * <p>Comando: {@code mvn -B test -Dtest=OpenApiContractExportTest -Dopenapi.export.path=../openapi.json}
 * (ver {@code scripts/export-openapi.sh}).
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      // Salida determinista: claves ordenadas + pretty printer => diff estable en git.
      "springdoc.writer-with-order-by-keys=true",
      "springdoc.writer-with-default-pretty-printer=true",
      // El export no necesita IA ni tools externas.
      "edusync.ai.enabled=false"
    })
@AutoConfigureTestRestTemplate
@EnabledIfSystemProperty(named = "openapi.export.path", matches = ".+")
class OpenApiContractExportTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void exportaContratoOpenApi() throws IOException {
    ResponseEntity<String> respuesta = restTemplate.getForEntity("/v3/api-docs", String.class);

    assertThat(respuesta.getStatusCode().is2xxSuccessful()).isTrue();
    String json = respuesta.getBody();
    assertThat(json).isNotBlank();
    // Endpoints de las funcionalidades de la Demo 2 (FSD-UC-022 / DD-UC-033).
    assertThat(json)
        .contains("/api/v1/profesores/me/asignaciones")
        .contains("/api/v1/materias/{id}/planilla")
        .contains("/api/v1/evaluaciones/{id}/calificaciones");

    Path destino = Path.of(System.getProperty("openapi.export.path")).toAbsolutePath().normalize();
    if (destino.getParent() != null) {
      Files.createDirectories(destino.getParent());
    }
    Files.writeString(destino, json.stripTrailing() + "\n", StandardCharsets.UTF_8);
  }
}
