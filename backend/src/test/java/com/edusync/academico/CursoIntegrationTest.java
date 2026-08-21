package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import com.edusync.shared.web.PageResponse;
import java.time.LocalDate;
import java.util.List;
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
 * Cubre el stop condition de {@code PR-IMPL-010} ({@code DD-UC-010} &sect;1.5): alta y
 * listado de Cursos y Paralelos, validacion del curso padre, y aislamiento de tenant
 * (404 cross-tenant, no 403).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class CursoIntegrationTest {

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
  void altaYListadoDeCursosYParalelosConAislamientoDeTenant() {
    HttpHeaders adminTenantA = crearTenantYAutenticarAdmin("Colegio Cursos A", "admin-cursos-a@colegio.edu.bo");
    HttpHeaders adminTenantB = crearTenantYAutenticarAdmin("Colegio Cursos B", "admin-cursos-b@colegio.edu.bo");

    ResponseEntity<CursoResponse> cursoCreado = restTemplate.exchange(
        "/api/v1/cursos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearCursoRequest("Primero de Primaria"), adminTenantA),
        CursoResponse.class);
    assertThat(cursoCreado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(cursoCreado.getBody()).isNotNull();
    var cursoId = cursoCreado.getBody().id();

    ResponseEntity<PageResponse<CursoResponse>> listaCursos = restTemplate.exchange(
        "/api/v1/cursos",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantA),
        new ParameterizedTypeReference<PageResponse<CursoResponse>>() {});
    assertThat(listaCursos.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listaCursos.getBody()).isNotNull();
    assertThat(listaCursos.getBody().content()).extracting(CursoResponse::id).contains(cursoId);

    ResponseEntity<ParaleloResponse> paraleloCreado = restTemplate.exchange(
        "/api/v1/cursos/" + cursoId + "/paralelos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearParaleloRequest("A"), adminTenantA),
        ParaleloResponse.class);
    assertThat(paraleloCreado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(paraleloCreado.getBody()).isNotNull();
    assertThat(paraleloCreado.getBody().cursoId()).isEqualTo(cursoId);

    ResponseEntity<List<ParaleloResponse>> listaParalelos = restTemplate.exchange(
        "/api/v1/cursos/" + cursoId + "/paralelos",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantA),
        new ParameterizedTypeReference<List<ParaleloResponse>>() {});
    assertThat(listaParalelos.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listaParalelos.getBody()).extracting(ParaleloResponse::nombre).containsExactly("A");

    ResponseEntity<ErrorResponse> paraleloCrossTenant = restTemplate.exchange(
        "/api/v1/cursos/" + cursoId + "/paralelos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearParaleloRequest("B"), adminTenantB),
        ErrorResponse.class);
    assertThat(paraleloCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(paraleloCrossTenant.getBody()).isNotNull();
    assertThat(paraleloCrossTenant.getBody().codigo()).isEqualTo("E_CURSO_NO_ENCONTRADO");

    ResponseEntity<ErrorResponse> listaParalelosCrossTenant = restTemplate.exchange(
        "/api/v1/cursos/" + cursoId + "/paralelos",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantB),
        ErrorResponse.class);
    assertThat(listaParalelosCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

    ResponseEntity<PageResponse<CursoResponse>> listaCursosTenantB = restTemplate.exchange(
        "/api/v1/cursos",
        HttpMethod.GET,
        new HttpEntity<>(adminTenantB),
        new ParameterizedTypeReference<PageResponse<CursoResponse>>() {});
    assertThat(listaCursosTenantB.getBody()).isNotNull();
    assertThat(listaCursosTenantB.getBody().content()).extracting(CursoResponse::id).doesNotContain(cursoId);
  }

  @Test
  void listarCursosConFiltroQYPaginacion() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Cursos Filtros", "admin-cursos-filtros@colegio.edu.bo");

    restTemplate.exchange(
        "/api/v1/cursos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearCursoRequest("Primero de Primaria"), adminHeaders),
        CursoResponse.class);
    restTemplate.exchange(
        "/api/v1/cursos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearCursoRequest("Segundo de Primaria"), adminHeaders),
        CursoResponse.class);

    ResponseEntity<PageResponse<CursoResponse>> porNombre = restTemplate.exchange(
        "/api/v1/cursos?q=primero",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<CursoResponse>>() {});
    assertThat(porNombre.getBody()).isNotNull();
    assertThat(porNombre.getBody().content()).extracting(CursoResponse::nombre).containsExactly("Primero de Primaria");

    ResponseEntity<PageResponse<CursoResponse>> paginado = restTemplate.exchange(
        "/api/v1/cursos?page=0&size=1",
        HttpMethod.GET,
        new HttpEntity<>(adminHeaders),
        new ParameterizedTypeReference<PageResponse<CursoResponse>>() {});
    assertThat(paginado.getBody()).isNotNull();
    assertThat(paginado.getBody().content()).hasSize(1);
    assertThat(paginado.getBody().totalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void crearParaleloSobreCursoInexistenteDevuelve404() {
    HttpHeaders adminHeaders = crearTenantYAutenticarAdmin("Colegio Curso Inexistente", "admin-curso-inexistente@colegio.edu.bo");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/cursos/" + java.util.UUID.randomUUID() + "/paralelos",
        HttpMethod.POST,
        new HttpEntity<>(new CrearParaleloRequest("A"), adminHeaders),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_CURSO_NO_ENCONTRADO");
  }

  @Test
  void cursosSinTokenDevuelve401() {
    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/cursos", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void cursosConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/cursos", HttpMethod.GET, new HttpEntity<>(sysAdminHeaders), String.class);

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
