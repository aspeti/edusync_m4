package com.edusync.identidad.domain;

/**
 * Roles de EduSync (ADR-0009 §nomenclatura, ADR-0010 §multi-rol).
 *
 * <p>{@code SYSADMIN} es el unico rol de plataforma (sin {@code tenant_id}); el resto son
 * roles de tenant. Un {@link Usuario} puede tener uno o mas roles simultaneos, salvo la
 * invariante permanente de {@code SYSADMIN} (ver {@link Usuario#crear}).
 */
public enum Rol {
  SYSADMIN,
  ADMIN,
  SECRETARIA,
  ASESOR,
  PROFESOR
}
