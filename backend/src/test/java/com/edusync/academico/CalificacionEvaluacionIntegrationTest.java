package com.edusync.academico;

import static org.assertj.core.api.Assertions.assertThat;

import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionCursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.AsignacionProfesorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CalificacionFilaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CalificacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.CambiarEstadoPeriodoEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearAsignacionProfesorRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearCursoRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEstudianteRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearEvaluacionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearGestionEscolarRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearInscripcionRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearMateriaRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CrearParaleloRequest;
import com.edusync.academico.infrastructure.adapter.in.rest.CursoResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ErrorResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EstudianteResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.EvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.GestionEscolarResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.InscripcionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.MateriaResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.NotaProvisionalResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.ParaleloResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.PeriodoEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.SeccionEvaluacionResponse;
import com.edusync.academico.infrastructure.adapter.in.rest.UpsertCalificacionesRequest;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
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
 * Stop condition de {@code PR-IMPL-018} ({@code DD-UC-018}): caso canónico ADR-0013,
 * rango inválido 422, no inscrito 422, cross-tenant 404.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
class CalificacionEvaluacionIntegrationTest {

  @Container
  static PostgreSQLContainer postgres =
      new PostgreSQLContainer("postgres:15")
          .withDatabaseName("edusync_it")
          .withUsername("edusync")
          .withPassword("edusync_it_local");

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired private TestRestTemplate restTemplate;

  @Value("${edusync.seed.sysadmin.email}")
  private String sysAdminEmail;

  @Value("${edusync.seed.sysadmin.password}")
  private String sysAdminPassword;

