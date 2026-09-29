-- V13__academico_parametro_periodo.sql
-- DD-UC-029 / PR-IMPL-029 / FSD-UC-009: parametros por (periodo, seccion).
-- Sin FK a seccion_evaluacion: ADR-0014 permite reemplazar la plantilla con un
-- periodo ya abierto, y el snapshot de parametros no debe bloquear ese PUT.
-- El periodo si tiene FK; al borrar el periodo se borran sus parametros.

CREATE TABLE parametro_periodo (
    id                      UUID PRIMARY KEY,
    tenant_id               UUID NOT NULL,
    periodo_evaluacion_id   UUID NOT NULL REFERENCES periodo_evaluacion (id) ON DELETE CASCADE,
    seccion_evaluacion_id   UUID NOT NULL,
    rango_min               NUMERIC(5,2) NOT NULL,
    rango_max               NUMERIC(5,2) NOT NULL,
    regla_combinacion       VARCHAR(30) NOT NULL,
    creado_en               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_parametro_periodo_rango CHECK (rango_min >= 0 AND rango_max > rango_min),
    CONSTRAINT ck_parametro_periodo_regla CHECK (regla_combinacion IN ('PROMEDIO_SIMPLE'))
);

CREATE INDEX idx_parametro_periodo_tenant_id ON parametro_periodo (tenant_id);
CREATE UNIQUE INDEX uq_parametro_periodo_periodo_seccion
    ON parametro_periodo (tenant_id, periodo_evaluacion_id, seccion_evaluacion_id);

ALTER TABLE parametro_periodo ENABLE ROW LEVEL SECURITY;
ALTER TABLE parametro_periodo FORCE ROW LEVEL SECURITY;

-- MITIGACION OBLIGATORIA: ParametroPeriodoRepositoryPort MUST filtrar explicitamente
-- por tenant_id, sin depender solo de esta politica (ADR-0001).
CREATE POLICY tenant_isolation ON parametro_periodo
    USING (
        current_setting('app.current_tenant', true) IS NOT NULL
        AND current_setting('app.current_tenant', true) <> ''
        AND tenant_id = current_setting('app.current_tenant', true)::uuid
    );
