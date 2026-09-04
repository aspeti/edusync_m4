package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
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
 * Stop condition de {@code PR-IMPL-017} ({@code DD-UC-017} &sect;6): dos evals Saber
 * con {@code puntajeMaximo=45}, A1 409, periodo PENDIENTE 422, Profesor no asignado 404,
 * cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class EvaluacionIntegrationTest {

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
  void dosEvalsSaberA1PeriodoPendienteYAislamiento() {
    HttpHeaders adminA = crearTenantYAutenticarAdmin("Colegio Evals A", "admin-evals-a@colegio.edu.bo");
    HttpHeaders adminB = crearTenantYAutenticarAdmin("Colegio Evals B", "admin-evals-b@colegio.edu.bo");

    UUID gestionId = crearGestion(adminA);
    UUID cursoId = crearCurso(adminA);
    UUID paraleloId = crearParalelo(adminA, cursoId);
    UUID profesorId = crearProfesor(adminA, "Profesor Asignado", "profesor-evals@colegio.edu.bo");
    UUID materiaId = crearMateria(adminA, "Matemáticas");
    asignarCursoYProfesor(adminA, materiaId, cursoId, paraleloId, profesorId);

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, adminA);
    UUID t1 = periodos.get(0).id();
    UUID t2 = periodos.get(1).id();
    UUID saberId = listarSecciones(gestionId, adminA).stream()
        .filter(s -> s.nombre().equals("Saber"))
        .findFirst()
        .orElseThrow()
        .id();

    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + t1 + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), adminA),
        PeriodoEvaluacionResponse.class);

    ResponseEntity<EvaluacionResponse> eval1 = restTemplate.exchange(
        "/api/v1/evaluaciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearEvaluacionRequest(
                "Prueba 1", materiaId, t1, saberId, LocalDate.of(2026, 3, 10), null),
            adminA),
        EvaluacionResponse.class);
    assertThat(eval1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(eval1.getBody().puntajeMaximo()).isEqualByComparingTo("45.00");
    assertThat(eval1.getBody().estado()).isEqualTo("ACTIVA");

    ResponseEntity<EvaluacionResponse> eval2 = restTemplate.exchange(
        "/api/v1/evaluaciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearEvaluacionRequest(
                "Prueba 2", materiaId, t1, saberId, LocalDate.of(2026, 3, 20), "oral"),
            adminA),
        EvaluacionResponse.class);
    assertThat(eval2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(eval2.getBody().puntajeMaximo()).isEqualByComparingTo("45.00");

    ResponseEntity<ErrorResponse> pendiente = restTemplate.exchange(
        "/api/v1/evaluaciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearEvaluacionRequest(
                "En T2", materiaId, t2, saberId, LocalDate.of(2026, 6, 10), null),
            adminA),
        ErrorResponse.class);
    assertThat(pendiente.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(pendiente.getBody().codigo()).isEqualTo("E_PERIODO_NO_ABIERTO");

    UUID materiaSinProfesor = crearMateria(adminA, "Física");
    ResponseEntity<ErrorResponse> sinProfesor = restTemplate.exchange(
        "/api/v1/evaluaciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearEvaluacionRequest(
                "Sin profesor", materiaSinProfesor, t1, saberId, LocalDate.of(2026, 3, 11), null),
            adminA),
        ErrorResponse.class);
    assertThat(sinProfesor.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(sinProfesor.getBody().codigo()).isEqualTo("E_MATERIA_SIN_PROFESOR");

    crearProfesor(adminA, "Otro Profesor", "otro-profesor-evals@colegio.edu.bo");
    HttpHeaders otroProfesor = autenticarComo("otro-profesor-evals@colegio.edu.bo", "secreto123");
    ResponseEntity<ErrorResponse> noAsignado = restTemplate.exchange(
        "/api/v1/evaluaciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearEvaluacionRequest(
                "Ajeno", materiaId, t1, saberId, LocalDate.of(2026, 3, 12), null),
            otroProfesor),
        ErrorResponse.class);
    assertThat(noAsignado.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(noAsignado.getBody().codigo()).isEqualTo("E_MATERIA_NO_ENCONTRADA");

    ResponseEntity<ErrorResponse> cross = restTemplate.exchange(
        "/api/v1/evaluaciones/" + eval1.getBody().id(),
        HttpMethod.GET,
        new HttpEntity<>(adminB),
        ErrorResponse.class);
    assertThat(cross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(cross.getBody().codigo()).isEqualTo("E_EVALUACION_NO_ENCONTRADA");

    HttpHeaders profesorAsignado = autenticarComo("profesor-evals@colegio.edu.bo", "secreto123");
    ResponseEntity<List<MateriaResponse>> mias = restTemplate.exchange(
        "/api/v1/materias/mias",
        HttpMethod.GET,
        new HttpEntity<>(profesorAsignado),
        new ParameterizedTypeReference<List<MateriaResponse>>() {});
    assertThat(mias.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mias.getBody()).extracting(MateriaResponse::id).contains(materiaId);
  }

  private UUID crearGestion(HttpHeaders admin) {
    return restTemplate.exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest("2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 11, 30)),
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

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate.exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
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

  private void asignarCursoYProfesor(
      HttpHeaders admin, UUID materiaId, UUID cursoId, UUID paraleloId, UUID profesorId) {
    restTemplate.exchange(
        "/api/v1/materias/" + materiaId + "/asignaciones-curso",
        HttpMethod.POST,
        new HttpEntity<>(new CrearAsignacionCursoRequest(cursoId, paraleloId), admin),
        AsignacionCursoResponse.class);
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
