# PRD Ligero — FTGO (Food To Go)

**Versión:** 1.0  
**Fecha:** 2026-05-25  
**Estado:** Aprobado  
**Trazabilidad:** Brief §A.1–A.5, Richardson Cap 1–2

---

## 1. Contexto y Objetivos

FTGO es una plataforma de delivery de comida que conecta consumidores, restaurantes y couriers. Actualmente opera como un monolito Java (WAR) que exhibe los síntomas clásicos del "monolith hell" descritos en Richardson Cap 1: builds lentos, escalado conflictivo entre módulos, acoplamiento tecnológico y bloqueo del equipo de desarrollo. [Brief §A.1]

El objetivo del proyecto es **documentar y migrar la arquitectura objetivo hacia microservicios**, aplicando una estrategia incremental Strangler Fig durante 18–24 meses [Brief §A.4]. Este PRD establece las capacidades, stakeholders y NFRs que guiarán el FSD y los ADRs arquitectónicos. No se reemplaza el monolito de golpe; se extraen servicios progresivamente manteniendo ambos sistemas operativos en paralelo.

---

## 2. Stakeholders

| Rol | Descripción | Necesidad principal |
|---|---|---|
| **Consumidor** | Usuario final (móvil/web) que ordena comida | UX rápida, tracking en tiempo real, transparencia del estado del pedido |
| **Restaurante** | Negocio asociado que prepara la comida | Gestión de tickets, control de carga de cocina, dashboard de pedidos |
| **Courier** | Repartidor independiente que entrega pedidos | Asignaciones cercanas, rutas optimizadas, pago confiable |
| **Empleado FTGO (back office)** | Customer support, finanzas, operaciones | Visibilidad, reportes, resolución de incidentes |
| **Equipo de arquitectura** | Responsable del rediseño hacia microservicios | Calidad arquitectónica, trazabilidad, mantenibilidad |
| **Sistemas externos** | Stripe, Google Maps, SendGrid/Twilio | Integración estable, SLAs predecibles |

_Fuente: Brief §A.2. No se añaden stakeholders fuera de esta lista._

---

## 3. Capacidades de Negocio

Identificadas por Richardson Cap 2 como candidatos estables a microservicios. La descomposición exacta se decide en los ADRs.

### CAP-01: Consumer Management
Gestión de perfiles de consumidores: registro, autenticación, direcciones de entrega y preferencias. Es el núcleo de identidad del lado del comprador. Soporta GDPR/locales para datos personales [Brief §A.4 Cumplimiento].

### CAP-02: Restaurant Management
Onboarding de restaurantes, gestión de menús, horarios y disponibilidad. Permite a los restaurantes actualizar su oferta en tiempo real sin afectar al flujo de pedidos activos.

### CAP-03: Order Taking
Recepción y validación de pedidos: cálculo de totales, confirmación de disponibilidad del restaurante, creación del aggregate `Order` con fuerte consistencia interna [Brief §A.4 Consistencia de datos]. Es el núcleo transaccional del sistema.

### CAP-04: Order Fulfillment / Kitchen
Generación y gestión de tickets de cocina. Desacopla la lógica del restaurante (preparación) del flujo de toma de pedidos. Recibe tickets y reporta estado de preparación.

### CAP-05: Delivery
Asignación de couriers, optimización de rutas, tracking en tiempo real. Alta variabilidad de carga durante picos [Brief §A.4 Carga]. El tracking puede degradar a 99.5% de disponibilidad [Brief §A.4 Disponibilidad].

### CAP-06: Billing & Accounting
Cobros al consumidor (integrado con Stripe), cálculo de comisiones y payouts a restaurantes y couriers. Delegación de PCI-DSS a Stripe [Brief §A.4 Cumplimiento]. Requiere consistencia eventual para reportes [Brief §A.4 Consistencia].

### CAP-07: Notifications
Envío de emails, SMS y push notifications (confirmaciones, alertas de estado, recibos). Altamente desacoplable mediante eventos asincrónicos. Integra SendGrid y Twilio [Brief §A.2 Sistemas externos].

---

