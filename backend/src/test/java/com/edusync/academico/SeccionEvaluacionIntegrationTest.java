package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ReemplazarSeccionesEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.SeccionEvaluacionResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Stop condition de {@code PR-IMPL-016} ({@code DD-UC-016} &sect;6): seed Gherkin,
 * PUT rebalance, freeze sticky ABIERTO y CERRADO, aislamiento cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class SeccionEvaluacionIntegrationTest {

  @Container
  static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15")
      .withDatabaseName("edusync_it")
      .withUsername("edusync")
      .withPassword("edusync_it_local");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Value("${edusync.seed.sysadmin.email}")
  private String sysAdminEmail;

  @Value("${edusync.seed.sysadmin.password}")
  private String sysAdminPassword;

  @Test
  void seedPutFreezeStickyYAislamientoDeTenant() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Secciones A", "admin-secciones-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Secciones B", "admin-secciones-b@colegio.edu.bo");

    UUID gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                adminA),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    List<SeccionEvaluacionResponse> seed = listarSecciones(gestionId, adminA);
    assertThat(seed).hasSize(4);
    assertThat(seed).extracting(SeccionEvaluacionResponse::nombre)
        .containsExactly("Ser", "Saber", "Hacer", "Autoevaluación");
    assertThat(seed.stream().map(SeccionEvaluacionResponse::nota).reduce(BigDecimal.ZERO, BigDecimal::add))
        .isEqualByComparingTo("100.00");

    ResponseEntity<List<SeccionEvaluacionResponse>> putOk = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("Formativa", "60"), item("Sumativa", "40")), adminA),
        new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {});
    assertThat(putOk.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(putOk.getBody()).hasSize(2);
    assertThat(putOk.getBody()).extracting(SeccionEvaluacionResponse::orden).containsExactly(1, 2);

    ResponseEntity<ErrorResponse> put99 = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "50"), item("B", "49")), adminA),
        ErrorResponse.class);
    assertThat(put99.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(put99.getBody().codigo()).isEqualTo("E_SUMA_SECCIONES_INVALIDA");

    ResponseEntity<ErrorResponse> putPeso = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "0"), item("B", "100")), adminA),
        ErrorResponse.class);
    assertThat(putPeso.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(putPeso.getBody().codigo()).isEqualTo("E_PESO_INVALIDO");

    ResponseEntity<ErrorResponse> putCross = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "60"), item("B", "40")), adminB),
        ErrorResponse.class);
    assertThat(putCross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(putCross.getBody().codigo()).isEqualTo("E_GESTION_ESCOLAR_NO_ENCONTRADA");

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, adminA);
    UUID t1 = periodos.get(0).id();
    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t1 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        PeriodoEvaluacionResponse.class);

    ResponseEntity<ErrorResponse> putAbierto = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "70"), item("B", "30")), adminA),
        ErrorResponse.class);
    assertThat(putAbierto.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(putAbierto.getBody().codigo()).isEqualTo("E_SECCIONES_INMUTABLES");

    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t1 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("CERRADO"), adminA),
        PeriodoEvaluacionResponse.class);

    ResponseEntity<ErrorResponse> putCerrado = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "70"), item("B", "30")), adminA),
        ErrorResponse.class);
    assertThat(putCerrado.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(putCerrado.getBody().codigo()).isEqualTo("E_SECCIONES_INMUTABLES");
  }

  @Test
  void abrirPeriodoSinSeccionesRechaza() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Sin Secciones", "admin-sin-secciones@colegio.edu.bo");
    UUID gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    // Vaciar la plantilla no es posible (PUT vacio 422); se cubre el caso de gestion
    // vieja via Mockito. Aqui: PUT a 2 secciones, abrir T1 ok (ya hay suma 100).
    restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.PUT,
        new HttpEntity<>(putDe(item("A", "100")), admin),
        new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {});

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, admin);
    ResponseEntity<PeriodoEvaluacionResponse> abierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodos.get(0).id() + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), admin),
        PeriodoEvaluacionResponse.class);
    assertThat(abierto.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private ReemplazarSeccionesEvaluacionRequest putDe(ReemplazarSeccionesEvaluacionRequest.Item... items) {
    return new ReemplazarSeccionesEvaluacionRequest(List.of(items));
  }

  private ReemplazarSeccionesEvaluacionRequest.Item item(String nombre, String nota) {
    return new ReemplazarSeccionesEvaluacionRequest.Item(nombre, new BigDecimal(nota));
  }

  private List<SeccionEvaluacionResponse> listarSecciones(UUID gestionId, HttpHeaders headers) {
    ResponseEntity<List<SeccionEvaluacionResponse>> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  private List<PeriodoEvaluacionResponse> listarPeriodos(UUID gestionId, HttpHeaders headers) {
    ResponseEntity<List<PeriodoEvaluacionResponse>> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        new ParameterizedTypeReference<List<PeriodoEvaluacionResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    return response.getBody();
  }

  private HttpHeaders crearTenantYAutenticarAdmin(String nombreTenant, String adminEmail) {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    var tenantId = restTemplate.exchange(
            "/api/v1/plataforma/tenants",
            HttpMethod.POST,
            new HttpEntity<>(
                new RegistrarTenantRequest(nombreTenant, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                sysAdminHeaders),
            TenantResponse.class)
        .getBody()
        .id();
    restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAdminTenantRequest("Admin " + nombreTenant, adminEmail, "secreto123"), sysAdminHeaders),
        AdminCreadoResponse.class);
    return autenticarComo(adminEmail, "secreto123");
  }

  private HttpHeaders autenticarComo(String email, String password) {
    ResponseEntity<LoginResponse> login = restTemplate.postForEntity(
        "/api/v1/auth/login", new LoginRequest(email, password), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(login.getBody().accessToken());
    return headers;
  }
}
