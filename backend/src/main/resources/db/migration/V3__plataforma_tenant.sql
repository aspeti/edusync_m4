-- V3__plataforma_tenant.sql
-- Modulo plataforma (DD-UC-003 / PR-IMPL-003): alta y gestion de Tenants y Suscripciones.
-- Tabla SIN politica RLS propia: no tiene tenant_id, es la tabla que define los tenants
-- (DD-UC-003 §1).

CREATE TABLE tenant (
    id                              UUID PRIMARY KEY,
    nombre                          VARCHAR(200) NOT NULL,
    fecha_inicio_suscripcion        DATE NOT NULL,
    fecha_vencimiento_suscripcion   DATE NOT NULL,
    estado                          VARCHAR(20) NOT NULL,
    creado_en                       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Usado por el scheduler de vencimiento (TenantRepositoryPort.listarPendientesDeVencer).
CREATE INDEX idx_tenant_estado_fecha_vencimiento ON tenant (estado, fecha_vencimiento_suscripcion);