  @Test
  void casoCanonicoRangoNoInscritoYAislamiento() {
    HttpHeaders adminA =
        crearTenantYAutenticarAdmin("Colegio Calc A", "admin-calc-a@colegio.edu.bo");
    HttpHeaders adminB =
        crearTenantYAutenticarAdmin("Colegio Calc B", "admin-calc-b@colegio.edu.bo");

    UUID gestionId = crearGestion(adminA);
    UUID cursoId = crearCurso(adminA);
    UUID paraleloId = crearParalelo(adminA, cursoId);
    UUID profesorId = crearProfesor(adminA, "Prof Calc", "profesor-calc@colegio.edu.bo");
    UUID materiaId = crearMateria(adminA, "Lenguaje");
    asignarCursoYProfesor(adminA, materiaId, cursoId, paraleloId, profesorId);

    UUID estudianteId =
        crearEstudiante(adminA, "87654321", "Luis Mamani");
    crearInscripcion(adminA, estudianteId, gestionId, cursoId, paraleloId);

    List<PeriodoEvaluacionResponse> periodos = listarPeriodos(gestionId, adminA);
    UUID t1 = periodos.get(0).id();
    Map<String, SeccionEvaluacionResponse> secciones =
        listarSecciones(gestionId, adminA).stream()
            .collect(Collectors.toMap(SeccionEvaluacionResponse::nombre, Function.identity()));

    abrirPeriodo(adminA, t1);

    UUID evalSaber1 =
        crearEval(adminA, "Saber 1", materiaId, t1, secciones.get("Saber").id());
    UUID evalSaber2 =
        crearEval(adminA, "Saber 2", materiaId, t1, secciones.get("Saber").id());
    UUID evalSer = crearEval(adminA, "Ser 1", materiaId, t1, secciones.get("Ser").id());
    UUID evalHacer = crearEval(adminA, "Hacer 1", materiaId, t1, secciones.get("Hacer").id());
    UUID evalAe =
        crearEval(adminA, "AE 1", materiaId, t1, secciones.get("Autoevaluación").id());

    upsert(adminA, evalSaber1, estudianteId, new BigDecimal("35"));
    upsert(adminA, evalSaber2, estudianteId, new BigDecimal("40"));
    upsert(adminA, evalSer, estudianteId, new BigDecimal("5"));
    upsert(adminA, evalHacer, estudianteId, new BigDecimal("40"));
    upsert(adminA, evalAe, estudianteId, new BigDecimal("10"));

    ResponseEntity<List<CalificacionFilaResponse>> nomina =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalSaber1 + "/calificaciones",
            HttpMethod.GET,
            new HttpEntity<>(adminA),
            new ParameterizedTypeReference<List<CalificacionFilaResponse>>() {});
    assertThat(nomina.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(nomina.getBody()).hasSize(1);
    assertThat(nomina.getBody().get(0).valor()).isEqualByComparingTo("35.00");

    ResponseEntity<NotaProvisionalResponse> provisional =
        restTemplate.exchange(
            "/api/v1/materias/"
                + materiaId
                + "/estudiantes/"
                + estudianteId
                + "/nota-provisional?periodoId="
                + t1,
            HttpMethod.GET,
            new HttpEntity<>(adminA),
            NotaProvisionalResponse.class);
    assertThat(provisional.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(provisional.getBody().estado()).isEqualTo("PROVISIONAL");
    assertThat(provisional.getBody().notaPeriodo()).isEqualTo(93);
    assertThat(provisional.getBody().promedioGestion()).isEqualTo(31);
    NotaProvisionalResponse.SeccionNotaResponse saberNota =
        provisional.getBody().secciones().stream()
            .filter(s -> s.nombre().equals("Saber"))
            .findFirst()
            .orElseThrow();
    assertThat(saberNota.notaSeccion()).isEqualByComparingTo("37.50");

    ResponseEntity<ErrorResponse> rango =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalSaber1 + "/calificaciones",
            HttpMethod.PUT,
            new HttpEntity<>(
                new UpsertCalificacionesRequest(
                    List.of(
                        new UpsertCalificacionesRequest.Item(
                            estudianteId, new BigDecimal("46")))),
                adminA),
            ErrorResponse.class);
    assertThat(rango.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(rango.getBody().codigo()).isEqualTo("E_RANGO_INVALIDO");

    UUID otroEstudiante = crearEstudiante(adminA, "11112222", "No Inscrito");
    ResponseEntity<ErrorResponse> noInscrito =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalSaber1 + "/calificaciones",
            HttpMethod.PUT,
            new HttpEntity<>(
                new UpsertCalificacionesRequest(
                    List.of(
                        new UpsertCalificacionesRequest.Item(
                            otroEstudiante, new BigDecimal("10")))),
                adminA),
            ErrorResponse.class);
    assertThat(noInscrito.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(noInscrito.getBody().codigo()).isEqualTo("E_ESTUDIANTE_NO_INSCRITO");

    ResponseEntity<ErrorResponse> cross =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalSaber1 + "/calificaciones",
            HttpMethod.GET,
            new HttpEntity<>(adminB),
            ErrorResponse.class);
    assertThat(cross.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private void upsert(HttpHeaders admin, UUID evalId, UUID estudianteId, BigDecimal valor) {
    ResponseEntity<List<CalificacionResponse>> resp =
        restTemplate.exchange(
            "/api/v1/evaluaciones/" + evalId + "/calificaciones",
            HttpMethod.PUT,
            new HttpEntity<>(
                new UpsertCalificacionesRequest(
                    List.of(new UpsertCalificacionesRequest.Item(estudianteId, valor))),
                admin),
            new ParameterizedTypeReference<List<CalificacionResponse>>() {});
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private UUID crearEval(
      HttpHeaders admin, String nombre, UUID materiaId, UUID periodoId, UUID seccionId) {
    return restTemplate
        .exchange(
            "/api/v1/evaluaciones",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearEvaluacionRequest(
                    nombre, materiaId, periodoId, seccionId, LocalDate.of(2026, 3, 10), null),
                admin),
            EvaluacionResponse.class)
        .getBody()
        .id();
  }

  private void abrirPeriodo(HttpHeaders admin, UUID periodoId) {
    restTemplate.exchange(
        "/api/v1/periodos-evaluacion/" + periodoId + "/estado",
        HttpMethod.PATCH,
        new HttpEntity<>(new CambiarEstadoPeriodoEvaluacionRequest("ABIERTO"), admin),
        PeriodoEvaluacionResponse.class);
  }

  private UUID crearEstudiante(HttpHeaders admin, String rude, String nombre) {
    return restTemplate
        .exchange(
            "/api/v1/estudiantes",
            HttpMethod.POST,
            new HttpEntity<>(new CrearEstudianteRequest(rude, nombre, null, null), admin),
            EstudianteResponse.class)
        .getBody()
        .id();
  }

  private void crearInscripcion(
      HttpHeaders admin, UUID estudianteId, UUID gestionId, UUID cursoId, UUID paraleloId) {
    restTemplate.exchange(
        "/api/v1/inscripciones",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearInscripcionRequest(
                estudianteId, gestionId, cursoId, paraleloId, LocalDate.of(2026, 2, 15)),
            admin),
        InscripcionResponse.class);
  }

  private UUID crearGestion(HttpHeaders admin) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearGestionEscolarRequest(
                    "2026", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 11, 30)),
                admin),
            GestionEscolarResponse.class)
        .getBody()
        .id();
  }

  private UUID crearCurso(HttpHeaders admin) {
    return restTemplate
        .exchange(
            "/api/v1/cursos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearCursoRequest("Primero"), admin),
            CursoResponse.class)
        .getBody()
        .id();
  }

  private UUID crearParalelo(HttpHeaders admin, UUID cursoId) {
    return restTemplate
        .exchange(
            "/api/v1/cursos/" + cursoId + "/paralelos",
            HttpMethod.POST,
            new HttpEntity<>(new CrearParaleloRequest("A"), admin),
            ParaleloResponse.class)
        .getBody()
        .id();
  }

  private UUID crearProfesor(HttpHeaders admin, String nombre, String email) {
    return restTemplate
        .exchange(
            "/api/v1/usuarios",
            HttpMethod.POST,
            new HttpEntity<>(
                new CrearUsuarioRequest(nombre, email, "secreto123", Set.of("PROFESOR")), admin),
            UsuarioResponse.class)
        .getBody()
        .id();
  }

  private UUID crearMateria(HttpHeaders admin, String nombre) {
    return restTemplate
        .exchange(
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
        new HttpEntity<>(
            new CrearAsignacionProfesorRequest(profesorId, cursoId, paraleloId), admin),
        AsignacionProfesorResponse.class);
  }

  private List<SeccionEvaluacionResponse> listarSecciones(UUID gestionId, HttpHeaders headers) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/secciones",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<SeccionEvaluacionResponse>>() {})
        .getBody();
  }

  private List<PeriodoEvaluacionResponse> listarPeriodos(UUID gestionId, HttpHeaders headers) {
    return restTemplate
        .exchange(
            "/api/v1/gestiones-escolares/" + gestionId + "/periodos",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new ParameterizedTypeReference<List<PeriodoEvaluacionResponse>>() {})
        .getBody();
  }

  private HttpHeaders crearTenantYAutenticarAdmin(String nombreTenant, String adminEmail) {
    HttpHeaders sysAdminHeaders = autenticarComo(sysAdminEmail, sysAdminPassword);
    var tenantId =
        restTemplate
            .exchange(
                "/api/v1/plataforma/tenants",
                HttpMethod.POST,
                new HttpEntity<>(
                    new RegistrarTenantRequest(
                        nombreTenant, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                    sysAdminHeaders),
                TenantResponse.class)
            .getBody()
            .id();
    restTemplate.exchange(
        "/api/v1/plataforma/tenants/" + tenantId + "/admins",
        HttpMethod.POST,
        new HttpEntity<>(
            new CrearAdminTenantRequest("Admin " + nombreTenant, adminEmail, "secreto123"),
            sysAdminHeaders),
        AdminCreadoResponse.class);
    return autenticarComo(adminEmail, "secreto123");
  }

  private HttpHeaders autenticarComo(String email, String password) {
    ResponseEntity<LoginResponse> login =
        restTemplate.postForEntity(
            "/api/v1/auth/login", new LoginRequest(email, password), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(login.getBody().accessToken());
    return headers;
  }
}
