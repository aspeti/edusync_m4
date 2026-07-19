/**
 * Modulo {@code identidad} (Spring Modulith) - Usuario, UsuarioRol, login/JWT, RBAC.
 *
 * <p>Implementa la autenticacion de {@code FSD-UC-021} (Gestion de Usuarios y Roles,
 * parcial: login. El CRUD administrativo completo llega en {@code DD-UC-004}).
 *
 * <p>Paquete API del modulo (convencion Spring Modulith: los tipos declarados
 * directamente aqui, como {@link com.edusync.identidad.UsuarioCreacionPort},
 * {@link com.edusync.identidad.CrearUsuarioCommand} y
 * {@link com.edusync.identidad.UsuarioId}, son el unico contrato publico visible para
 * otros modulos; {@code domain}/{@code application}/{@code infrastructure} son
 * internos).
 *
 * <p>Poblado por {@code DD-UC-002} / {@code PR-IMPL-002} (ADR-0001, ADR-0010, ADR-0011).
 * Bootstrap original: {@code DD-UC-001} / {@code PR-IMPL-001}.
 */
package com.edusync.identidad;
