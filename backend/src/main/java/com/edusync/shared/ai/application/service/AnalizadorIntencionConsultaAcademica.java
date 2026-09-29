package com.edusync.shared.ai.application.service;

import com.edusync.shared.ai.domain.ContextoConsultaAgente;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Extrae operación y menciones (texto) de la pregunta. No resuelve IDs (ADR-0020).
 */
@Component
public class AnalizadorIntencionConsultaAcademica {

    private static final Pattern PROMEDIO_MAS_ALTO =
            Pattern.compile("promedio mas alto(?: de)? (.+)$");
    private static final Pattern REPROBARON =
            Pattern.compile("(?:que )?(?:alumnos |estudiantes )?(?:que )?reprobaron(?: en)? (.+)$");
    private static final Pattern PROMEDIO_DURANTE =
            Pattern.compile("promedio de (.+?) (?:durante el|del|de el) (.+)$");
    private static final Pattern PROMEDIO_EN =
            Pattern.compile("promedio de (.+?) en (.+?)(?: (?:durante el|del) (.+))?$");
    private static final Pattern NOTAS_EN_DEL =
            Pattern.compile("notas de (.+?) en (.+?)(?: (?:durante el|del|de el) (.+))?$");
    private static final Pattern NOTAS_DEL =
            Pattern.compile("notas de (.+?) (?:durante el|del|de el) (.+)$");
    private static final Pattern NOTAS_DE =
            Pattern.compile("notas de (.+)$");
    private static final Pattern MATERIAS_TIENE =
            Pattern.compile("(?:que )?materias (?:tiene|tiene asignadas) (.+)$");
    private static final Pattern ALUMNOS_EN =
            Pattern.compile("(?:que )?(?:alumnos|estudiantes) (?:estan|están|hay) en (.+)$");
    private static final Pattern ALUMNOS_DE =
            Pattern.compile("(?:alumnos|estudiantes) de (.+)$");
    private static final Pattern PROFESOR =
            Pattern.compile("(?:existe|hay)(?: algun| un)? profesor(?:es)?(?: con nombre| llamado| nombrado)? (.+)$");
    private static final Pattern PROFESOR_NOMBRE =
            Pattern.compile("profesor(?:es)? con nombre (.+)$");
    private static final Pattern ESTUDIANTE =
            Pattern.compile("(?:existe|hay)(?: algun| un)? (?:estudiante|alumno)(?: con nombre| llamado)? (.+)$");
    private static final Pattern FOLLOW_PERIODO =
            Pattern.compile("^(?:del|de el|de) (.+)$");
    private static final Pattern FOLLOW_EN =
            Pattern.compile("^(?:y )?en (.+)$");

    public Optional<IntencionConsultaAcademica> analizar(String pregunta, ContextoConsultaAgente contexto) {
        String n = EnrutadorPalabrasClaveAgente.normalizar(pregunta);
        n = n.replaceFirst(
                "^(cuales son las |cual es el |cual es la |muestrame las |mostrame las |mostrame el |muestrame el |quien tuvo el )",
                "");
        n = n.replace("de este trimestre", "del este trimestre");
        n = n.replace("de este periodo", "del este periodo");
        if (n.isBlank()) {
            return Optional.empty();
        }

        Matcher m;
        m = PROMEDIO_MAS_ALTO.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.TOP, null, null, null, m.group(1).trim(), null));
        }
        m = REPROBARON.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.REPROBADOS, null, m.group(1).trim(), null, null, null));
        }
        m = PROMEDIO_DURANTE.matcher(n);
        if (m.find()) {
            String sujeto = m.group(1).trim();
            String periodo = m.group(2).trim();
            if (pareceCursoParalelo(sujeto)) {
                return Optional.of(new IntencionConsultaAcademica(
                        IntencionConsultaAcademica.Operacion.PROMEDIO, null, null, periodo, sujeto, null));
            }
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.PROMEDIO, sujeto, null, periodo, null, null));
        }
        m = PROMEDIO_EN.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.PROMEDIO,
                    m.group(1).trim(),
                    m.group(2).trim(),
                    m.group(3) == null ? null : m.group(3).trim(),
                    null,
                    null));
        }
        m = NOTAS_EN_DEL.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.NOTAS,
                    m.group(1).trim(),
                    m.group(2).trim(),
                    m.group(3) == null ? null : m.group(3).trim(),
                    null,
                    null));
        }
        m = NOTAS_DEL.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.NOTAS, m.group(1).trim(), null, m.group(2).trim(), null, null));
        }
        m = NOTAS_DE.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.NOTAS, m.group(1).trim(), null, null, null, null));
        }
        m = MATERIAS_TIENE.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.MATERIAS_ESTUDIANTE, m.group(1).trim(), null, null, null, null));
        }
        m = ALUMNOS_EN.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.NOMINA, null, null, null, m.group(1).trim(), null));
        }
        m = ALUMNOS_DE.matcher(n);
        if (m.find() && !n.contains("lista los")) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.NOMINA, null, null, null, m.group(1).trim(), null));
        }
        m = PROFESOR_NOMBRE.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.BUSCAR_PROFESOR, null, null, null, null, m.group(1).trim()));
        }
        m = PROFESOR.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.BUSCAR_PROFESOR, null, null, null, null, m.group(1).trim()));
        }
        m = ESTUDIANTE.matcher(n);
        if (m.find()) {
            return Optional.of(new IntencionConsultaAcademica(
                    IntencionConsultaAcademica.Operacion.BUSCAR_ESTUDIANTE, m.group(1).trim(), null, null, null, null));
        }

        return followUp(n, contexto);
    }

    private Optional<IntencionConsultaAcademica> followUp(String n, ContextoConsultaAgente ctx) {
        if (ctx == null || ctx.ultimaOperacion() == null) {
            return Optional.empty();
        }
        IntencionConsultaAcademica.Operacion op;
        try {
            op = IntencionConsultaAcademica.Operacion.valueOf(ctx.ultimaOperacion());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        Matcher m = FOLLOW_PERIODO.matcher(n);
        if (m.matches()) {
            String resto = m.group(1).trim();
            if (parecePeriodo(resto)) {
                return Optional.of(new IntencionConsultaAcademica(op, null, null, resto, null, null));
            }
        }
        m = FOLLOW_EN.matcher(n);
        if (m.matches()) {
            String resto = m.group(1).trim();
            if (parecePeriodo(resto)) {
                return Optional.of(new IntencionConsultaAcademica(op, null, null, resto, null, null));
            }
            return Optional.of(new IntencionConsultaAcademica(op, null, resto, null, null, null));
        }
        return Optional.empty();
    }

    static boolean pareceCursoParalelo(String texto) {
        String n = EnrutadorPalabrasClaveAgente.normalizar(texto);
        return n.matches(".*\\b(\\d+|1ro|2do|3ro|4to|5to|6to|primero|segundo|tercero)\\b.*")
                || n.matches(".+\\s+[a-z0-9]$");
    }

    static boolean parecePeriodo(String texto) {
        String n = EnrutadorPalabrasClaveAgente.normalizar(texto);
        return n.contains("trimestre")
                || n.contains("periodo")
                || n.contains("primer")
                || n.contains("segund")
                || n.contains("tercer")
                || n.matches(".*\\b(1|2|3|t1|t2|t3)\\b.*");
    }
}
