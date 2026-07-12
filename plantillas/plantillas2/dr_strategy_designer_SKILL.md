---
name: dr-strategy-designer
description: >
  Diseña la estrategia de Disaster Recovery (DR) y multi-AZ /
  multi-región de un producto cloud-native. Aplica RPO/RTO,
  compara las 4 estrategias estándar (Backup-Restore, Pilot Light,
  Warm Standby, Multi-site Active-Active), mapea servicios AWS
  específicos para DR (Backup, Route53, RDS cross-region replicas,
  DynamoDB Global Tables, S3 CRR, Global Accelerator), estima costo
  y produce matriz comparativa + recomendación justificada + ADR
  esqueleto para el DTI.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: dr-strategy-designer (RPO/RTO + 4 estrategias DR + servicios AWS)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/dr-strategy-designer/` o a
> `.claude/skills/dr-strategy-designer/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: definición de §8 del DTI (despliegue AWS), redacción del ADR `0005-cloud-provider-y-estilo-de-despliegue.md`, planificación de continuidad de negocio, conversación sobre SLAs vs costos.
- ARRANCA cuando: el usuario invoca `"@dr-strategy-designer"`, abre `docs/adr/<n>-dr.md`, o pide "estrategia de DR / disaster recovery / multi-región / RPO RTO".
- NO ACTIVAR cuando: aún no se ha decidido la arquitectura base de despliegue (multi-AZ mínimo); cuando no hay SLA objetivo declarado por el negocio.

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **SLA objetivo** del producto (ej. 99.9 %, 99.99 %).
- **RPO objetivo** (Recovery Point Objective: cuánto dato como máximo se puede perder, ej. 5 min, 1 h, 24 h).
- **RTO objetivo** (Recovery Time Objective: cuánto tiempo como máximo puede estar caído, ej. 15 min, 1 h, 4 h).
- **Presupuesto mensual** aproximado para infra (rango USD/mes).
- **Criticidad por servicio o por dominio**: P0 (no aceptable que caiga) / P1 (degradación tolerable unas horas) / P2 (best-effort).
- **Región primaria** (típico módulo: `us-east-1`) y **región secundaria** candidata.
- **Tipo de datos** que se replican: transaccional (BD relacional), NoSQL, archivos (S3), eventos en stream.

Si falta cualquiera de SLA/RPO/RTO/criticidad/presupuesto, responder:
`"Necesito el SLA objetivo (ej. 99.9 %), RPO (ej. 15 min), RTO (ej. 1 h), criticidad por servicio (P0/P1/P2) y presupuesto mensual aproximado antes de diseñar la estrategia DR."`

## 3. Fuentes de verdad (orden de precedencia)

1. NFRs del PRD (disponibilidad, RPO/RTO si están declarados).
2. ADRs vigentes (cloud provider, runtime, BD).
3. DTI §6 (catálogo de servicios) y §8 (despliegue).
4. `AGENTS.md` del repo del producto (si existe; restricciones operativas y de cumplimiento).
5. Literatura recomendada opcional: AWS Well-Architected — Reliability Pillar; AWS *Disaster Recovery of Workloads on AWS* whitepaper.

## 4. Procedimiento

1. **Verificar inputs**. Si falta SLA/RPO/RTO/criticidad, STOP con el mensaje del paso 2.
2. **Establecer fundamentos** y compartirlos con el usuario brevemente:
   - **RPO (Recovery Point Objective)**: cuánto dato puedes perder. Se mide en tiempo "para atrás" desde el desastre (ej. RPO 15 min = puedes perder los últimos 15 min de transacciones).
   - **RTO (Recovery Time Objective)**: cuánto tiempo puedes estar caído antes de recuperar (ej. RTO 1 h = a la 1 h max debes estar arriba).
   - **SLA → indisponibilidad permitida al año**: 99.9 % = 8.77 h/año; 99.99 % = 52.6 min/año; 99.999 % = 5.26 min/año.
   - **Multi-AZ vs multi-región**:
     - Multi-AZ: AZs dentro de una misma región AWS. Latencia inter-AZ < 2 ms. Protege contra falla de hardware/datacenter individual. **Base obligatoria** para cualquier producto serio en AWS; no es estrategia DR per se sino HA (High Availability).
     - Multi-región: regiones AWS distintas. Latencia inter-región decenas a centenas de ms. Protege contra falla regional completa, eventos climáticos, problemas de proveedor cloud por región. **Aquí empieza DR de verdad**.
3. **Aplicar las 4 estrategias estándar**. Evaluar cuál encaja con los RPO/RTO declarados:

   | Estrategia | RPO típico | RTO típico | Costo mensual relativo | Idea principal |
   |------------|-----------|-----------|------------------------|----------------|
   | **Backup-Restore** | horas a 24 h | horas a 24 h | $ (más barato) | Backups regulares a S3 + Glacier; en desastre restauras infra desde IaC y datos desde backup. Casi todo está apagado en la región DR. |
   | **Pilot Light** | minutos | 10–30 min | $$ | Núcleo crítico (BD con réplica continua, AMIs preparadas) prendido en región DR; el resto se arranca al fallover. |
   | **Warm Standby** | segundos a minutos | < 15 min | $$$ | Versión "reducida pero funcional" del stack corriendo en región DR (capacidad menor); en fallover se escala. |
   | **Multi-site Active-Active** | ~0 (segundos) | ~0 (segundos) | $$$$ (más caro) | Stack completo corriendo en ambas regiones, tráfico balanceado (Route53 latency / Global Accelerator). Replicación bidireccional o partición geográfica de datos. |

