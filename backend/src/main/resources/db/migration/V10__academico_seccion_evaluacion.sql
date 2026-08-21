-- V10__academico_seccion_evaluacion.sql
-- Modulo academico (DD-UC-016 / PR-IMPL-016 / FSD-UC-014): SeccionEvaluacion, Aggregate
-- independiente de GestionEscolar (plantilla de la gestion, no del periodo; ADR-0013).
-- tenant_id redundante por diseño para RLS directa por tabla. Sin backfill de
-- gestiones ya persistidas.

CREATE TABLE seccion_evaluacion (
    id                   UUID PRIMARY KEY,
    tenant_id            UUID NOT NULL,
    gestion_escolar_id   UUID NOT NULL REFERENCES gestion_escolar (id),
    nombre               VARCHAR(50) NOT NULL,
    orden                INTEGER NOT NULL,
    nota                 NUMERIC(5,2) NOT NULL,
    creado_en            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_seccion_evaluacion_orden CHECK (orden >= 1),
    CONSTRAINT ck_seccion_evaluacion_nota CHECK (nota > 0 AND nota <= 100)
);

CREATE INDEX idx_seccion_evaluacion_tenant_id ON seccion_evaluacion (tenant_id);
CREATE INDEX idx_seccion_evaluacion_gestion_id ON seccion_evaluacion (gestion_escolar_id);
CREATE UNIQUE INDEX uq_seccion_evaluacion_gestion_orden
    ON seccion_evaluacion (gestion_escolar_id, orden);

ALTER TABLE seccion_evaluacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE seccion_evaluacion FORCE ROW LEVEL SECURITY;

-- MITIGACION OBLIGATORIA: SeccionEvaluacionRepositoryPort MUST filtrar explicitamente
-- por tenant_id, sin depender solo de esta politica (ADR-0001).
CREATE POLICY tenant_isolation ON seccion_evaluacion
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
