-- V7__academico_materia.sql
-- Modulo academico (DD-UC-012 / PR-IMPL-012): Materia + asignaciones a Curso y a Profesor,
-- tercer feature de negocio real del modulo academico. Tres tablas independientes (no
-- curso_id/profesor_id embebidos en materia, ver DD-UC-012 §2), las tres CON tenant_id
-- obligatorio: mismo patron que curso/paralelo (V6__academico_curso_paralelo.sql).
-- profesor_id es UUID sin FK a usuario: integridad en la capa de aplicacion
-- (ProfesorConsultaPort) para no acoplar esquemas academico e identidad.

CREATE TABLE materia (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    nombre          VARCHAR(100) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_materia_tenant_id ON materia (tenant_id);

ALTER TABLE materia ENABLE ROW LEVEL SECURITY;
ALTER TABLE materia FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON materia
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );

CREATE TABLE asignacion_materia_curso (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    materia_id      UUID NOT NULL REFERENCES materia (id),
    curso_id        UUID NOT NULL REFERENCES curso (id),
    paralelo_id     UUID NOT NULL REFERENCES paralelo (id),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_asignacion_materia_curso_tenant_id ON asignacion_materia_curso (tenant_id);
CREATE INDEX idx_asignacion_materia_curso_materia_id ON asignacion_materia_curso (materia_id);

ALTER TABLE asignacion_materia_curso ENABLE ROW LEVEL SECURITY;
ALTER TABLE asignacion_materia_curso FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON asignacion_materia_curso
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );

CREATE TABLE asignacion_materia_profesor (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    materia_id      UUID NOT NULL REFERENCES materia (id),
    profesor_id     UUID NOT NULL,
    curso_id        UUID NOT NULL REFERENCES curso (id),
    paralelo_id     UUID NOT NULL REFERENCES paralelo (id),
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_asignacion_materia_profesor_tenant_id ON asignacion_materia_profesor (tenant_id);
CREATE INDEX idx_asignacion_materia_profesor_materia_id ON asignacion_materia_profesor (materia_id);

ALTER TABLE asignacion_materia_profesor ENABLE ROW LEVEL SECURITY;
ALTER TABLE asignacion_materia_profesor FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON asignacion_materia_profesor
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
