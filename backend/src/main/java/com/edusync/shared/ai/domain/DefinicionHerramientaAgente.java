package com.edusync.shared.ai.domain;

import java.util.List;

/**
 * Entrada del catalogo tipado del asistente (DD-UC-025 / TOOL-CATALOG.md).
 * El LLM y el camino KEYWORD usan {@link #toolId} como nombre de herramienta.
 */
public record DefinicionHerramientaAgente(
        String toolId,
        String etiqueta,
        String metodoHttp,
        String path,
        List<String> tablasFuente,
        List<String> frasesKeyword,
        List<ParametroHerramienta> parametros,
        boolean escritura
) {
    public DefinicionHerramientaAgente {
        tablasFuente = List.copyOf(tablasFuente);
        frasesKeyword = List.copyOf(frasesKeyword);
        parametros = List.copyOf(parametros);
    }

    public HerramientaLlm aHerramientaLlm() {
        String descripcion = switch (toolId) {
            case "find_estudiante" ->
                    "Busca estudiantes por nombre (argumento q, texto, NUNCA inventes un UUID). Si estado=AMBIGUO pide aclaracion.";
            case "find_materia" ->
                    "Busca materias por nombre (q). No inventes IDs.";
            case "find_periodo" ->
                    "Busca el periodo/trimestre de la gestion activa (q: 'primer trimestre', 'Trimestre 1'). No uses orden=1 por tu cuenta.";
            case "find_curso_paralelo" ->
                    "Resuelve un curso y paralelo (q: '1ro A'). Devuelve paraleloId.";
            case "find_profesor" ->
                    "Busca profesores por nombre (q). No inventes IDs.";
            case "consultar_academico" ->
                    "Consulta notas/promedios/nomina. IDs solo los devueltos por find_*. operacion: NOTAS, PROMEDIO, REPROBADOS, TOP, NOMINA, MATERIAS_ESTUDIANTE.";
            default -> escritura
                    ? "Crear " + etiqueta + " (requiere confirmacion del usuario)."
                    : "Listar " + etiqueta + " del tenant (solo lectura).";
        };
        return new HerramientaLlm(toolId, descripcion, metodoHttp, path, parametros);
    }
}