4. **Mapear a servicios AWS concretos**:

   | Capa | Backup-Restore | Pilot Light | Warm Standby | Multi-site |
   |------|----------------|-------------|--------------|-----------|
   | **Compute** | AMIs / IaC en S3 | núcleo prendido (1 task Fargate / 1 EC2) | stack reducido prendido (n/2 capacity) | stack completo prendido (n capacity en cada región) |
   | **BD relacional** | RDS snapshots automatizadas + AWS Backup | RDS Cross-Region Read Replica (continuo) | RDS Cross-Region Read Replica + promote ready | Aurora Global Database (escritura región primaria, lectura global, < 1 s replicación) o RDS con réplica promovible y app activa/pasiva |
   | **NoSQL** | DynamoDB on-demand backups + PITR | DynamoDB Global Tables (modo single-region writer) | DynamoDB Global Tables | DynamoDB Global Tables (multi-region writer) |
   | **Object storage** | S3 versioning + lifecycle a Glacier | S3 Cross-Region Replication (CRR) | S3 CRR | S3 CRR + Multi-Region Access Points |
   | **DNS / failover** | manual (cambio de DNS al detectar caída) | Route53 health checks + DNS failover automático | Route53 health checks + DNS failover | Route53 latency-based routing o **Global Accelerator** (anycast, segundos de failover) |
   | **Configuración** | CloudFormation / Terraform en repo Git | IaC + Parameter Store replicado | IaC + Parameter Store + Secrets Manager replicados | IaC + replicación de secrets cross-region |
   | **Eventos / colas** | re-procesable desde origen | SNS cross-region o EventBridge global endpoint | EventBridge global endpoints + SQS dead-letter | replicación de eventos (MSK MirrorMaker 2 o Confluent multi-region) |

5. **Cruzar RPO/RTO declarado contra las estrategias** y descartar las que no cumplen:
   - RPO > 4 h y RTO > 4 h y presupuesto bajo → **Backup-Restore** suele bastar (típico P2).
   - RPO 5–15 min y RTO 15–60 min y presupuesto medio → **Pilot Light** (típico P1).
   - RPO < 5 min y RTO < 15 min y presupuesto alto → **Warm Standby** (típico P0 si tolera segundos de degradación).
   - RPO ~0 y RTO ~0 y presupuesto sin techo → **Multi-site Active-Active** (P0 crítico, banca, healthcare crítico).
6. **Estimar costo mensual aproximado**. Modelo simplificado: el costo base de tu stack en región primaria = `C`. Entonces:
   - Backup-Restore ≈ `1.05 C` (backups + retención).
   - Pilot Light ≈ `1.20–1.40 C` (núcleo prendido en DR).
   - Warm Standby ≈ `1.50–1.80 C` (~50 % del stack prendido en DR).
   - Multi-site ≈ `2.0–2.2 C` (~stack completo duplicado).
   Más egress entre regiones (puede ser significativo para streams sostenidos > 1 TB/mes).
7. **Recomendar la estrategia** que cumple RPO/RTO al menor costo. Si dos estrategias cumplen los SLOs, gana la más barata salvo que la criticidad lo prohíba.
8. **Producir matriz comparativa + recomendación + ADR esqueleto**.

## 5. Salida esperada

### 5.1 Matriz comparativa obligatoria (4 estrategias × dimensiones)

```markdown
| Estrategia | RPO real estimado | RTO real estimado | Costo mensual aprox | ¿Cumple RPO objetivo (<X>)? | ¿Cumple RTO objetivo (<Y>)? | Servicios AWS clave |
|------------|--------------------|--------------------|---------------------|------------------------------|------------------------------|---------------------|
| Backup-Restore | … | … | … | sí/no | sí/no | S3, AWS Backup, IaC |
| Pilot Light | … | … | … | sí/no | sí/no | RDS Cross-Region Read Replica, Route53 health checks |
| Warm Standby | … | … | … | sí/no | sí/no | RDS replica + ECS stack reducido + Route53 |
| Multi-site Active-Active | ~0 | ~0 | … | sí | sí | Aurora Global DB, DynamoDB Global Tables, Global Accelerator |
```

### 5.2 Recomendación final (3 bullets)

- Estrategia recomendada: **<Pilot Light / Warm Standby / etc.>**.
- Razón principal: cumple RPO **<X>** y RTO **<Y>** declarados y minimiza costo (costo mensual estimado **~<USD>/mes**).
- Trade-offs aceptados: <enumerar 2-3: ej. "30 s de tráfico degradado en failover, sin pérdida de datos transaccionales por Aurora Global DB con sub-second replicación">.

