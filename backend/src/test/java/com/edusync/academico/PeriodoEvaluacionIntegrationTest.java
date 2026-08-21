package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.ActualizarPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
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
 * Stop condition de {@code PR-IMPL-015} ({@code DD-UC-015} &sect;6): seed, secuencia,
 * freeze, Gherkin N=2 y aislamiento cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class PeriodoEvaluacionIntegrationTest {

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
  void seedSecuenciaFreezeYAislamientoDeTenant() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Periodos A", "admin-periodos-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Periodos B", "admin-periodos-b@colegio.edu.bo");

    ResponseEntity<GestionEscolarResponse> creada = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
            adminA),
        GestionEscolarResponse.class);
    assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    UUID gestionId = creada.getBody().id();

    ResponseEntity<GestionEscolarResponse> detalle = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId,
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        GestionEscolarResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detalle.getBody().nombre()).isEqualTo("2027");

    ResponseEntity<GestionEscolarResponse> detalleCross = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId,
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        GestionEscolarResponse.class);
    assertThat(detalleCross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, adminA);
    assertThat(periodos).hasSize(3);
    assertThat(periodos).extracting(PeriodoEvaluacionResponse::nombre)
        .containsExactly("Trimestre 1", "Trimestre 2", "Trimestre 3");
    assertThat(periodos).allSatisfy(p -> assertThat(p.estado()).isEqualTo("PENDIENTE"));

    UUID t1 = periodos.get(0).id();
    UUID t2 = periodos.get(1).id();
    UUID t3 = periodos.get(2).id();

    ResponseEntity<PeriodoEvaluacionResponse> t1Abierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t1 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        PeriodoEvaluacionResponse.class);
    assertThat(t1Abierto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(t1Abierto.getBody().estado()).isEqualTo("ABIERTO");

    ResponseEntity<ErrorResponse> t2Prematuro = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t2 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        ErrorResponse.class);
    assertThat(t2Prematuro.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(t2Prematuro.getBody().codigo()).isEqualTo("E_PERIODO_NO_SECUENCIAL");

    ResponseEntity<ErrorResponse> postConAbierto = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearPeriodoEvaluacionRequest("Extra", LocalDate.of(2027, 12, 1), LocalDate.of(2027, 12, 15)),
            adminA),
        ErrorResponse.class);
    assertThat(postConAbierto.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(postConAbierto.getBody().codigo()).isEqualTo("E_PERIODOS_INMUTABLES");

    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t1 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("CERRADO"), adminA),
        PeriodoEvaluacionResponse.class);

    ResponseEntity<PeriodoEvaluacionResponse> t2Abierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t2 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        PeriodoEvaluacionResponse.class);
    assertThat(t2Abierto.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<ErrorResponse> patchCross = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t3 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminB),
        ErrorResponse.class);
    assertThat(patchCross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(patchCross.getBody().codigo()).isEqualTo("E_PERIODO_NO_ENCONTRADO");
  }

  @Test
  void dejaDosBimestresEnLugarDeTresTrimestres() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Bimestres", "admin-bimestres@colegio.edu.bo");
    UUID gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    List<PeriodoEvaluacionResponse> seed = listarPeriodos(gestionId, admin);
    UUID t3 = seed.get(2).id();
    ResponseEntity<Void> borrado = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t3,
        HttpMethod.DELETE,
        new HttpEntity<>(admin),
        Void.class);
    assertThat(borrado.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + seed.get(1).id(),
        HttpMethod.PATCH,
        new HttpEntity<>(
            new ActualizarPeriodoEvaluacionRequest("Bimestre 2", LocalDate.of(2027, 7, 1), LocalDate.of(2027, 11, 30)),
            admin),
        PeriodoEvaluacionResponse.class);
    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + seed.get(0).id(),
        HttpMethod.PATCH,
        new HttpEntity<>(
            new ActualizarPeriodoEvaluacionRequest("Bimestre 1", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 6, 30)),
            admin),
        PeriodoEvaluacionResponse.class);

    List<PeriodoEvaluacionResponse> restantes = listarPeriodos(gestionId, admin);
    assertThat(restantes).hasSize(2);
    assertThat(restantes).extracting(PeriodoEvaluacionResponse::nombre)
        .containsExactly("Bimestre 1", "Bimestre 2");
    assertThat(restantes).extracting(PeriodoEvaluacionResponse::orden).containsExactly(1, 2);
  }

  @Test
  void rechazaSolapeYEliminarElUltimo() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Solape", "admin-solape@colegio.edu.bo");
    UUID gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    List<PeriodoEvaluacionResponse> seed = listarPeriodos(gestionId, admin);
    ResponseEntity<ErrorResponse> solape = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearPeriodoEvaluacionRequest("Choque", seed.get(0).fechaInicio(), seed.get(0).fechaFin()),
            admin),
        ErrorResponse.class);
    assertThat(solape.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(solape.getBody().codigo()).isEqualTo("E_PERIODOS_SOLAPADOS");

    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + seed.get(2).id(),
        HttpMethod.DELETE,
        new HttpEntity<>(admin),
        Void.class);
    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + seed.get(1).id(),
        HttpMethod.DELETE,
        new HttpEntity<>(admin),
        Void.class);

    ResponseEntity<ErrorResponse> ultimo = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + seed.get(0).id(),
        HttpMethod.DELETE,
        new HttpEntity<>(admin),
        ErrorResponse.class);
    assertThat(ultimo.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(ultimo.getBody().codigo()).isEqualTo("E_PERIODO_UNICO");
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
