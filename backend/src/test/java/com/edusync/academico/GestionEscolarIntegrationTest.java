package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import com.edusync.shared.web.PageResponse;
import java.time.LocalDate;
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
 * Cubre el stop condition de {@code PR-IMPL-008} ({@code DD-UC-008} &sect;6): alta,
 * listado con filtros/paginacion, ciclo de estado de {@code GestionEscolar} y aislamiento
 * de tenant (404 cross-tenant, no 403).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class GestionEscolarIntegrationTest {

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
  void crudDeGestionEscolarDePuntaAPuntaConAislamientoDeTenant() {
    HttpHeaders adminTenantA = crearTenantYAutenticarAdmin("Colegio Academico A", "admin-academico-a@colegio.edu.bo");
    HttpHeaders adminTenantB = crearTenantYAutenticarAdmin("Colegio Academico B", "admin-academico-b@colegio.edu.bo");

    ResponseEntity<GestionEscolarResponse> creada = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
            adminTenantA),
        GestionEscolarResponse.class);
    assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(creada.getBody()).isNotNull();
    assertThat(creada.getBody().estado()).isEqualTo("PLANIFICACION");
    var gestionId = creada.getBody().id();

    ResponseEntity<PageResponse<GestionEscolarResponse>> lista = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantA),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(lista.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(lista.getBody()).isNotNull();
    assertThat(lista.getBody().content()).extracting(GestionEscolarResponse::id).contains(gestionId);
    assertThat(lista.getBody().page()).isZero();
    assertThat(lista.getBody().size()).isEqualTo(20);

    ResponseEntity<GestionEscolarResponse> activada = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("ACTIVA"), adminTenantA),
        GestionEscolarResponse.class);
    assertThat(activada.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(activada.getBody()).isNotNull();
    assertThat(activada.getBody().estado()).isEqualTo("ACTIVA");

    ResponseEntity<ErrorResponse> patchCrossTenant = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("CERRADA"), adminTenantB),
        ErrorResponse.class);
    assertThat(patchCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(patchCrossTenant.getBody()).isNotNull();
    assertThat(patchCrossTenant.getBody().codigo()).isEqualTo("E_GESTION_ESCOLAR_NO_ENCONTRADA");

    ResponseEntity<PageResponse<GestionEscolarResponse>> listaTenantB = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantB),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(listaTenantB.getBody()).isNotNull();
    assertThat(listaTenantB.getBody().content()).extracting(GestionEscolarResponse::id).doesNotContain(gestionId);
  }

  @Test
  void rechazaFechaFinNoPosteriorAFechaInicioCon422() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Fechas Invalidas", "admin-fechas@colegio.edu.bo");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 11, 30), LocalDate.of(2027, 2, 1)),
            adminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_FECHAS_INVALIDAS");
  }

  @Test
  void rechazaTransicionDeEstadoInvalidaCon422() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Transicion Invalida", "admin-transicion@colegio.edu.bo");
    var gestionId = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                adminHeaders),
            GestionEscolarResponse.class)
        .getBody()
        .id();

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + gestionId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("CERRADA"), adminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_ESTADO_INVALIDO");
  }

  @Test
  void listarGestionesEscolaresConFiltroQYEstadoYPaginacion() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Academico Filtros", "admin-academico-filtros@colegio.edu.bo");

    restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearGestionEscolarRequest("Gestion Norte", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
            adminHeaders),
        GestionEscolarResponse.class);
    var idActiva = restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("Gestion Sur", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                adminHeaders),
            GestionEscolarResponse.class)
        .getBody()
        .id();
    restTemplate.exchange(
        "/api/v1/gestiones-escolares/" + idActiva + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoGestionEscolarRequest("ACTIVA"), adminHeaders),
        GestionEscolarResponse.class);

    ResponseEntity<PageResponse<GestionEscolarResponse>> porNombre = restTemplate.exchange(
        "/api/v1/gestiones-escolares?q=norte",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(porNombre.getBody()).isNotNull();
    assertThat(porNombre.getBody().content()).extracting(GestionEscolarResponse::nombre)
        .containsExactly("Gestion Norte");

    ResponseEntity<PageResponse<GestionEscolarResponse>> porEstado = restTemplate.exchange(
        "/api/v1/gestiones-escolares?estado=ACTIVA",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(porEstado.getBody()).isNotNull();
    assertThat(porEstado.getBody().content()).extracting(GestionEscolarResponse::id).contains(idActiva);
    assertThat(porEstado.getBody().content()).allSatisfy(g -> assertThat(g.estado()).isEqualTo("ACTIVA"));

    ResponseEntity<PageResponse<GestionEscolarResponse>> paginado = restTemplate.exchange(
        "/api/v1/gestiones-escolares?page=0&size=1",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<GestionEscolarResponse>>() {});
    assertThat(paginado.getBody()).isNotNull();
    assertThat(paginado.getBody().content()).hasSize(1);
    assertThat(paginado.getBody().totalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void gestionesEscolaresSinTokenDevuelve401() {
    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void gestionesEscolaresConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/gestiones-escolares",
        HttpMethod.GET,
        new HttpEntity<>(sysAdminHeaders),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
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
