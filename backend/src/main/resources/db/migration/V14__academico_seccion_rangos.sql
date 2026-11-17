-- V14__academico_seccion_rangos.sql
-- DD-UC-034 / PR-IMPL-034 / ADR-0027: rangoMin/rangoMax en SeccionEvaluacion;
-- retira parametro_periodo (rangos dejan de ser por periodo).

ALTER TABLE seccion_evaluacion
    ADD COLUMN rango_min NUMERIC(5,2),
    ADD COLUMN rango_max NUMERIC(5,2);

UPDATE seccion_evaluacion
SET rango_min = 0,
    rango_max = nota
WHERE rango_min IS NULL OR rango_max IS NULL;

ALTER TABLE seccion_evaluacion
    ALTER COLUMN rango_min SET NOT NULL,
    ALTER COLUMN rango_max SET NOT NULL;

ALTER TABLE seccion_evaluacion
    ADD CONSTRAINT ck_seccion_evaluacion_rango
        CHECK (rango_min >= 0 AND rango_max > rango_min AND rango_max <= nota);

DROP TABLE IF EXISTS parametro_periodo;
