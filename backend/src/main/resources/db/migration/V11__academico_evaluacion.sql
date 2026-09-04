-- V11__academico_evaluacion.sql
-- Modulo academico (DD-UC-017 / PR-IMPL-017 / FSD-UC-015): Evaluacion, Aggregate
-- independiente en (Materia x PeriodoEvaluacion x SeccionEvaluacion).
-- puntaje_maximo es snapshot de seccion.nota (ADR-0013). tenant_id redundante
-- por diseno para RLS directa por tabla. Sin backfill.

CREATE TABLE evaluacion (
    id                       UUID PRIMARY KEY,
    tenant_id                UUID NOT NULL,
    materia_id               UUID NOT NULL REFERENCES materia (id),
    periodo_evaluacion_id    UUID NOT NULL REFERENCES periodo_evaluacion (id),
    seccion_evaluacion_id    UUID NOT NULL REFERENCES seccion_evaluacion (id),
    nombre                   VARCHAR(100) NOT NULL,
    fecha                    DATE NOT NULL,
    puntaje_maximo           NUMERIC(5,2) NOT NULL,
    descripcion              TEXT,
    estado                   VARCHAR(20) NOT NULL,
    creado_en                TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_evaluacion_puntaje_maximo CHECK (puntaje_maximo > 0),
    CONSTRAINT ck_evaluacion_estado CHECK (estado IN ('ACTIVA', 'ANULADA'))
);

CREATE INDEX idx_evaluacion_tenant_id ON evaluacion (tenant_id);
CREATE INDEX idx_evaluacion_materia_id ON evaluacion (materia_id);
CREATE INDEX idx_evaluacion_periodo_id ON evaluacion (periodo_evaluacion_id);

ALTER TABLE evaluacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE evaluacion FORCE ROW LEVEL SECURITY;

-- MITIGACION OBLIGATORIA: EvaluacionRepositoryPort MUST filtrar explicitamente
-- por tenant_id, sin depender solo de esta politica (ADR-0001).
CREATE POLICY tenant_isolation ON evaluacion
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
