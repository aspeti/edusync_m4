-- V2__identidad_usuario.sql
-- Modulo identidad (DD-UC-002 / PR-IMPL-002): Usuario/UsuarioRol y politica RLS de la
-- primera tabla plataforma-scoped (sin tenant_id propio en el caso SYSADMIN, ADR-0010).

CREATE TABLE usuario (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE usuario_rol (
    id          UUID PRIMARY KEY,
    usuario_id  UUID NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    rol         VARCHAR(50) NOT NULL,
    CONSTRAINT uq_usuario_rol UNIQUE (usuario_id, rol)
);

CREATE INDEX idx_usuario_rol_usuario_id ON usuario_rol (usuario_id);
CREATE INDEX idx_usuario_tenant_id ON usuario (tenant_id);

-- Politica RLS (ADR-0001), extendida con "OR tenant_id IS NULL" para permitir que las
-- filas SYSADMIN (tenant_id nulo) sean visibles durante el propio flujo de login, antes
-- de que exista un tenant activo en la sesion (DD-UC-002 §2, alternativa D).
--
-- MITIGACION OBLIGATORIA: esta politica por si sola NO oculta las filas SYSADMIN de un
-- Admin de tenant ya autenticado (ambas condiciones del OR se evaluan independientemente).
-- UsuarioRepositoryPort (adapter Java) MUST filtrar explicitamente por tenant_id en toda
-- consulta que no sea el propio flujo de login, sin depender solo de esta politica.
ALTER TABLE usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE usuario FORCE ROW LEVEL SECURITY;

-- current_setting(...) puede ser '' (set_config con tenant ausente, TenantAwareDataSource);
-- el CASE evita castear '' a uuid, lo que lanzaria un error en cada consulta sin tenant.
CREATE POLICY tenant_isolation ON usuario
    USING (
        tenant_id IS NULL
        OR (
            current_setting('app.current_tenant', true) IS NOT NULL
            AND current_setting('app.current_tenant', true) <> ''
            AND tenant_id = current_setting('app.current_tenant', true)::uuid
        )
    );
