# Runbook — POC-01 RLS Multitenancy

> Pasos repetibles para ejecutar la POC declarada en `README.md`.
> Trazabilidad: `docs/DTI.md §12.1` · `docs/adr/0001-multitenancy-rls-postgresql.md`.
> Todos los comandos son **placeholders**: el proyecto Java/Maven todavia no existe.

---

## Pre-requisitos

- Docker Desktop instalado y corriendo.
- JDK 21 disponible (`java -version`).
- Maven 3.9+ disponible (`mvn -v`).
- 4 CPU + 8 GB RAM libres.

---

## Paso 1 — Preparar Docker / PostgreSQL 15

```bash
# Opcion A: contenedor independiente (debugging manual)
docker run --name edusync-poc01-pg \
  -e POSTGRES_USER=edusync \
  -e POSTGRES_PASSWORD=edusync \
  -e POSTGRES_DB=poc01 \
  -p 5432:5432 \
  -d postgres:15

# Opcion B (preferida): dejarlo a Testcontainers desde el test JUnit.
```

Verificacion:

```bash
docker ps --filter "name=edusync-poc01-pg"
psql "postgresql://edusync:edusync@localhost:5432/poc01" -c "SELECT version();"
```

---

## Paso 2 — Crear schema minimo con `tenant_id`

Migracion Flyway `V001__schema.sql` (placeholder):

```sql
CREATE TABLE calificacion (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     UUID NOT NULL,
  rude          VARCHAR(20) NOT NULL,
  materia_id    UUID NOT NULL,
  periodo_id    UUID NOT NULL,
  valor         INTEGER NOT NULL,
  timestamp_utc TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE centralizador (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id     UUID NOT NULL,
  materia_id    UUID NOT NULL,
  periodo_id    UUID NOT NULL,
  promedio      INTEGER NOT NULL,
  estado        VARCHAR(20) NOT NULL DEFAULT 'PROVISIONAL'
);

CREATE INDEX idx_calif_tenant     ON calificacion (tenant_id);
CREATE INDEX idx_central_tenant   ON centralizador (tenant_id);
```

---

## Paso 3 — Activar RLS y política `tenant_isolation`

Migracion Flyway `V002__rls.sql` (placeholder):

```sql
ALTER TABLE calificacion   ENABLE ROW LEVEL SECURITY;
ALTER TABLE centralizador  ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation ON calificacion
  USING (tenant_id = current_setting('app.tenant_id')::uuid);

CREATE POLICY tenant_isolation ON centralizador
  USING (tenant_id = current_setting('app.tenant_id')::uuid);

-- Default opcional para evitar leaks por falta de SET LOCAL:
ALTER DATABASE poc01 SET app.tenant_id TO '00000000-0000-0000-0000-000000000000';
```

Verificacion:

```sql
SELECT relname, relrowsecurity, relforcerowsecurity
FROM pg_class
WHERE relname IN ('calificacion','centralizador');
```

---

## Paso 4 — Ejecutar 1000 requests mixtos por 2 tenants

Codigo Java de la POC todavia no existe (placeholder de comando):

```bash
mvn -pl pocs/POC-01-rls-multitenancy -am clean test \
  -Dtest=MultitenantTest \
  -Dpoc.requests=1000 \
  -Dpoc.threads=200 \
  -Dpoc.tenantA=11111111-1111-1111-1111-111111111111 \
  -Dpoc.tenantB=22222222-2222-2222-2222-222222222222
```

Notas:

- Cada hilo abre conexion HikariCP, ejecuta `SET LOCAL app.tenant_id = ?`, luego INSERT o SELECT.
- Mezcla 50 % Tenant A / 50 % Tenant B.
- Mezcla 50 % INSERT / 50 % SELECT.

---

## Paso 5 — Capturar métricas p95/p99 y leaks cross-tenant

Salida esperada del test:

```text
== MultitenantTest ==
Total ops              : 1000
Throughput             : <tps>
p50 latency (ms)       : <num>
p95 latency (ms)       : <num>
p99 latency (ms)       : <num>
Cross-tenant leaks (A) : <0 esperado>
Cross-tenant leaks (B) : <0 esperado>
Verdict                : PASS | FAIL
```

Verificacion adicional via SQL (desde sesion de Tenant A):

```sql
SET LOCAL app.tenant_id = '11111111-1111-1111-1111-111111111111';
SELECT COUNT(*) FROM calificacion;   -- debe contar solo filas de TENANT_A
SELECT COUNT(*) FROM centralizador;  -- idem
```

---

## Paso 6 — Guardar evidencias en `evidencia/`

Archivos minimos a producir (ver `evidencia/README.md`):

- `metrics.csv`
- `test-output.txt`
- `p95-latency.txt`
- `cross-tenant-leak-count.txt`
- (opcional) `pg-rls-check.sql.out` con verificacion del Paso 3.
- (opcional) capturas de pantalla del log del test.

---

## Plan de reversión

Si la POC falla (leaks > 0 o p95 >= 600 ms):

1. Documentar el modo de fallo en `README.md §10` y `§11`.
2. Abrir issue para evaluar Alternativa A de `ADR-0001` (schema separado por tenant).
3. Bloquear merge a `release/2.0.0` hasta resolver.

---

## Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1 | 28/05/2026 | Rodrigo Aspeti | runbook inicial con 6 pasos placeholder |
