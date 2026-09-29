package com.edusync.shared.ai.infrastructure.adapter.out.descubrimiento;

/**
 * Textos pensados para el LLM: el summary de OpenAPI a menudo es generico
 * ("Listar") y el modelo entonces solo piensa en usuarios. El asistente debe
 * cubrir el dominio academico completo (DD-UC-024).
 */
final class DescripcionHerramientaAgente {

    private DescripcionHerramientaAgente() {}

    static String enriquecer(String path, String metodoHttp, String summary, String nombre) {
        String pista = pistaPorPath(path, metodoHttp);
        String base = (summary == null || summary.isBlank()) ? ("Herramienta " + nombre) : summary.strip();
        if (pista == null) {
            return base + " [" + metodoHttp + " " + path + "]";
        }
        return pista + " OpenAPI: " + base + ". [" + metodoHttp + " " + path + "]";
    }

    private static String pistaPorPath(String path, String metodoHttp) {
        if (path.startsWith("/api/v1/gestiones-escolares")) {
            if (path.contains("/periodos")) {
                return "Periodos de evaluacion de una gestion escolar (trimestres u orden configurable).";
            }
            if (path.contains("/secciones")) {
                return "Secciones de evaluacion de una gestion (Ser/Saber/Hacer/AE o las configuradas).";
            }
            if (path.endsWith("/activa") || path.contains("/activa/")) {
                return "Gestion escolar ACTIVA del tenant (la vigente para consulta operativa).";
            }
            if ("GET".equals(metodoHttp) && path.equals("/api/v1/gestiones-escolares")) {
                return "Lista gestiones escolares del tenant (anio lectivo, estado PLANIFICACION/ACTIVA/CERRADA).";
            }
            return "Detalle de una gestion escolar del tenant.";
        }
        if (path.startsWith("/api/v1/cursos")) {
            if (path.contains("/paralelos")) {
                return "Paralelos (secciones/aulas) de un curso, p. ej. 1ro A / 1ro B.";
            }
            if (path.equals("/api/v1/cursos")) {
                return "Lista cursos academicos del tenant (grados/niveles), no usuarios.";
            }
            return "Datos de un curso academico.";
        }
        if (path.startsWith("/api/v1/materias")) {
            if (path.contains("/asignaciones-curso")) {
                return "Asignaciones de una materia a curso/paralelo.";
            }
            if (path.contains("/asignaciones-profesor")) {
                return "Asignaciones de una materia a profesores.";
            }
            if (path.contains("/evaluaciones")) {
                return "Evaluaciones de una materia (sin calificaciones individuales).";
            }
            if (path.endsWith("/mias")) {
                return "Materias asignadas al profesor autenticado.";
            }
            if (path.endsWith("/profesores-disponibles")) {
                return "Profesores del tenant disponibles para asignar a una materia.";
            }
            if (path.equals("/api/v1/materias")) {
                return "Lista materias (asignaturas) del tenant.";
            }
            return "Detalle de una materia y su configuracion.";
        }
        if (path.startsWith("/api/v1/estudiantes")) {
            if (path.contains("/inscripciones")) {
                return "Inscripciones de un estudiante (curso y paralelo por gestion).";
            }
            if (path.equals("/api/v1/estudiantes")) {
                return "Lista estudiantes del tenant (identidad academica, no el catalogo de usuarios).";
            }
            return "Detalle de un estudiante del tenant.";
        }
        if (path.startsWith("/api/v1/profesores")) {
            if (path.contains("/asignaciones")) {
                return "Asignaciones de materias/cursos de un profesor.";
            }
            if (path.equals("/api/v1/profesores")) {
                return "Lista profesores (usuarios con rol PROFESOR) del tenant.";
            }
            return "Detalle de un profesor del tenant.";
        }
        if (path.startsWith("/api/v1/evaluaciones")) {
            return "Metadatos de una evaluacion (materia, periodo, seccion, puntaje). Sin notas por estudiante.";
        }
        if (path.startsWith("/api/v1/usuarios")) {
            return "Lista o consulta usuarios de la institucion (cuentas con roles ADMIN/SECRETARIA/PROFESOR/ASESOR).";
        }
        if (path.startsWith("/api/v1/periodos-evaluacion")) {
            return "Consulta de un periodo de evaluacion por id.";
        }
        if (path.startsWith("/api/v1/secciones-evaluacion")) {
            return "Consulta de una seccion de evaluacion por id.";
        }
        return null;
    }
}
