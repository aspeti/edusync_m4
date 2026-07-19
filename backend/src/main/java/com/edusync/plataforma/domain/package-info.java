/**
 * Dominio puro del modulo {@code plataforma} (sin dependencias de Spring/JPA).
 *
 * <p>{@link com.edusync.plataforma.domain.Tenant} es el Aggregate Root (ciclo de
 * suscripcion: {@code ACTIVO}/{@code SUSPENDIDO}/{@code VENCIDO}, {@code BR-013}/
 * {@code BR-014}). Poblado por {@code DD-UC-003} (docs/design/DD-UC-003.md).
 */
package com.edusync.plataforma.domain;
