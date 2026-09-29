package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GuardarParametrosPeriodoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParametroPeriodoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.SeccionEvaluacionResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.CrearUsuarioRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginRequest;
import com.edusync.identidad.infrastructure.adapter.in.rest.LoginResponse;
import com.edusync.identidad.infrastructure.adapter.in.rest.UsuarioResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.AdminCreadoResponse;
import com.edusync.plataforma.infrastructure.adapter.in.rest.CrearAdminTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.RegistrarTenantRequest;
import com.edusync.plataforma.infrastructure.adapter.in.rest.TenantResponse;
import java.math.BigDecimal;
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
 * {@code DD-UC-029}: parametros por seccion, inmutables fuera de {@code PENDIENTE},
 * y cobertura docente al abrir. Sin seed automatico.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class ParametroPeriodoIntegrationTest {

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
  void putGetAbrirYPutPosteriorInmutable() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Param A", "admin-param-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Param B", "admin-param-b@colegio.edu.bo");
    UUID gestionId = crearGestion(adminA);
    UUID periodoId = listarPeriodos(gestionId, adminA).get(0).id();

    ResponseEntity<ErrorResponse> sinParametros = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        ErrorResponse.class);
    assertThat(sinParametros.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(sinParametros.getBody().codigo()).isEqualTo("E_PARAMETROS_INCOMPLETOS");

    ParametrosPeriodoDePrueba.configurarPorDefecto(restTemplate, adminA, gestionId, periodoId);
    ResponseEntity<List<ParametroPeriodoResponse>> listado = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/parametros",
        HttpMethod.GET,
        new HttpEntity<>(adminA),
        new ParameterizedTypeReference<>() {});
    assertThat(listado.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listado.getBody()).hasSize(4);
    assertThat(listado.getBody()).allSatisfy(p -> assertThat(p.reglaCombinacion()).isEqualTo("PROMEDIO_SIMPLE"));

    ResponseEntity<ErrorResponse> cross = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/parametros",
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(cross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(cross.getBody().codigo()).isEqualTo("E_PERIODO_NO_ENCONTRADO");

    ResponseEntity<PeriodoEvaluacionResponse> abierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        PeriodoEvaluacionResponse.class);
    assertThat(abierto.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<SeccionEvaluacionResponse> secciones = listarSecciones(gestionId, adminA);
    ResponseEntity<ErrorResponse> putAbierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/parametros",
        HttpMethod.PUT,
        new HttpEntity<>(
            new GuardarParametrosPeriodoRequest(secciones.stream()
                .map(seccion -> new GuardarParametrosPeriodoRequest.Item(
                    seccion.id(), BigDecimal.ZERO, seccion.nota(), "PROMEDIO_SIMPLE"))
                .toList()),
            adminA),
        ErrorResponse.class);
    assertThat(putAbierto.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(putAbierto.getBody().codigo()).isEqualTo("E_PARAMETROS_INMUTABLES");
  }

  @Test
  void materiaConCursoSinProfesorBloqueaLaApertura() {
    HttpHeaders admin = crearTenantYAutenticarAdmin("Colegio Param Docente", "admin-param-docente@colegio.edu.bo");
    UUID gestionId = crearGestion(admin);
    UUID periodoId = listarPeriodos(gestionId, admin).get(0).id();
    UUID cursoId = crearCurso(admin);
    UUID paraleloId = crearParalelo(admin, cursoId);
    UUID materiaId = crearMateria(admin, "Ciencias");
    asignarCurso(admin, materiaId, cursoId, paraleloId);
    ParametrosPeriodoDePrueba.configurarPorDefecto(restTemplate, admin, gestionId, periodoId);

    ResponseEntity<ErrorResponse> sinDocente = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), admin),
        ErrorResponse.class);
    assertThat(sinDocente.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(sinDocente.getBody().codigo()).isEqualTo("E_MATERIA_SIN_DOCENTE");
    assertThat(sinDocente.getBody().mensaje()).doesNotContain("@");

    UUID profesorId = crearProfesor(admin, "Profe Ciencias", "profe-param-docente@colegio.edu.bo");
    asignarProfesor(admin, materiaId, profesorId, cursoId, paraleloId);

    ResponseEntity<PeriodoEvaluacionResponse> abierto = restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), admin),
        PeriodoEvaluacionResponse.class);
    assertThat(abierto.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(abierto.getBody().estado()).isEqualTo("ABIERTO");
  }

  private UUID crearGestion(HttpHeaders admin) {
    return restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2027", LocalDate.of(2027, 2, 1), LocalDate.of(2027, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();
  }

  private UUID crearCurso(HttpHeaders admin) {
    return restTemplate.exchange(
            "/api/v1/cursos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearCursoRequest("Primero"), admin),
            CursoResponse.class)
        .getBody()
        .id();
  }

  private UUID crearParalelo(HttpHeaders admin, UUID cursoId) {
    return restTemplate.exchange(
            "/api/v1/cursos/" + cursoId + "/paralelos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearParaleloRequest("A"), admin),
            ParaleloResponse.class)
        .getBody()
        .id();
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

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate.exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
        .getBody()
        .id();
  }

  private void asignarCurso(HttpHeaders admin, UUID materiaId, UUID cursoId, UUID paraleloId) {
    restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-curso",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionCursoRequest(cursoId, paraleloId), admin),
        AsignacionCursoResponse.class);
  }

  private void asignarProfesor(
      HttpHeaders admin, UUID materiaId, UUID profesorId, UUID cursoId, UUID paraleloId) {
    restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-profesor",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        AsignacionProfesorResponse.class);
  }

  private List<SeccionEvaluacionResponse> listarSecciones(UUID gestionId, HttpHeaders headers) {
    return restTemplate.exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {})
        .getBody();
  }

  private List<PeriodoEvaluacionResponse> listarPeriodos(UUID gestionId, HttpHeaders headers) {
    return restTemplate.exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<PeriodoEvaluacionResponse>>() {})
        .getBody();
  }

  private HttpHeaders crearTenantYAutenticarAdmin(String nombreTenant, String adminEmail) {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    UUID tenantId = restTemplate.exchange(
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