### 5.3 ADR esqueleto sugerido (`docs/adr/<n>-dr.md`)

Esquema estándar de ADR (Architecture Decision Record):

- **Estado**: Propuesta / Aceptada / Rechazada / Reemplazada por ADR-<n> (con fecha).
- **Contexto**: SLA, RPO, RTO objetivos, criticidad por dominio, presupuesto, región primaria, región secundaria candidata, restricciones regulatorias (residencia de datos), capacidad operativa del equipo (¿pueden ejecutar un drill anual de fallover?).
- **Decisión**: estrategia DR elegida + servicios AWS por capa (compute, BD, storage, DNS, eventos).
- **Opciones evaluadas**: las 4 estrategias (Backup-Restore, Pilot Light, Warm Standby, Multi-site), con motivo de descarte de cada no elegida.
- **Consecuencias positivas**: cumplimiento de SLA, resiliencia a falla regional, etc.
- **Consecuencias negativas**: costo adicional declarado, complejidad operativa, necesidad de drills periódicos, posible deuda en testing.
- **Plan de validación**: drill de fallover trimestral / anual; runbook documentado; métricas RPO/RTO medidas en cada drill.

## 6. Verificación (criterios de "bien hecho")

- La estrategia recomendada **cumple ambos** RPO y RTO objetivos (no solo uno).
- El **costo estimado** está dentro del presupuesto declarado.
- Cada **capa** del stack (compute, BD relacional, NoSQL, storage, DNS, eventos) tiene su servicio AWS de DR mapeado.
- La estrategia elegida tiene **runbook** explícito (qué se hace en fallover, quién lo hace, en qué orden).
- Hay un **plan de drill** (frecuencia + métricas a validar).
- **Multi-AZ está dado por defecto** como HA base; el ADR diferencia HA (multi-AZ) de DR (multi-región).
- Si la criticidad es P0 con RPO ~0 y RTO ~0 → la estrategia es **Multi-site Active-Active**; cualquier otra no cumple.
- Si el presupuesto es bajo y RPO/RTO permiten 4+ h → **Backup-Restore** es válido y se evita over-engineering.

## 7. Anti-patrones específicos

- **"Multi-region" como objetivo sin RPO/RTO declarados**: termina en sobrecosto sin necesidad. Mitigación: empezar siempre por RPO/RTO del negocio.
- **Multi-AZ confundido con DR**: multi-AZ es HA dentro de una región. Si la región AWS cae completa, multi-AZ no salva. Mitigación: declarar explícitamente "multi-AZ es HA, multi-región es DR".
- **Aurora Global Database con escrituras en ambas regiones sin entender consecuencias**: Aurora Global tiene **una sola región writer** (modo estándar). Multi-master multi-region requiere DynamoDB Global Tables o Aurora con write forwarding (con sus latencias). Mitigación: claridad sobre quién escribe dónde.
- **Réplica continua sin medir lag**: una read replica con 15 min de lag NO cumple RPO de 5 min. Mitigación: medir lag en producción y declarar el RPO real, no el objetivo.
- **Sin drill de fallover**: estrategia documentada que nunca se probó suele fallar el día real. Mitigación: drill trimestral mínimo; semestral aceptable; anual es lo mínimo defendible para auditoría.
- **Failover automático sin estrategia anti-flap**: Route53 con health checks agresivos puede causar flapping entre regiones. Mitigación: failover automático para datos (DNS) + decisión humana para compute crítico, o thresholds conservadores.
- **Egress cross-region no contabilizado**: replicar TBs/mes entre regiones es caro y no aparece en la estimación inicial. Mitigación: estimar egress explícitamente.
- **DR para servicios no críticos**: gastar Warm Standby en una herramienta interna usada 1 vez/semana. Mitigación: criticidad por servicio (P0/P1/P2) y estrategia distinta para cada nivel si aplica.

## 8. Mini ejemplo de invocación

> "SLA 99.95 %, RPO 5 min, RTO 30 min, presupuesto ~3000 USD/mes. Producto: marketplace B2B con 5 servicios (OrderService P0, CatalogService P1, ReportingService P2). Región primaria us-east-1, secundaria us-west-2. Datos: PostgreSQL transaccional, DynamoDB para sesiones, S3 para documentos. Usa el skill `dr-strategy-designer` y genera la matriz comparativa, recomendación y ADR esqueleto."

## 9. Modos de fallo conocidos

- Sin RPO/RTO declarados → STOP, pedirlos antes de seguir.
- RPO/RTO contradictorios con el presupuesto declarado (ej. RPO ~0 con $200/mes) → declarar el conflicto y pedir al usuario priorizar (relajar SLOs o subir presupuesto).
- Restricción regulatoria de residencia de datos (GDPR, DNI Bolivia, etc.) que prohíbe replicar fuera de país → declarar el conflicto, multi-región puede no ser viable; sugerir multi-AZ + DR manual + backups locales.
- "Queremos multi-cloud" → fuera del alcance de este skill (asume AWS); marcar como out-of-scope o redirigir a una decisión arquitectónica previa.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; 4 estrategias DR + RPO/RTO + matriz AWS por capa + ADR esqueleto |
