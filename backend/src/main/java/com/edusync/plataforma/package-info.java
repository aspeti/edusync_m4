/**
 * Modulo {@code plataforma} (Spring Modulith) - SysAdmin, Tenant, Suscripcion.
 *
 * <p>Implementa {@code FSD-UC-011} (Gestion de Tenants y Suscripciones) completo:
 * {@code Tenant} (Aggregate Root, ciclo ACTIVO/SUSPENDIDO/VENCIDO), scheduler diario de
 * vencimiento, y {@code CrearAdminTenantService}, primer consumidor real de
 * {@code identidad.UsuarioCreacionPort} (API publica de {@code identidad}).
 *
 * <p>Sin paquete API propio nuevo: este modulo no expone tipos publicos en su raiz hoy
 * (a diferencia de {@code identidad}) — su unica interaccion saliente hacia otro modulo
 * (alta de admin) usa la API publica de {@code identidad}; su interaccion entrante
 * (consulta de estado de tenant durante el login) la resuelve implementando
 * {@code identidad.TenantConsultaPort} desde {@code infrastructure} (ver su Javadoc),
 * sin necesidad de declarar nada en la raiz de {@code plataforma}.
 *
 * <p>Bootstrap creado por {@code DD-UC-001} / {@code PR-IMPL-001} (ADR-0011). Poblado por
 * {@code DD-UC-003} / {@code PR-IMPL-003} (ADR-0009, ADR-0010, ADR-0011).
 */
package com.edusync.plataforma;