## 4. Requisitos No Funcionales

### NFR-01: Escalabilidad en Pico
- **Métrica:** El sistema debe soportar tráfico 5× la línea base durante 12:00–14:00 y 19:00–22:00 hora local sin degradación de latencia.
- **Origen:** [Brief §A.4 Carga]
- **Justificación:** Horarios de almuerzo y cena concentran la demanda; cada componente debe escalar horizontalmente de forma independiente (X-axis y Y-axis del Scale Cube).

### NFR-02: Latencia UX
- **Métrica:** Tiempo de respuesta p95 < 200 ms para acciones del consumidor en la app (creación de pedido, visualización de menú, confirmación).
- **Origen:** [Brief §A.4 Latencia UX]
- **Justificación:** Experiencia móvil en horarios pico; la latencia percibida es factor crítico de conversión.

### NFR-03: Disponibilidad del Flujo de Pedidos
- **Métrica:** 99.9% mensual para el flujo Order Taking (CAP-03). El tracking en tiempo real puede degradar a 99.5% mensual.
- **Origen:** [Brief §A.4 Disponibilidad]
- **Justificación:** La toma de pedidos es el flujo de ingresos principal; el tracking es importante pero tolera degradación temporal.

### NFR-04: Tolerancia a Fallos Externos
- **Métrica:** El sistema debe poder tomar y confirmar pedidos aunque Stripe esté caído (cola de retry con reintentos exponenciales). Degradación de mapas aceptada con fallback a última ruta conocida.
- **Origen:** [Brief §A.4 Tolerancia a fallos externos]
- **Justificación:** La dependencia de terceros no debe bloquear el flujo principal de ingresos.

### NFR-05: Consistencia de Datos
- **Métrica:** Consistencia fuerte dentro del aggregate `Order` (una única transacción de BD); consistencia eventual aceptada entre servicios para reporting con SLA de propagación < 5 s p95.
- **Origen:** [Brief §A.4 Consistencia de datos]
- **Justificación:** El aggregate de pedido requiere atomicidad (Richardson Cap 3); los reportes pueden tolerar lag.

### NFR-06: Trazabilidad End-to-End
- **Métrica:** 100% de las acciones del consumidor deben incluir correlation ID en los logs; tracing distribuido con latencia de instrumentación < 5 ms overhead.
- **Origen:** [Brief §A.4 Trazabilidad]
- **Justificación:** Diagnóstico de incidentes y auditoría requieren trazabilidad completa entre servicios.

### NFR-07: Migración Incremental
- **Métrica:** El monolito legacy y los microservicios extraídos deben coexistir sin downtime. Cada extracción de servicio validada con feature flag antes de cut-over completo.
- **Origen:** [Brief §A.4 Migración incremental]
- **Justificación:** El negocio no puede permitirse una migración big-bang; Strangler Fig garantiza continuidad operativa.

---

## 5. Alcance

### Dentro del alcance (MVP de la arquitectura objetivo)
- Documentación de la arquitectura objetivo para los 7 dominios de negocio (CAP-01 a CAP-07).
- Extracción incremental de microservicios via Strangler Fig durante 18–24 meses.
- Integración con Stripe (pagos), Google Maps (rutas), SendGrid/Twilio (notificaciones).
- APIs REST/JSON síncronas para acciones de consumidor + mensajería asíncrona (Kafka) para eventos internos.
- Stack principal Java 17 / Spring Boot en servicios core; libertad tecnológica en servicios satélite [Brief §A.4 Tecnología].

### Fuera del alcance (este PRD)
- Implementación detallada del código fuente (el PRD documenta la arquitectura objetivo, no el código).
- Reemplazo completo del monolito legacy (ocurre gradualmente; el monolito sigue vivo durante la migración).
- Funcionalidades no presentes en el brief: programa de fidelización, motores de recomendación, internacionalización multi-moneda.
- Infraestructura cloud específica y configuración de Kubernetes (se trata en ADRs posteriores de infraestructura).

---

_Documento trazable a: Brief §A.1–A.5 | Richardson, Microservices Patterns, Manning 2019, Cap 1–2_
