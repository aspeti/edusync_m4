# Architecture Decision Record (ADR)

## ADR-0006: Cloud Provider y estilo de despliegue — AWS Cloud Native con ECS Fargate

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0006` |
| Título | Cloud Provider y estilo de despliegue — AWS Cloud Native con ECS Fargate |
| Fecha | 28/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Infraestructura completa del sistema — todos los componentes de EduSync |
| Stakeholders consultados | Directores de unidades educativas, Equipo de arquitectura G-EduSync |

### 1. Contexto

EduSync es una plataforma SaaS B2B multitenant para el mercado boliviano que requiere alta disponibilidad durante los períodos de cierre trimestral (picos de carga predecibles pero intensos), cifrado de datos PII (RUDE, nombre, fecha de nacimiento) en reposo, distribución de contenido estático con baja latencia desde Bolivia, y un modelo de infraestructura que un equipo de uno (Rodrigo + agentes IA) pueda operar sin fricción.

El mercado boliviano tiene conectividad a Internet variable entre ciudades; la latencia hacia servidores en Latinoamérica es significativamente menor que hacia Europa o Asia. El cumplimiento con el formato de exportación al SIE del Ministerio de Educación implica que EduSync es el sistema de registro académico oficial de cada colegio, lo que impone requisitos de disponibilidad y recuperación ante desastres (RTO: 4 horas, RPO: 1 hora) que deben estar documentados y auditables.

Las fuerzas en tensión son: **disponibilidad y DR** vs. **simplicidad operativa para equipo de uno** vs. **costo de infraestructura para el mercado boliviano** vs. **latencia desde Bolivia**.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-----------------|
| A. GCP Cloud Run + Cloud SQL (PostgreSQL) + Firebase Hosting | Serverless nativo; escala a cero; Firebase para CDN; fuerte integración con Google Workspace (usada en algunos colegios bolivianos) | Cloud SQL RLS menos documentado que RDS; menor madurez de Resilience4j en ecosistema GCP; soporte en Bolivia más limitado; KMS equivalente (Cloud KMS) con menor adopción en Spring Boot | Comparable a AWS en costo base; mayor costo de migración desde el stack Java/Spring familiar |
| B. Azure AKS (Kubernetes) + Azure Database for PostgreSQL + Azure CDN | Kubernetes ofrece máxima portabilidad y control; integración nativa con Azure AD (útil si los colegios usan Office 365) | AKS requiere gestión de plano de control Kubernetes — complejidad operativa alta para equipo de uno; Azure Database for PostgreSQL no tiene RLS tan optimizado como RDS PostgreSQL 15; costo de AKS > ECS Fargate para workloads pequeños | Mayor — gestión de plano de control Kubernetes + nodos worker |
| C. AWS ECS Fargate + RDS PostgreSQL 15 Multi-AZ + CloudFront + KMS + SQS + ALB + Secrets Manager, con IaC en Terraform 1.8 | Sin gestión de servidores ni plano de control Kubernetes; escalado automático en picos de cierre trimestral; RDS PostgreSQL 15 con RLS nativo y Multi-AZ para DR; CloudFront con edge en Latinoamérica para baja latencia desde Bolivia; KMS para cifrado PII; stack familiar para Spring Boot 3.3; Terraform 1.8 para IaC reproducible | Lock-in a AWS; la factura puede crecer si el escalado automático no está bien configurado; requiere conocimiento de ECS Fargate + Terraform para el equipo | Medio — estimado $80–$200/mes en configuración base para 10–20 tenants activos |

### 3. Decisión

> **Elegimos la Alternativa C: AWS como cloud provider único con estilo de despliegue Cloud Native, usando ECS Fargate + RDS PostgreSQL 15 Multi-AZ + CloudFront + KMS + SQS + ALB + Secrets Manager, infraestructura definida como código con Terraform 1.8.**

AWS es el proveedor con mayor presencia en el mercado SaaS latinoamericano y el que ofrece el mejor soporte para el stack EduSync (Spring Boot 3.3, PostgreSQL 15 con RLS, cifrado KMS). ECS Fargate elimina la gestión de servidores y permite a un equipo de uno operar contenedores Docker sin gestionar instancias EC2 ni planos de control Kubernetes. CloudFront ofrece edge locations en Sao Paulo (Brasil) con latencia aceptable desde Bolivia (~80–120 ms). La estrategia Warm Standby de RDS Multi-AZ garantiza RPO ≤ 1 hora y RTO ≤ 4 horas sin operación manual.

El estilo de despliegue es **Cloud Native**: la API es stateless (JWT sin sesión en servidor), los contenedores son efímeros (sin estado local), toda la infraestructura está en código Terraform versionado en Git, y los secretos nunca se almacenan en código fuente (Secrets Manager).

### 4. Consecuencias

#### 4.1 Positivas

- Sin gestión de servidores: ECS Fargate gestiona el provisioning de capacidad automáticamente; el equipo no administra EC2 ni AMIs.
- Escalado automático en picos de cierre trimestral: las políticas de escalado de ECS escalan horizontalmente durante los picos y reducen la capacidad en horas valle, optimizando costos.
- RDS PostgreSQL 15 Multi-AZ garantiza failover automático en < 60 segundos ante caída del nodo primario; cumple RPO ≤ 1 hora y RTO ≤ 4 horas.
- CloudFront CDN distribuye el Angular SPA desde edge locations en Latinoamérica, reduciendo la latencia percibida por usuarios bolivianos.
- AWS KMS con la clave `alias/edusync-pii-key` cifra RUDE, nombre y fecha de nacimiento en reposo (NFR-007); el acceso a la clave queda auditado en CloudTrail.
- Terraform 1.8 garantiza infraestructura reproducible: cualquier entorno (dev, stg, prd) se crea con el mismo código; el equipo puede destruir y recrear stg en minutos.

#### 4.2 Negativas / costos

- Lock-in a AWS: migrar a otro cloud provider requeriría reemplazar ECS por un orquestador alternativo, RDS por otra instancia PostgreSQL gestionada, y KMS por otra solución de gestión de claves.
- Costo de infraestructura: la configuración Multi-AZ de RDS es el componente más caro (~$50–$100/mes adicionales sobre una instancia Single-AZ); es el precio de cumplir el RTO de 4 horas.
- La factura AWS puede crecer inesperadamente si el escalado automático de ECS no está correctamente limitado (se requiere presupuesto de CloudWatch Billing Alarm).
- Terraform 1.8 requiere conocimiento del equipo; un error en el estado de Terraform puede destruir recursos críticos.

#### 4.3 Neutras / observables

- Los entornos dev y stg usan instancias Single-AZ para reducir costos; solo prd usa Multi-AZ.
- El Angular SPA se despliega en S3 + CloudFront mediante un pipeline CI/CD separado del backend ECS.
- Los secretos (DB credentials, JWT secret, API keys del SIE) se almacenan en Secrets Manager y se inyectan como variables de entorno en los contenedores ECS; nunca en el código fuente ni en `application.yml` versionado en Git.
- La región `us-east-1` (Virginia, USA) es el punto de presencia más cercano a Bolivia con disponibilidad de todos los servicios AWS requeridos.

### 5. Impacto en el sistema

- **Código**: la API Spring Boot 3.3 se empaqueta como imagen Docker (OpenJDK 21 Alpine); el `Dockerfile` y `docker-compose.yml` viven en la raíz del repo. El Angular SPA se construye con `ng build --configuration production` y se sube a S3.
- **Operaciones**: directorio `infra/` (pendiente de creación) contiene los módulos Terraform para ECS, RDS, CloudFront, KMS, SQS, ALB, Secrets Manager. Los tres entornos (dev/stg/prd) se gestionan con workspaces Terraform o directorios separados. CloudWatch Alarms alertan sobre: p95 > 500 ms (NFR-001), error rate > 1 %, uso de CPU ECS > 80 % durante 5 minutos.
- **Seguridad**: HTTPS/TLS 1.3 en el ALB (NFR-009); AWS WAF en el ALB para protección contra OWASP Top 10; Secrets Manager para rotación automática de credenciales BD cada 30 días; CloudTrail para auditoría de acceso a KMS y Secrets Manager.
- **Equipo**: el equipo debe conocer ECS Fargate, RDS Multi-AZ y Terraform básico. Los pipelines CI/CD (GitHub Actions o equivalente) despliegan automáticamente a stg en cada merge a `main` y a prd en cada tag `release/*`.
- **Costo**: estimado $80–$200/mes para 10–20 tenants activos (RDS Multi-AZ ~$100/mes, ECS Fargate ~$20–$50/mes, CloudFront/S3 ~$5–$10/mes, KMS ~$1/mes, SQS ~$0.40/mes).

### 6. Plan de reversión

- **Señales tempranas de decisión incorrecta**: si la factura AWS supera el presupuesto mensual del producto (> $500/mes) con < 50 tenants, o si la latencia desde Bolivia al edge CloudFront más cercano supera 500 ms de forma consistente.
- **Costo estimado de revertir**: 4–6 semanas para migrar a GCP Cloud Run (Alternativa A), que es el proveedor con mejor relación feature/costo como alternativa; el dominio no cambia; el Dockerfile es portable; solo cambia la infraestructura Terraform y la gestión de secretos.
- **Plan B**: reducir el nivel de disponibilidad en dev y stg a Single-AZ si los costos en esos entornos superan el presupuesto; solo prd mantiene Multi-AZ.

### 7. Validación

- **Test de infraestructura (Terraform plan)**: el pipeline CI ejecuta `terraform plan` en cada PR que toque el directorio `infra/`; un `terraform plan` con cambios destructivos no previstos bloquea el merge.
- **Test de disponibilidad**: CloudWatch Synthetic Canary ejecuta un health check cada 1 minuto contra el endpoint `GET /actuator/health` en prd; alertas si el uptime < 99.9 % mensual (NFR-002).
- **Test de cifrado PII**: `KMSCipherTest` verifica que el campo RUDE almacenado en BD está cifrado (no en texto plano) y que la clave utilizada es `alias/edusync-pii-key` (NFR-007).
- **Test de disaster recovery**: simulación semestral de failover RDS Multi-AZ; verificación de que el sistema es operativo dentro de 4 horas desde la simulación del fallo (RTO ≤ 4 horas).
- **Test de escalado**: prueba de carga k6 con 500 usuarios simultáneos contra el endpoint de registro de calificaciones (UC-01) verifica que ECS escala y mantiene p95 < 500 ms (NFR-001).

### 8. Referencias

- `docs/DTI.md §8` (Despliegue – Cloud Native — tabla de mapeo de componentes a servicios AWS, diagrama de despliegue Mermaid, entornos, estrategia DR).
- `NFR-001` (p95 de registro de calificación < 500 ms — garantizado por escalado automático ECS).
- `NFR-002` (Uptime >= 99.9 % mensual — garantizado por RDS Multi-AZ + ECS Fargate).
- `NFR-003` (OWASP ASVS L2 — HTTPS/TLS 1.3 + AWS WAF + Secrets Manager).
- `NFR-007` (Cifrado PII con AWS KMS `alias/edusync-pii-key`).
- `NFR-009` (HTTPS/TLS 1.3 en tránsito — configuración ALB).
- `ADR-0001` (Multitenancy RLS — RDS PostgreSQL 15 con RLS nativo como prerequisito).
- `ADR-0004` (Consolidación asíncrona — SQS FIFO como destino de migración planificada desde Spring Events).
- `ADR-0005` (Resiliencia SIE — Secrets Manager almacena las credenciales del SIE).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 28/05/2026 | Rodrigo Aspeti | ADR formal creado a partir de docs/DTI.md §8; requerido explícitamente por la rúbrica del Módulo 4 (Criterio 2); estado Aceptada |
