/**
 * Modulo {@code identidad} (Spring Modulith) - Usuario, UsuarioRol, login/JWT, RBAC.
 *
 * <p>Implementa la autenticacion de {@code FSD-UC-021} (Gestion de Usuarios y Roles,
 * parcial: login. El CRUD administrativo completo llega en {@code DD-UC-004}), incluido
 * el enforcement de {@code BR-014} (bloqueo de login de tenants suspendidos/vencidos).
 *
 * <p>Paquete API del modulo (convencion Spring Modulith: los tipos declarados
 * directamente aqui, como {@link com.edusync.identidad.UsuarioCreacionPort},
 * {@link com.edusync.identidad.CrearUsuarioCommand}, {@link com.edusync.identidad.UsuarioId}
 * y {@link com.edusync.identidad.TenantConsultaPort}, son el unico contrato publico
 * visible para otros modulos; {@code domain}/{@code application}/{@code infrastructure}
 * son internos). {@code TenantConsultaPort} es un puerto de salida (SPI) que
 * {@code identidad} declara para si misma pero que implementa {@code plataforma}
 * (ver su Javadoc para el porque de esta inversion, {@code DD-UC-003}).
 * {@code identidad} tambien implementa {@code academico.ProfesorConsultaPort}
 * ({@code DD-UC-012}): arista {@code identidad -> academico}, sin ciclo.
 *
 * <p>Poblado por {@code DD-UC-002} / {@code PR-IMPL-002} (ADR-0001, ADR-0010, ADR-0011);
 * {@code TenantConsultaPort} y el enforcement de {@code BR-014} llegan en
 * {@code DD-UC-003} / {@code PR-IMPL-003}. Bootstrap original: {@code DD-UC-001} /
 * {@code PR-IMPL-001}.
 */
package com.edusync.identidad;
