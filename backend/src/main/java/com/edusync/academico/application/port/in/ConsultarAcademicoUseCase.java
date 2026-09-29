package com.edusync.academico.application.port.in;

import java.util.List;
import java.util.UUID;

/** Consulta académica parametrizable del asistente (ADR-0020). Sin SQL; IDs ya resueltos. */
public interface ConsultarAcademicoUseCase {

  Resultado consultar(UUID tenantId, UUID actorId, boolean veTodasLasMaterias, Comando comando);

  enum Operacion {
    NOTAS,
    PROMEDIO,
    REPROBADOS,
    TOP,
    NOMINA,
    MATERIAS_ESTUDIANTE
  }

  record Comando(
      Operacion operacion,
      UUID estudianteId,
      UUID cursoId,
      UUID paraleloId,
      UUID materiaId,
      UUID periodoEvaluacionId,
      UUID gestionEscolarId,
      Integer umbral) {}

  record Fila(String etiqueta, Integer valor, String detalle) {}

  record Resultado(
      String estado,
      String operacion,
      String preguntaAclaracion,
      List<String> faltantes,
      Integer agregado,
      List<Fila> filas) {

    public static final String OK = "OK";
    public static final String FALTA_CONTEXTO = "FALTA_CONTEXTO";
    public static final String SIN_RESULTADOS = "SIN_RESULTADOS";

    public Resultado {
      faltantes = faltantes == null ? List.of() : List.copyOf(faltantes);
      filas = filas == null ? List.of() : List.copyOf(filas);
    }

    public static Resultado falta(String pregunta, List<String> faltantes) {
      return new Resultado(FALTA_CONTEXTO, null, pregunta, faltantes, null, List.of());
    }

    public static Resultado vacio(String operacion) {
      return new Resultado(SIN_RESULTADOS, operacion, null, List.of(), null, List.of());
    }

    public static Resultado ok(String operacion, Integer agregado, List<Fila> filas) {
      return new Resultado(OK, operacion, null, List.of(), agregado, filas);
    }
  }
}
