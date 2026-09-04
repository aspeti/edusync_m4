-- V12__academico_calificacion_evaluacion.sql
-- Modulo academico (DD-UC-018 / PR-IMPL-018 / FSD-UC-016): CalificacionEvaluacion,
-- Aggregate independiente (evaluacion x estudiante). Unicidad de negocio
-- (tenant_id, evaluacion_id, estudiante_id). tenant_id redundante por diseno
-- para RLS directa por tabla. Sin backfill. Sin floor() (ADR-0013).

CREATE TABLE calificacion_evaluacion (
    id              UUID PRIMARY KEY,
    tenant_id       UUID NOT NULL,
    evaluacion_id   UUID NOT NULL REFERENCES evaluacion (id),
    estudiante_id   UUID NOT NULL REFERENCES estudiante (id),
    valor           NUMERIC(5,2) NOT NULL,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_calificacion_evaluacion_valor CHECK (valor >= 0),
    CONSTRAINT uq_calificacion_evaluacion_tenant_eval_est
        UNIQUE (tenant_id, evaluacion_id, estudiante_id)
);

CREATE INDEX idx_calificacion_evaluacion_tenant_id ON calificacion_evaluacion (tenant_id);
CREATE INDEX idx_calificacion_evaluacion_evaluacion_id ON calificacion_evaluacion (evaluacion_id);
CREATE INDEX idx_calificacion_evaluacion_estudiante_id ON calificacion_evaluacion (estudiante_id);

ALTER TABLE calificacion_evaluacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE calificacion_evaluacion FORCE ROW LEVEL SECURITY;

-- MITIGACION OBLIGATORIA: CalificacionEvaluacionRepositoryPort MUST filtrar
-- explicitamente por tenant_id, sin depender solo de esta politica (ADR-0001).
CREATE POLICY tenant_isolation ON calificacion_evaluacion
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
