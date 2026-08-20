/**
 * Modulo {@code academico} (Spring Modulith) - GestionEscolar, PeriodoEvaluacion,
 * SeccionEvaluacion, TipoEvaluacion, Evaluacion, Curso, Paralelo, Materia, Estudiante,
 * Inscripcion.
 *
 * <p>Implementa {@code FSD-UC-012} (Gestion Escolar) completo: {@code GestionEscolar}
 * (Aggregate Root, ciclo {@code PLANIFICACION}/{@code ACTIVA}/{@code CERRADA}), alta,
 * listado filtrable/paginado y cambio de estado ({@code DD-UC-008} / {@code PR-IMPL-008}).
 * {@code FSD-UC-013}..{@code FSD-UC-020} siguen pendientes: no implementar codigo sobre
 * ellos sin resolver primero los puntos 2-5 pendientes de definicion de {@code ADR-0009}
 * &sect;3 (ver docs/product/DTP.md &sect;A.2).
 *
 * <p>Bootstrap creado por {@code DD-UC-001} / {@code PR-IMPL-001} (ADR-0011). Poblado por
 * {@code DD-UC-008} / {@code PR-IMPL-008} (ADR-0009, ADR-0011, ADR-0012).
 */
package com.edusync.academico;
