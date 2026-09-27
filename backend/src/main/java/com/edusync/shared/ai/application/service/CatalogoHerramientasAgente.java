package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.DefinicionHerramientaAgente;
import com.edusync.shared.ai.domain.HerramientaLlm;
import com.edusync.shared.ai.domain.ParametroHerramienta;
import com.edusync.shared.ai.domain.ParametroHerramienta.Ubicacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Catalogo tipado (DD-UC-025 / ADR-0020). Lecturas + finds + consulta parametrizable.
 */
@Component
public class CatalogoHerramientasAgente {

    private static final ParametroHerramienta NOMBRE_BODY =
            new ParametroHerramienta("nombre", "string", true, Ubicacion.BODY);
    private static final ParametroHerramienta Q_QUERY =
            new ParametroHerramienta("q", "string", true, Ubicacion.QUERY);
    private static final ParametroHerramienta Q_QUERY_OPCIONAL =
            new ParametroHerramienta("q", "string", false, Ubicacion.QUERY);
    private static final List<ParametroHerramienta> CONSULTA_BODY = List.of(
            new ParametroHerramienta("operacion", "string", true, Ubicacion.BODY),
            new ParametroHerramienta("estudianteId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("cursoId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("paraleloId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("materiaId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("periodoEvaluacionId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("gestionEscolarId", "string", false, Ubicacion.BODY),
            new ParametroHerramienta("umbral", "integer", false, Ubicacion.BODY));

    private static final List<DefinicionHerramientaAgente> LECTURAS = List.of(
            lectura("list_cursos", "cursos", "/api/v1/cursos", List.of("curso"),
                    List.of("lista los cursos", "cuantos cursos", "que cursos hay", "cuántos cursos", "qué cursos hay")),
            lectura("list_gestiones_escolares", "gestiones escolares", "/api/v1/gestiones-escolares",
                    List.of("gestion_escolar"),
                    List.of("lista las gestiones", "gestiones escolares")),
            lectura("get_gestion_activa", "gestion activa", "/api/v1/gestiones-escolares/activa",
                    List.of("gestion_escolar"),
                    List.of("gestion activa", "gestión activa", "cual es la gestion activa",
                            "cuál es la gestión activa")),
            lectura("list_periodos_gestion_activa", "periodos de evaluacion",
                    "/api/v1/gestiones-escolares/activa/periodos", List.of("periodo_evaluacion"),
                    List.of("lista los periodos", "periodos de evaluacion", "periodos de evaluación",
                            "cuantos periodos", "cuántos periodos")),
            lectura("list_secciones_gestion_activa", "secciones de evaluacion",
                    "/api/v1/gestiones-escolares/activa/secciones", List.of("seccion_evaluacion"),
                    List.of("lista las secciones", "secciones de evaluacion", "secciones de evaluación")),
            lectura("list_materias", "materias", "/api/v1/materias", List.of("materia"),
                    List.of("lista las materias", "que materias hay", "qué materias hay")),
            lectura("list_mis_materias", "materias asignadas", "/api/v1/materias/mias", List.of("materia"),
                    List.of("mis materias", "lista mis materias", "materias asignadas")),
            lectura("list_estudiantes", "estudiantes", "/api/v1/estudiantes", List.of("estudiante"),
                    List.of("lista los estudiantes", "lista los alumnos", "cuantos estudiantes",
                            "cuantos alumnos", "cuántos estudiantes", "cuántos alumnos")),
            lectura("list_profesores", "profesores", "/api/v1/profesores", List.of("usuario"),
                    List.of("lista los profesores", "quienes son los profesores", "quiénes son los profesores")),
            lectura("list_usuarios", "usuarios", "/api/v1/usuarios", List.of("usuario", "usuario_rol"),
                    List.of("lista los usuarios", "quienes son los usuarios", "quiénes son los usuarios")),
            lectura("find_estudiante", "estudiantes", "/api/v1/consultas-academicas/entidades/estudiantes",
                    List.of("estudiante"), List.of(), List.of(Q_QUERY)),
            lectura("find_materia", "materias", "/api/v1/consultas-academicas/entidades/materias",
                    List.of("materia"), List.of(), List.of(Q_QUERY)),
            lectura("find_periodo", "periodos de evaluacion",
                    "/api/v1/consultas-academicas/entidades/periodos",
                    List.of("periodo_evaluacion"), List.of(), List.of(Q_QUERY_OPCIONAL)),
            lectura("find_curso_paralelo", "cursos y paralelos",
                    "/api/v1/consultas-academicas/entidades/curso-paralelo",
                    List.of("curso", "paralelo"), List.of(), List.of(Q_QUERY)),
            lectura("find_profesor", "profesores", "/api/v1/consultas-academicas/entidades/profesores",
                    List.of("usuario"),
                    List.of("existe un profesor", "existe algun profesor", "hay un profesor",
                            "hay algun profesor", "profesor con nombre"),
                    List.of(Q_QUERY)),
            lectura("consultar_academico", "consulta academica",
                    "/api/v1/consultas-academicas/consultar",
                    List.of("calificacion_evaluacion", "inscripcion"),
                    List.of(),
                    CONSULTA_BODY,
                    "POST")
    );

    private static final List<DefinicionHerramientaAgente> ESCRITURAS = List.of(
            escritura("create_curso", "curso", "/api/v1/cursos", List.of("curso"),
                    List.of("crea el curso", "crear el curso", "crea un curso", "crear curso")),
            escritura("create_materia", "materia", "/api/v1/materias", List.of("materia"),
                    List.of("crea la materia", "crear la materia", "crea una materia", "crear materia"))
    );

    private static DefinicionHerramientaAgente lectura(
            String toolId, String etiqueta, String path, List<String> tablas, List<String> frases) {
        return lectura(toolId, etiqueta, path, tablas, frases, List.of(), "GET");
    }

    private static DefinicionHerramientaAgente lectura(
            String toolId, String etiqueta, String path, List<String> tablas, List<String> frases,
            List<ParametroHerramienta> parametros) {
        return lectura(toolId, etiqueta, path, tablas, frases, parametros, "GET");
    }

    private static DefinicionHerramientaAgente lectura(
            String toolId, String etiqueta, String path, List<String> tablas, List<String> frases,
            List<ParametroHerramienta> parametros, String metodo) {
        return new DefinicionHerramientaAgente(toolId, etiqueta, metodo, path, tablas, frases, parametros, false);
    }

    private static DefinicionHerramientaAgente escritura(
            String toolId, String etiqueta, String path, List<String> tablas, List<String> frases) {
        return new DefinicionHerramientaAgente(
                toolId, etiqueta, "POST", path, tablas, frases, List.of(NOMBRE_BODY), true);
    }

    public List<DefinicionHerramientaAgente> lecturas() {
        return LECTURAS;
    }

    public List<DefinicionHerramientaAgente> escrituras() {
        return ESCRITURAS;
    }

    public List<DefinicionHerramientaAgente> todas() {
        return Stream.concat(LECTURAS.stream(), ESCRITURAS.stream()).toList();
    }

    public List<HerramientaLlm> herramientasLlm() {
        return todas().stream().map(DefinicionHerramientaAgente::aHerramientaLlm).toList();
    }

    public Optional<DefinicionHerramientaAgente> porId(String toolId) {
        return todas().stream().filter(d -> d.toolId().equals(toolId)).findFirst();
    }

    public Optional<HerramientaLlm> aHerramientaLlm(String toolId) {
        return porId(toolId).map(DefinicionHerramientaAgente::aHerramientaLlm);
    }

    public static boolean pathProhibido(String path) {
        String p = path.toLowerCase(Locale.ROOT);
        return p.startsWith("/api/v1/auth/")
                || p.startsWith("/api/v1/plataforma/")
                || p.startsWith("/api/v1/ai/")
                || p.contains("/calificaciones")
                || p.contains("/nota-provisional");
    }
}
