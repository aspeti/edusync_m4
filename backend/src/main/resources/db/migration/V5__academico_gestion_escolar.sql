-- V5__academico_gestion_escolar.sql
-- Modulo academico (DD-UC-008 / PR-IMPL-008): GestionEscolar, primer feature de negocio
-- real del modulo academico. Tabla CON tenant_id obligatorio: a diferencia de usuario, no
-- existe un caso SYSADMIN sin tenant -- toda GestionEscolar pertenece a un tenant.

CREATE TABLE gestion_escolar (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    nombre          VARCHAR(200) NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE NOT NULL,
    estado          VARCHAR(20) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_gestion_escolar_tenant_id ON gestion_escolar (tenant_id);

-- Politica RLS (ADR-0001), sin el caso especial "OR tenant_id IS NULL" de usuario
-- (V2__identidad_usuario.sql): toda GestionEscolar pertenece a un tenant.
--
-- MITIGACION OBLIGATORIA: GestionEscolarRepositoryPort (adapter Java) MUST filtrar
-- explicitamente por tenant_id en toda consulta, sin depender solo de esta politica.
ALTER TABLE gestion_escolar ENABLE ROW LEVEL SECURITY;
ALTER TABLE gestion_escolar FORCE ROW LEVEL SECURITY;

-- current_setting(...) puede ser '' (set_config con tenant ausente, TenantAwareDataSource);
-- el CASE evita castear '' a uuid, lo que lanzaria un error en cada consulta sin tenant.
CREATE POLICY tenant_isolation ON gestion_escolar
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
