-- V4__identidad_password_reset_token.sql
-- Modulo identidad (DD-UC-005 / PR-IMPL-005): token de un solo uso para el flujo de
-- restablecimiento de contrasena.
--
-- SIN tenant_id ni politica RLS propia (misma justificacion que la tabla `tenant` en
-- V3__plataforma_tenant.sql y que el flujo de login en V2__identidad_usuario.sql): el paso
-- de confirmacion (POST /api/v1/auth/restablecer-password/confirmar) es publico, sin JWT y
-- por lo tanto sin tenant activo en la sesion -- una politica RLS con tenant_id bloquearia
-- la lectura del propio token en ese momento. Solo se persiste el hash SHA-256 del token,
-- nunca el valor en claro (AGENTS.md seccion 7).

CREATE TABLE password_reset_token (
    id          UUID PRIMARY KEY,
    usuario_id  UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(64) NOT NULL UNIQUE,
    expira_en   TIMESTAMPTZ NOT NULL,
    usado       BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_token_usuario_id ON password_reset_token (usuario_id);
