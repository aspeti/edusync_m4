/**
 * Dominio puro del modulo {@code academico} (sin dependencias de Spring/JPA).
 *
 * <p>{@link com.edusync.academico.domain.GestionEscolar} es el primer Aggregate Root
 * (ciclo {@code PLANIFICACION}/{@code ACTIVA}/{@code CERRADA}, {@code FSD-UC-012}). Poblado
 * por {@code DD-UC-008} (docs/design/DD-UC-008.md). {@link com.edusync.academico.domain.Curso}
 * y {@link com.edusync.academico.domain.Paralelo} son el segundo par de Aggregates, sin
 * estado ni transiciones ({@code FSD-UC-017}, {@code DD-UC-010}).
 */
package com.edusync.academico.domain;
