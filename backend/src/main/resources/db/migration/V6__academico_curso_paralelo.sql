-- V6__academico_curso_paralelo.sql
-- Modulo academico (DD-UC-010 / PR-IMPL-010): Curso y Paralelo, segundo feature de negocio
-- real del modulo academico. Dos tablas independientes (no Paralelo embebido en Curso, ver
-- DD-UC-010 §2), ambas CON tenant_id obligatorio: mismo patron que gestion_escolar
-- (V5__academico_gestion_escolar.sql), sin el caso SYSADMIN sin tenant de usuario.

CREATE TABLE curso (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_curso_tenant_id ON curso (tenant_id);

-- Politica RLS (ADR-0001), sin el caso especial "OR tenant_id IS NULL" de usuario:
-- todo Curso pertenece a un tenant.
--
-- MITIGACION OBLIGATORIA: CursoRepositoryPort (adapter Java) MUST filtrar explicitamente
-- por tenant_id en toda consulta, sin depender solo de esta politica.
ALTER TABLE curso ENABLE ROW LEVEL SECURITY;
ALTER TABLE curso FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON curso
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );

-- tenant_id es redundante respecto al diccionario de datos del FSD (derivable via join
-- contra curso), añadido deliberadamente para mantener RLS directa por tabla sin depender
-- de un JOIN para el aislamiento (decision de bajo riesgo, ver DD-UC-010 §2).
CREATE TABLE paralelo (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    curso_id        UUID NOT NULL REFERENCES curso (id),
    nombre          VARCHAR(50) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_paralelo_tenant_id ON paralelo (tenant_id);
CREATE INDEX idx_paralelo_curso_id ON paralelo (curso_id);

ALTER TABLE paralelo ENABLE ROW LEVEL SECURITY;
ALTER TABLE paralelo FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON paralelo
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
