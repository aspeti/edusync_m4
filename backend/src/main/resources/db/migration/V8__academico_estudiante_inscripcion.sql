-- V8__academico_estudiante_inscripcion.sql
-- Modulo academico (DD-UC-013 / PR-IMPL-013): Estudiante e Inscripcion, cuarto feature de
-- negocio real del modulo academico. Dos tablas independientes (no Inscripcion embebida en
-- Estudiante, BR-023 / DD-UC-013 §2), ambas CON tenant_id obligatorio: mismo patron que
-- curso/paralelo (V6) y materia (V7). Unique (tenant_id, rude) aplica BR-004/RB-01.
-- Unique (tenant_id, estudiante_id, gestion_escolar_id) aplica A1 E_INSCRIPCION_DUPLICADA.
-- inscripcion.tenant_id es redundante (derivable por join), igual que paralelo.tenant_id.

CREATE TABLE estudiante (
    id                 UUID PRIMARY KEY,
    tenant_id          UUID NOT NULL,
    rude               VARCHAR(20) NOT NULL,
    nombre_completo    VARCHAR(200) NOT NULL,
    estado             VARCHAR(20) NOT NULL,
    datos_personales   JSONB,
    creado_en          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_estudiante_tenant_rude UNIQUE (tenant_id, rude)
);

CREATE INDEX idx_estudiante_tenant_id ON estudiante (tenant_id);

ALTER TABLE estudiante ENABLE ROW LEVEL SECURITY;
ALTER TABLE estudiante FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON estudiante
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );

CREATE TABLE inscripcion (
    id                    UUID PRIMARY KEY,
    tenant_id             UUID NOT NULL,
    estudiante_id         UUID NOT NULL REFERENCES estudiante (id),
    gestion_escolar_id    UUID NOT NULL REFERENCES gestion_escolar (id),
    curso_id              UUID NOT NULL REFERENCES curso (id),
    paralelo_id           UUID NOT NULL REFERENCES paralelo (id),
    fecha_inscripcion     DATE NOT NULL,
    estado                VARCHAR(20) NOT NULL,
    creado_en             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_inscripcion_estudiante_gestion UNIQUE (tenant_id, estudiante_id, gestion_escolar_id)
);

CREATE INDEX idx_inscripcion_tenant_id ON inscripcion (tenant_id);
CREATE INDEX idx_inscripcion_estudiante_id ON inscripcion (estudiante_id);

ALTER TABLE inscripcion ENABLE ROW LEVEL SECURITY;
ALTER TABLE inscripcion FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON inscripcion
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
