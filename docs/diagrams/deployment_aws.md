---
producto: "EduSync"
grupo: "G-EduSync"
diagrama: "deployment_aws"
nivel: "C4 - Deployment"
version: v0.1.0
fecha: "28/05/2026"
autor: "Rodrigo Aspeti"
estado: borrador
prompt: "PR-C4-006 (registrado en docs/PROMPT_MAPPING.md v1.5)"
skill: ".cursor/skills/c4-edusync/SKILL.md v0.3.0"
fuente_principal: "docs/adr/0006-cloud-provider-y-estilo-de-despliegue.md"
fuentes_secundarias:
  - "docs/DTI.md v0.4 §8"
  - "docs/adr/0001-multitenancy-rls-postgresql.md"
  - "docs/adr/0004-async-consolidacion-spring-events.md"
  - "docs/adr/0005-resiliencia-integracion-sie-resilience4j.md"
artefacto_mermaid: "docs/diagrams/deployment_aws.mmd"
---

# C4 Deployment - AWS (EduSync)

> Diagrama de despliegue AWS para EduSync, alineado con ADR-0006 y DTI §8. Este artefacto cubre explícitamente el criterio de defensa: "mapeo a AWS con justificación por servicio".

---

## 1. Alcance

Este diagrama representa el estado objetivo `release/2.0.0`: monolito modular stateless en ECS Fargate, Angular SPA en S3/CloudFront, PostgreSQL 15 Multi-AZ con RLS, mensajería SQS FIFO como evolución de Spring Events, KMS/Secrets Manager para seguridad, CloudWatch para observabilidad y ALB/WAF para entrada HTTPS.

El diagrama no implica que `infra/` ya exista; ADR-0006 declara Terraform 1.8 como mecanismo objetivo y DTI §8 marca IaC como pendiente de creación.

---

## 2. Mapeo servicio AWS -> responsabilidad

| Servicio AWS | Componente EduSync | Justificación |
|--------------|--------------------|---------------|
| CloudFront + S3 | `Angular SPA` | Entrega estática con baja latencia para Bolivia. |
| ALB + WAF | Entrada HTTPS | TLS 1.3, routing por tenant subdomain, rate limiting y OWASP Top 10. |
| ECS Fargate | `api-gateway`, `domain-layer`, adapters y schedulers | Sin gestión de servidores; adecuado para equipo pequeño. |
| RDS PostgreSQL 15 Multi-AZ | `postgres-rls` | ACID, RLS nativo, failover y DR. |
| SQS FIFO + DLQ | `event-bus` v1.1+ | Orden por `periodoId`, retries y DLQ para eventos fallidos. |
| AWS KMS | Cifrado PII | Cifra RUDE, nombre y fecha de nacimiento en reposo. |
| Secrets Manager | Secretos runtime | DB credentials, JWT keys, credenciales SIE. |
| CloudWatch Logs/Metrics/Synthetics | Observabilidad | p95, error rate, circuit breaker state, uptime. |
| CloudTrail | Auditoría cloud | Acceso a KMS y Secrets Manager. |
| Terraform 1.8 | IaC | Infraestructura reproducible y revisable por PR. |

---

## 3. Trazabilidad con ADR y NFR

| Decisión / NFR | Nodo del deployment | Evidencia |
|----------------|---------------------|-----------|
| ADR-0006 | AWS Cloud, ECS, RDS, CloudFront, KMS, SQS, ALB, Secrets Manager | Cloud provider y estilo de despliegue aceptados. |
| ADR-0001 / DA-01 | RDS PostgreSQL 15 | RLS multitenant en BD. |
| ADR-0004 / DA-04 | SQS FIFO | Evolución planificada del event bus. |
| ADR-0005 / DA-05 | SIE external + CloudWatch metrics | Resilience4j y reintentos para SIE. |
| NFR-001 | ECS autoscaling + CloudWatch p95 | p95 registro calificación < 500 ms. |
| NFR-002 | RDS Multi-AZ + Canary | Uptime >= 99.9 %. |
| NFR-003 | WAF, Secrets Manager, no PII logs | Seguridad OWASP ASVS L2. |
| NFR-007 | KMS | Cifrado PII en reposo. |
| NFR-009 | ALB HTTPS/TLS 1.3 | Transporte seguro. |

---

## 4. Reporte de validate

- [x] Cabecera Mermaid `C4Deployment`.
- [x] Cada nodo tiene tecnología/servicio explícito.
- [x] Cada relación declara protocolo: HTTPS, JDBC/TLS, AWS SDK/TLS, CloudWatch API, Terraform plan/apply.
- [x] El mapeo AWS se alinea con ADR-0006.
- [x] No introduce microservicios productivos en v1.0; mantiene monolito modular sobre ECS Fargate.
- [x] SQS queda marcado como evolución v1.1+ del event bus, no como dependencia obligatoria de v1.0.
- [x] No expone PII en logs; KMS y Secrets Manager son servicios separados.
- [x] Terraform se muestra como mecanismo objetivo con `infra/` pendiente, sin afirmar que ya existe.

---

## 5. Gaps conscientes

| Gap | Tratamiento |
|-----|-------------|
| `infra/` aún no existe. | Se marca como pendiente y se representa como `Terraform state` externo objetivo. |
| SQS FIFO es evolución v1.1+. | El diagrama lo muestra como target planificado, coherente con DTI §8 y ADR-0004. |
| No hay métricas ejecutadas de AWS. | No se inventan resultados; solo se declaran alarmas y synthetic canary objetivo. |

---

## 6. Registro de cambios

| Versión | Fecha | Autor | Cambio | Documentos base |
|---------|-------|-------|--------|-----------------|
| 0.1.0 | 28/05/2026 | Rodrigo Aspeti | Versión inicial generada por `c4-edusync` v0.3.0. Cubre CloudFront/S3, ALB/WAF, ECS Fargate, RDS Multi-AZ, SQS FIFO/DLQ, KMS, Secrets Manager, CloudWatch, CloudTrail y Terraform. | ADR-0006, DTI v0.4 §8, ADR-0001, ADR-0004, ADR-0005 |
