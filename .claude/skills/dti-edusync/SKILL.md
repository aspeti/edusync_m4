---
name: dti-edusync
description: >-
  Puebla y mantiene el Documento Tecnico Inicial de EduSync (docs/DTI.md)
  siguiendo plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE.md. Mantiene
  sincronizacion atomica ADR <-> DTI <-> AGENTS.md. Activar cuando el usuario
  pide "poblar seccion N del DTI", "actualizar el DTI", o cuando un ADR
  nuevo requiere reflejar una decision arquitectonica en el documento.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v1.0
status: stable
owner: G-EduSync
---

# Skill: dti-edusync — Poblar y mantener el DTI de EduSync

> Skill en `.cursor/skills/dti-edusync/` y `.claude/skills/dti-edusync/`.
> Activar con: `@dti-edusync §N <tema>` o `@dti-edusync sincronizar AGENTS.md`

## 1. Cuándo activarlo

- Usuario pide "pobla §N del DTI" o "genera el DTI de EduSync".
- Se crea o actualiza un ADR en `docs/adr/` y necesita reflejarse en el DTI.
- `AGENTS.md` cambió y el DTI debe sincronizarse.
- **NO activar** mientras el FSD no esté en v1.0 mínimo.

---

## 2. Entradas obligatorias

| Campo | Fuente | Estado en EduSync |
|-------|--------|-------------------|
| Sección(es) del DTI a poblar | Usuario indica `§N` | `docs/DTI.md` existe v0.1 |
| FSD vigente | `docs/fsd/FSD_EduSync.md` | v1.0 - disponible |
| LFSD vigente | `docs/LFSD-EduSync.md` | v1.0 - disponible |
| Stack autoritativo | `docs/AGENTS.md` §4 | Java 21, Spring Boot 3.3, PostgreSQL 15 |
| C4 diagramas | `docs/diagrams/c4_level*.mmd` | Level 1 y Level 2 generados |

Si falta el FSD: `"Necesito docs/fsd/FSD_EduSync.md antes de poblar el DTI."`

---

## 3. Fuentes de verdad (orden de precedencia)

1. `plantillas/DOCUMENTO_TECNICO_INICIAL_TEMPLATE (1).md` — estructura y frontmatter.
2. `docs/LFSD-EduSync.md` — diseño técnico de bajo nivel (packages, DDL, APIs, secuencias).
3. `docs/fsd/FSD_EduSync.md` — FSD-UC-001, 003, 004, 005, 009 + BR-001..BR-012 + NFRs.
4. `docs/AGENTS.md` v0.2 — stack, agentes, guardrails, golden tests.
5. `docs/arquitectura_funcional_EduSync.md` — DA-01..DA-05.
6. `docs/PROMPT_MAPPING.md` v0.6 — 20 prompt-contratos.
7. `docs/brd/BRD_EduSync_v2.md`, `docs/mrd/MRD_EduSync.md`, `docs/prd/PRD_EduSync.md`.

---

## 4. Mapa de secciones DTI — datos clave de EduSync

| Sección | Tag | Datos principales EduSync |
|---------|-----|--------------------------|
| §0 Metadatos | [máquina] | Producto: EduSync, Grupo: G-EduSync, Stack: Java 21 / Spring Boot 3.3 / PostgreSQL 15 / Angular 17 |
| §0.1 Rol agentes IA SDLC | [máquina] | 6 agentes: docs-agent, dev-agent, arch-agent, qa-agent, process-agent, compliance-agent |
| §1 Vision | [humano] | Eliminar triple digitacion manual; SaaS B2B multitenant Bolivia; North Star: <10 min cierre |
| §2.1 C4 Level 1 | [humano+máquina] | Embed `docs/diagrams/c4_level1.mmd` |
| §2.2 Actores | [humano+máquina] | Director, Docente, Secretaria, SIE (sistema), AWS KMS (sistema) |
| §3.1 Estilo arq. | [humano+máquina] | Hexagonal + Event-driven; DA-01 (multitenancy RLS), DA-02 (hexagonal), DA-04 (async) |
| §3.2 C4 Level 2 | [humano+máquina] | Embed `docs/diagrams/c4_level2.mmd` |
| §3.3 C4 Level 3 | [humano+máquina] | api-gateway: embed `docs/diagrams/c4_level3_api_gateway.mmd` (pendiente) |
| §3.4 Data Flow | [humano+máquina] | Sequence diagram FSD-UC-001 (registro calificacion) del LFSD §8.1 |
| §3.5 Contenedores agénticos | [humano+máquina] | N/A — EduSync v1.0 no tiene agentes en runtime; IA solo en construccion |
| §4 Modelo dominio | [humano+máquina] | 5 BCs, 14 entidades, DTOs del LFSD §4-§5 |
| §5 Arch hexagonal | [humano+máquina] | Puertos y adaptadores del LFSD §2-§3 |
| §6 Distribuida | [humano+máquina] | Monolito modular; circuit breaker SIE (DA-05); RLS PostgreSQL (DA-01) |
| §7 Asincrona | [humano+máquina] | Eventos: CalificacionRegistradaEvent, MateriaCerradaEvent, CentralizadorOficialEvent (DA-04) |
| §8 Despliegue AWS | [humano+máquina] | ECS Fargate, RDS Multi-AZ, SQS, KMS, CloudFront |
| §9 Capa IA | [humano+máquina] | AI-SDLC multi-agente SOLO en construccion; NO en runtime v1.0 |
| §10 Prompt Mapping | [máquina] | Referencia `docs/PROMPT_MAPPING.md` v0.6 (20 contratos) |
| §11 NFRs | [máquina] | NFR-001..016 del FSD (p95 < 500 ms, uptime 99.9%, OWASP ASVS L2) |
| §12 POCs | [humano+máquina] | POC-01: RLS multitenancy; POC-02: SIE circuit breaker |
| §13 Seguridad | [humano+máquina] | OWASP ASVS L2, JWT 8h, RBAC, KMS, Ley 164 Bolivia, `.cursor/rules/seguridad.mdc` |
| §14 Observabilidad | [humano+máquina] | audit_log append-only, AuditLogAspect AOP, structured logs, CloudWatch |
| §16 Antipatrones | [humano] | floor() fuera de dominio, audit_log fuera de TX, tenant_id sin RLS |
| §17 Trade-offs | [humano] | DA-01..DA-05 del LFSD |
| §21 ADRs | [máquina] | DA-01..DA-05 -> pendientes de formalizacion en `docs/adr/` |
| §22 Auditoria IA | [humano+máquina] | audit_log, AuditLogAspect, politica de retencion |
| §23 Eval agentes | [humano+máquina] | 4 golden tests (FloorTest, SIEPayloadTest, VentanaTest, MultitenantTest) |

