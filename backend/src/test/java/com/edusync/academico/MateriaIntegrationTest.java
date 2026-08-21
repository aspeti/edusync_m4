package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ProfesorResumenResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.CrearUsuarioRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.UsuarioResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import com.edusync.shared.web.PageResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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
 * Cubre el stop condition de {@code PR-IMPL-012} ({@code DD-UC-012} &sect;1.5): alta y
 * listado de Materias, asignaciones curso/profesor, A1 409, GET por id, y aislamiento
 * cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class MateriaIntegrationTest {

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
  void altaAsignacionesYAislamientoDeTenant() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Materias A", "admin-materias-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Materias B", "admin-materias-b@colegio.edu.bo");

    UUID cursoId = crearCurso(adminA, "Primero de Primaria");
    UUID paraleloId = crearParalelo(adminA, cursoId, "A");
    UUID profesorId = crearProfesor(adminA, "Profesor A", "profesor-a@colegio.edu.bo");

    ResponseEntity<MateriaResponse> materiaCreada = restTemplate.exchange(
        "/api/v1/materias",
        HttpMethod.POST,
        new HttpEntity<>(new CrearMateriaRequest("Matemáticas"), adminA),
        MateriaResponse.class);
    assertThat(materiaCreada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(materiaCreada.getBody()).isNotNull();
    UUID materiaId = materiaCreada.getBody().id();

    ResponseEntity<MateriaResponse> detalle = restTemplate.exchange(
        "/api/v1/materias/" + materiaId,
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        MateriaResponse.class);
    assertThat(detalle.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(detalle.getBody().nombre()).isEqualTo("Matemáticas");

    ResponseEntity<PageResponse<MateriaResponse>> lista = restTemplate.exchange(
        "/api/v1/materias",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<PageResponse<MateriaResponse>>() {});
    assertThat(lista.getBody().content()).extracting(MateriaResponse::id).contains(materiaId);

    ResponseEntity<AsignacionCursoResponse> asignacionCurso = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-curso",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionCursoRequest(cursoId, paraleloId), adminA),
        AsignacionCursoResponse.class);
    assertThat(asignacionCurso.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<AsignacionProfesorResponse> asignacionProfesor = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), adminA),
        AsignacionProfesorResponse.class);
    assertThat(asignacionProfesor.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(asignacionProfesor.getBody().profesorId()).isEqualTo(profesorId);

    ResponseEntity<List<ProfesorResumenResponse>> catalogo = restTemplate.exchange(
        "/api/v1/materias/profesores-disponibles",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<List<ProfesorResumenResponse>>() {});
    assertThat(catalogo.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(catalogo.getBody()).extracting(ProfesorResumenResponse::id).contains(profesorId);

    ResponseEntity<ErrorResponse> detalleCrossTenant = restTemplate.exchange(
        "/api/v1/materias/" + materiaId,
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(detalleCrossTenant.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(detalleCrossTenant.getBody().codigo()).isEqualTo("E_MATERIA_NO_ENCONTRADA");

    ResponseEntity<PageResponse<MateriaResponse>> listaTenantB = restTemplate.exchange(
        "/api/v1/materias",
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        new ParameterizedTypeReference<PageResponse<MateriaResponse>>() {});
    assertThat(listaTenantB.getBody().content()).extracting(MateriaResponse::id).doesNotContain(materiaId);
  }

  @Test
  void asignarProfesorSinAsignacionCursoDevuelve409() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Materias A1", "admin-materias-a1@colegio.edu.bo");
    UUID cursoId = crearCurso(admin, "Primero");
    UUID paraleloId = crearParalelo(admin, cursoId, "A");
    UUID profesorId = crearProfesor(admin, "Profesor A1", "profesor-a1@colegio.edu.bo");
    UUID materiaId = crearMateria(admin, "Lenguaje");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().codigo()).isEqualTo("E_MATERIA_SIN_CURSO");
  }

  @Test
  void listarMateriasConFiltroQYPaginacion() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Materias Filtros", "admin-materias-filtros@colegio.edu.bo");
    crearMateria(admin, "Matemáticas");
    crearMateria(admin, "Lenguaje");

    ResponseEntity<PageResponse<MateriaResponse>> porNombre = restTemplate.exchange(
        "/api/v1/materias?q=mate",
        HttpMethod.GET,
        new HttpEntity<>(admin),
        new ParameterizedTypeReference<PageResponse<MateriaResponse>>() {});
    assertThat(porNombre.getBody().content()).extracting(MateriaResponse::nombre).containsExactly("Matemáticas");

    ResponseEntity<PageResponse<MateriaResponse>> paginado = restTemplate.exchange(
        "/api/v1/materias?page=0&size=1",
        HttpMethod.GET,
        new HttpEntity<>(admin),
        new ParameterizedTypeReference<PageResponse<MateriaResponse>>() {});
    assertThat(paginado.getBody().content()).hasSize(1);
    assertThat(paginado.getBody().totalElements()).isGreaterThanOrEqualTo(2);
  }

  @Test
  void obtenerMateriaInexistenteDevuelve404() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Materia Inexistente", "admin-materia-inexistente@colegio.edu.bo");

    ResponseEntity<ErrorResponse> response = restTemplate.exchange(
        "/api/v1/materias/" + UUID.randomUUID(),
        HttpMethod.GET,
        new HttpEntity<>(admin),
        ErrorResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().codigo()).isEqualTo("E_MATERIA_NO_ENCONTRADA");
  }

  @Test
  void materiasSinTokenDevuelve401() {
    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/materias", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void materiasConRolSysAdminDevuelve403() {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/materias", HttpMethod.GET, new HttpEntity<>(sysAdminHeaders), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private UUID crearMateria(HttpHeaders admin, String nombre) {
    return restTemplate.exchange(
            "/api/v1/materias",
            HttpMethod.POST,
            new HttpEntity<>(new CrearMateriaRequest(nombre), admin),
            MateriaResponse.class)
        .getBody()
        .id();
  }

  private UUID crearCurso(HttpHeaders admin, String nombre) {
    return restTemplate.exchange(
            "/api/v1/cursos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearCursoRequest(nombre), admin),
            CursoResponse.class)
        .getBody()
        .id();
  }

  private UUID crearParalelo(HttpHeaders admin, UUID cursoId, String nombre) {
    return restTemplate.exchange(
            "/api/v1/cursos/" + cursoId + "/paralelos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearParaleloRequest(nombre), admin),
            ParaleloResponse.class)
        .getBody()
        .id();
  }

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate.exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
        .getBody()
        .id();
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
