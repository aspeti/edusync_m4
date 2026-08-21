-- V9__academico_periodo_evaluacion.sql
-- Modulo academico (DD-UC-015 / PR-IMPL-015 / FSD-UC-013): PeriodoEvaluacion, Aggregate
-- independiente de GestionEscolar (mismo criterio que paralelo vs curso). tenant_id
-- redundante por diseño para RLS directa por tabla (DD-UC-015 §2). Sin backfill de
-- gestiones ya persistidas.

CREATE TABLE periodo_evaluacion (
    id                   UUID PRIMARY KEY,
    tenant_id            UUID NOT NULL,
    gestion_escolar_id   UUID NOT NULL REFERENCES gestion_escolar (id),
    nombre               VARCHAR(200) NOT NULL,
    fecha_inicio         DATE NOT NULL,
    fecha_fin            DATE NOT NULL,
    orden                INTEGER NOT NULL,
    estado               VARCHAR(20) NOT NULL,
    creado_en            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_periodo_evaluacion_fechas CHECK (fecha_fin > fecha_inicio),
    CONSTRAINT ck_periodo_evaluacion_orden CHECK (orden >= 1),
    CONSTRAINT ck_periodo_evaluacion_estado CHECK (estado IN ('PENDIENTE', 'ABIERTO', 'CERRADO'))
);

CREATE INDEX idx_periodo_evaluacion_tenant_id ON periodo_evaluacion (tenant_id);
CREATE INDEX idx_periodo_evaluacion_gestion_id ON periodo_evaluacion (gestion_escolar_id);
CREATE UNIQUE INDEX uq_periodo_evaluacion_gestion_orden
    ON periodo_evaluacion (gestion_escolar_id, orden);

ALTER TABLE periodo_evaluacion ENABLE ROW LEVEL SECURITY;
ALTER TABLE periodo_evaluacion FORCE ROW LEVEL SECURITY;

-- MITIGACION OBLIGATORIA: PeriodoEvaluacionRepositoryPort MUST filtrar explicitamente
-- por tenant_id, sin depender solo de esta politica (ADR-0001).
CREATE POLICY tenant_isolation ON periodo_evaluacion
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