---

## 5. Procedimiento

1. **Leer** `docs/DTI.md` para detectar qué secciones ya están pobladas.
2. **Identificar audiencia** de cada sección (`[humano]`, `[máquina]`, `[humano+máquina]`):
   - `[máquina]`: YAML o tablas semánticas — sin prosa ambigua.
   - `[humano]`: prosa estructurada con trade-offs y riesgos.
   - `[humano+máquina]`: narrativa breve + tabla o código.
3. **Poblar** usando datos reales del §4 de este skill (no inventar).
4. **Detectar drift** con `AGENTS.md`:
   - Si la sección cambia stack o invariante → proponer diff de `AGENTS.md` en el mismo commit.
5. **Aplicar checklist** de §6 antes de entregar.

---

## 6. Verificacion (bien hecho)

- [ ] Frontmatter YAML presente y parseable: `producto`, `version`, `stack`, `audiencia`.
- [ ] Tag de audiencia correcto en cada sección.
- [ ] Cada decision arquitectonica cita su DA/ADR (`[DA-01]`, `[ADR-NNNN]`).
- [ ] C4 Level 1 y Level 2 embebidos o referenciados.
- [ ] §0.1 tabla de agentes SDLC completa y sincronizada con `AGENTS.md`.
- [ ] §3.5 marcado `N/A` con justificacion (no hay agentes en runtime v1.0).
- [ ] §9 describe AI-SDLC en construccion, no runtime.
- [ ] §11 cita los 16 NFRs del FSD con umbral y mecanismo.
- [ ] §22 declara `audit_log` como mecanismo de auditoria de decisions IA.
- [ ] §23 lista los 4 golden tests de `AGENTS.md` §8.3.
- [ ] Cero secretos ni PII en el documento.
- [ ] `AGENTS.md` y DTI coherentes (misma version del stack).

---

## 7. Anti-patrones DTI EduSync

| Anti-patron | Mitigacion |
|-------------|-----------|
| Sección poblada sin ADR fuente | Cada decision significativa requiere DA-NN o ADR-NNNN |
| §3.5 con contenedores agénticos cuando no existen en runtime | EduSync v1.0: marcar N/A con justificacion explícita |
| `floor()` en DTI como "detalle de implementacion" | Es invariante de dominio BR-008; citar DA-02 |
| DTI desincronizado de AGENTS.md | Usar commit atomico: `docs(dti+agents): <decision> [DA-NN]` |
| NFRs sin umbral medible | Todo NFR requiere threshold + mecanismo de verificacion |

---

## 8. Mini ejemplo de invocacion

```
@dti-edusync §3 - Arquitectura de alto nivel

Fuentes: docs/LFSD-EduSync.md §2, docs/AGENTS.md §4, docs/diagrams/c4_level2.mmd
Decisiones: DA-01 (multitenancy RLS), DA-02 (hexagonal), DA-04 (async consolidacion)
Proponer diff AGENTS.md si alguna decision cambia el stack.
```

---

## 9. Modos de fallo conocidos

- ADR en estado `propuesto` (no `aceptado`) -> STOP antes de reflejarlo en el DTI.
- FSD y LFSD contradicen una decision -> STOP, escalar; no resolver por cuenta propia.
- `§3.5` intenta modelar agentes IA runtime que no existen en EduSync v1.0 -> marcar N/A.
- Seccion poblada con datos del template (placeholders) sin reemplazar -> rechazar entrega.

---

## 10. Registro de cambios

| Version | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 0.1.0 | 17/05/2026 | Rodrigo Aspeti | Version inicial — adaptacion de plantillas/dti-author.md al proyecto EduSync; mapa de 25 secciones con datos reales del stack, agentes y decisiones arquitectonicas |
