# Market Requirements Document (MRD) — EduSync

## 0. Metadatos

| Campo | Valor |
|-------|-------|
| **Producto** | EduSync |
| **Grupo** | G-EduSync |
| **Versión** | v1.0 |
| **Fecha** | 14/05/2026 |
| **Product Manager / Autor** | Rodrigo Aspeti — Dev Lead / PM EduSync |
| **Revisores** | Docente + Equipo G-EduSync + Stakeholders institucionales |
| **Estado** | En revisión |
| **Relación con BRD** | `BRD_EduSync_v2.md` (v2.0) |
| **Prompts utilizados** | `PR-BRD-001`, `PR-BRD-002`, `PR-ARCH-001` (ver `docs/PROMPT_MAPPING.md`) |

---

## 1. Resumen ejecutivo

EduSync es una plataforma SaaS B2B multitenant de gestión académica diseñada exclusivamente para el mercado educativo boliviano. Ataca un problema estructural y crónico: la "triple digitación manual" que obliga a secretarías y docentes a trabajar de madrugada para cumplir con los plazos del Sistema de Información Educativa (SIE) del Ministerio de Educación, bajo riesgo de sanciones económicas equivalentes a hasta cinco días de sueldo por errores de datos.

El mercado objetivo son las unidades educativas privadas y de convenio de Bolivia, estimadas en más de 4 000 instituciones con necesidades de digitalización activas, de las cuales aproximadamente 800 en zonas urbanas representan el segmento inmediatamente servible. Ningún competidor actual resuelve el cuello de botella crítico: la sincronización masiva con el SIE mediante el código RUDE del estudiante sin digitación manual. Academium y Colegio360 trasladan el estrés operativo al usuario; EduSync lo elimina.

La diferenciación central de EduSync es triple: (1) motor algorítmico de consolidación con criterio `floor` que elimina descuadres de decimales observados en los registros Excel reales; (2) exportación one-click al SIE con resiliencia ante fallos parciales y mapeo exclusivo por RUDE; (3) inmutabilidad post-cierre con log de auditoría inalterable que convierte a EduSync en la fuente única de verdad académica ante inspecciones ministeriales.

La oportunidad de mercado estimada es un SOM de USD 480 000 anuales en el año 3, escalable a un SAM de USD 2,4 M conforme se expande la cobertura urbana. El modelo SaaS B2B por institución garantiza ingresos recurrentes con bajo costo de retención.

---

## 2. Visión del producto

> "Para directores, secretarías y docentes de colegios bolivianos que hoy trabajan de madrugada con Excel para reportar al Ministerio, EduSync es la plataforma SaaS que automatiza el cierre académico trimestral en menos de 10 minutos, con cero digitación manual y cero riesgo de multas ministeriales, disponible desde el primer trimestre de 2027."

---

## 3. Análisis de mercado

### 3.1 Tamaño de mercado

| Métrica | Valor | Fuente / Nota |
|---------|-------|---------------|
| **TAM** (*Total Addressable Market*) | ~18 000 unidades educativas registradas en Bolivia · USD 10,8 M anuales a precio de lista | Ministerio de Educación Bolivia — Anuario Estadístico 2025 *(assumption: extrapolado de datos públicos)* |
| **SAM** (*Serviceable Addressable Market*) | ~4 000 unidades educativas privadas y de convenio con capacidad de pago · USD 2,4 M anuales | Segmento con presupuesto administrativo propio y obligación directa de reporte SIE |
| **SOM** (*Serviceable Obtainable Market*) | ~800 instituciones urbanas (Cochabamba, La Paz, Santa Cruz, Oruro) en años 1–3 · USD 480 000 anuales | Estimación conservadora de penetración del 20 % del SAM en 36 meses con equipo de 1 dev + ventas directas |

> *Nota de asunción:* Los valores monetarios se calculan sobre un ticket promedio anual de USD 600 por institución (Setup Fee + suscripción por ~200 estudiantes activos). Requieren validación con piloto en 3 instituciones antes del lanzamiento comercial.

### 3.2 Tendencias del sector

- **Digitalización educativa acelerada post-COVID:** Bolivia registra un crecimiento acelerado en adopción de herramientas digitales en el sector educativo privado, impulsado por la experiencia de educación remota 2020–2022. Los colegios que adoptaron herramientas digitales durante la pandemia tienen mayor disposición a pagar por soluciones de gestión.
- **Presión regulatoria creciente del Ministerio de Educación:** Los plazos de reporte al SIE se han mantenido estrictos con multas vigentes. La tendencia de auditorías ministeriales más frecuentes incrementa el riesgo para instituciones que dependen de procesos manuales.
- **Escasez de personal administrativo calificado:** Las secretarías de colegios privados bolivianos tienen alta rotación. Los procesos que dependen de conocimiento tácito de una persona (cómo consolidar el Excel correctamente) son un riesgo institucional latente.
- **Auge del SaaS B2B en EdTech latinoamericano:** El segmento de gestión escolar en América Latina creció a tasas del 18–22 % anual en 2023–2025 (referencia: HolonIQ Latin America EdTech Report 2025 *(assumption)*). Bolivia permanece subatendido comparado con Perú, Argentina o Colombia.
- **Demanda de trazabilidad y compliance:** La digitalización de registros académicos con inmutabilidad y log de auditoría se convierte en una necesidad legal, no solo operativa, ante inspecciones del Ministerio.

### 3.3 Factores regulatorios y de cumplimiento

- **Ley 070 "Avelino Siñani - Elizardo Pérez"** (Ley de la Educación Bolivia): establece la estructura del año escolar en tres trimestres y las dimensiones de evaluación (Ser, Saber, Hacer, Decidir) aplicables a educación secundaria comunitaria productiva.
- **Sistema de Información Educativa (SIE):** plataforma obligatoria del Ministerio de Educación Bolivia. El reporte de calificaciones por RUDE (Registro Único de Estudiante) en formato y plazos específicos es una obligación legal con sanciones económicas explícitas.
- **Código RUDE:** identificador único del estudiante emitido por el Estado boliviano. Su uso en EduSync garantiza el cumplimiento de la trazabilidad estudiantil exigida por el Ministerio.
- **Normativa de privacidad de datos:** Bolivia no cuenta aún con una Ley General de Protección de Datos Personales equivalente a GDPR; sin embargo, la confidencialidad de registros académicos de menores está protegida por el Código del Niño, Niña y Adolescente (Ley 548). EduSync aplica aislamiento multitenant (RLS) y no expone datos entre instituciones.
- **Restricción de horario SIE:** los servidores del Ministerio colapsan en horario laboral; el acceso en jornadas nocturnas es una práctica generalizada, no opcional. EduSync mitiga esto con reintentos asíncronos e idempotentes.

### 3.4 Cadencia de Continuous Discovery

| Aspecto | Valor |
|---------|-------|
| **Cadencia de entrevistas** | Quincenal durante la fase de piloto; mensual en estado operativo |
| **Usuarios contactados por ciclo** | Mínimo 1 entrevista por ciclo (Torres: ≥ 1/semana); actualmente 3 validadas (Marcela, Wendy, Jeanneth) |
| **Formato de hipótesis** | *Cuando `<situación>`, espero `<resultado>`, porque `<razón>`* — ver §12 |
| **Backlog de hipótesis** | Ver §12 de este documento (H1–H8) |
| **Output del track** | Validaciones que actualizan §3 (mercado), §5 (JTBD), §11 (requerimientos) y §12 (hipótesis) |

> **Riesgo Discovery activo:** El equipo cuenta con 3 entrevistas validadas (pre-piloto). Durante la fase de construcción, si transcurre más de 1 mes sin contacto con usuarios reales, se declara riesgo en §13 (MR-06).

---

## 4. Segmentación y personas

### 4.1 Segmentos de clientes

| Segmento | Tamaño estimado | Necesidad principal | Disposición a pagar | Origen M2 |
|----------|-----------------|---------------------|---------------------|-----------|
| **Unidades educativas privadas urbanas** | ~1 800 instituciones (Cochabamba, La Paz, Santa Cruz, Oruro) | Eliminar el trabajo de madrugada en cierres trimestrales y sincronización SIE | Alta — el riesgo de multa ministerial justifica el gasto | Segmento validado con Colegio Abaroa (piloto) |
| **Unidades educativas de convenio** | ~2 200 instituciones a nivel nacional | Cumplimiento normativo SIE sin capacidad de personal dedicado a IT | Media — presupuesto más restringido, mayor sensibilidad al precio | Hipótesis H7 — pendiente de validación |
| **Directores / compradores B2B (decision makers)** | 1 por institución (~4 000 tomadores de decisión) | Visibilidad institucional en tiempo real + control jerárquico sin herramientas manuales | Alta si se demuestra ROI en primer trimestre | Jeanneth (Directora) — entrevista validada |
| **Secretarías / usuarios operativos** | 1–3 por institución | Eliminación de trabajo nocturno y reingresos manuales | N/A (comprador ≠ usuario) | Wendy (Secretaría) — entrevista validada |

### 4.2 Personas

> **Trazabilidad con M2 (UI/UX):** Las 3 personas provienen de las entrevistas de campo documentadas en `01_vision_negocio.md` y `BRD_EduSync_v2.md §4`. El MRD las complementa con la mirada de mercado: tamaño del segmento, disposición a pagar y posición competitiva.

---

#### Persona 1 — Wendy (Decisora operativa + Compradora de facto)

- **Origen M2:** `docs/BRD_EduSync_v2.md §4.2` — validada mediante entrevista directa.
- **Rol:** Secretaría académica / Asesora administrativa.
- **Demografía:** 28–42 años, educación universitaria en administración o educación, empleada de planta en colegio privado urbano. Trabaja en Cochabamba o La Paz.
- **Objetivos:** Cerrar el trimestre sin errores antes de las 11 PM. Obtener confirmación de recepción exitosa del SIE sin reingresar datos. Tener visibilidad inmediata de qué docentes faltan.
- **Dolores actuales:** Trabaja entre las 2:00 AM y las 4:00 AM los días de cierre. Si el SIE falla a mitad del proceso, debe reiniciar desde cero. Dedica 10 ciclos de revisión manual por trimestre auditando los Excel de cada docente. Carga con el estrés de los errores de otros.
- **Comportamiento digital:** Usa Excel avanzado a diario. Maneja el portal web del SIE del Ministerio. Prefiere soluciones web sin instalación. Acceso desde computadora de escritorio o laptop del colegio.
- **Disposición a pagar:** No compra directamente, pero tiene influencia decisiva en la recomendación al Director. Su adopción determina el éxito del producto.
- **Frase representativa:** *"Antes pasaba el fin de semana auditando filas de Excel. Ahora quiero que el sistema me diga quién falta y yo solo apriete un botón."*

---

#### Persona 2 — Marcela (Usuario primario / Docente)

- **Origen M2:** `docs/BRD_EduSync_v2.md §4.1` — validada mediante entrevista directa.
- **Rol:** Docente de aula / Asesora de curso en educación secundaria.
- **Demografía:** 25–50 años, licenciatura en educación o especialidad, titular o contratada. Enseña múltiples materias en un mismo colegio.
- **Objetivos:** Registrar sus calificaciones solo para sus materias sin depender de que nadie más "procese" sus datos. Saber en tiempo real si le falta algo antes del cierre. Poder corregir un error sin sentir que está "hackeando" el sistema.
- **Dolores actuales:** Llena un Excel por materia que luego "alguien más" consolida, sin feedback de si los datos llegaron bien. Vive con miedo de que la secretaría encuentre un error que genere un descuento salarial. No puede corregir un error post-cierre sin exponer la irregularidad.
- **Comportamiento digital:** Usuario casual de tecnología. Accede desde teléfono personal (Android) o computadora del aula. No tolera interfaces de más de 3 clics para completar una tarea.
- **Disposición a pagar:** No paga directamente. Su adopción sin fricción es condición necesaria para que el producto funcione (si no carga notas, el sistema no genera valor).
- **Frase representativa:** *"Yo solo quiero poner mis notas y que me digan 'todo OK, ya terminaste'. Nada más."*

---

#### Persona 3 — Jeanneth (Compradora decisora / Directora institucional)

- **Origen M2:** `docs/BRD_EduSync_v2.md §4.3` — validada mediante entrevista directa.
- **Rol:** Directora de unidad educativa privada.
- **Demografía:** 35–55 años, formación en gestión educativa o pedagogía. Toma decisiones de inversión tecnológica del colegio. Responde ante los propietarios por los resultados académicos y el cumplimiento regulatorio.
- **Objetivos:** Saber en tiempo real qué docentes han entregado notas y cuáles faltan. Tener indicadores de rendimiento institucional listos para cualquier auditoría ministerial. Aprobar correcciones retroactivas con control y trazabilidad legal.
- **Dolores actuales:** Solo conoce el estado del colegio cuando la secretaría le reporta en papel o por WhatsApp. Sin visibilidad de datos, no puede anticipar problemas antes del plazo ministerial. Si hay una corrección clandestina post-cierre, no se entera.
- **Comportamiento digital:** Accede desde computadora de escritorio o tablet. Usa WhatsApp, email y Excel de forma habitual. Valora dashboards simples con semáforos visuales (verde/amarillo/rojo).
- **Disposición a pagar:** Comprador principal. Aprueba el gasto si percibe ROI claro: menos riesgo de multas ministeriales + visibilidad + ahorro en horas nocturnas del personal.
- **Frase representativa:** *"Quiero ver de un vistazo si estamos al día, sin que nadie me tenga que llamar a las 3 de la madrugada."*

---

## 5. Jobs-to-be-Done

| JTBD ID | Cuando… | Quiero… | Para poder… |
|---------|---------|---------|-------------|
| JTBD-01 | llega el plazo de cierre trimestral | exportar todas las calificaciones al SIE con un clic | no trabajar de madrugada ni reingresar datos manualmente |
| JTBD-02 | ingreso las notas de mis estudiantes | que el sistema valide automáticamente que están dentro del rango permitido | no recibir un rechazo del SIE días después por un dato inválido |
| JTBD-03 | un alumno entra o se retira del colegio | que las notas de los demás no se desplacen | no corromper el historial académico de toda la clase |
| JTBD-04 | detecto un error en una nota ya cerrada | solicitar una corrección con autorización del director y que quede un registro | no tener que modificar datos clandestinamente y arriesgar una sanción |
| JTBD-05 | soy directora y quiero saber el avance | ver en tiempo real qué docentes han cerrado sus materias | anticipar problemas antes del plazo ministerial sin esperar un reporte en papel |
| JTBD-06 | cierro el trimestre | generar boletines oficiales en PDF directamente del sistema | no tener que transcribir nada a mano antes de entregárselos a los padres |
| JTBD-07 | hay un fallo del servidor SIE a mitad de la exportación | que el sistema retome desde donde quedó sin reiniciar todo | no perder el trabajo hecho ni arriesgar duplicados en el SIE |
| JTBD-08 | abro el año académico | definir los parámetros de dimensiones y pesos una sola vez | no tener que recordar ni recalcular las reglas de notas en cada trimestre |

---

## 6. Análisis competitivo

### 6.1 Tabla comparativa

| Criterio | **EduSync** | **Excel + SIE Manual** | **Academium** | **Colegio360** | **Google Sheets + Forms** |
|----------|-------------|------------------------|---------------|----------------|---------------------------|
| **Precio** | Setup 200 Bs + suscripción anual por estudiante | Costo cero (monetario) / Altísimo costo humano | Suscripción mensual por institución (~150–300 USD/año) *(assumption)* | Suscripción anual (>500 USD/año) *(assumption)* | Gratuito |
| **Integración directa con SIE Bolivia** | Sí — exportación masiva one-click por RUDE | No — digitación manual nota por nota | Parcial — exporta archivos; no integración directa | No documentada | No |
| **Mapeo por RUDE** | Sí — único identificador en todo el sistema | No — por posición visual en lista | No confirmado *(assumption)* | No confirmado *(assumption)* | No |
| **Consolidación automática con `floor`** | Sí — motor algorítmico centralizado | No — errores frecuentes de decimales | No documentado | No documentado | No |
| **Inmutabilidad post-cierre + audit log** | Sí — append-only, log inalterable | No — cualquier usuario puede modificar | No confirmado | No confirmado | No |
| **Ventana temporal de corrección retroactiva** | Sí — autorización del director con 1–72h | No — modificación libre sin trazabilidad | No documentado | No documentado | No |
| **Dashboard en tiempo real para director** | Sí — vista de avance docente por curso | No — depende de reporte manual | Básico | Básico | No |
| **Cobertura geográfica** | Bolivia (mercado local, hiper-localizado) | Bolivia | Latinoamérica (sin hiper-localización boliviana) | Latinoamérica | Global |
| **Curva de aprendizaje (Zero-Training)** | Diseñada para cero capacitación | Alta — requiere conocimiento tácito | Media | Alta | Media |
| **Resiliencia ante fallos SIE** | Sí — reintentos idempotentes por RUDE+periodo | No — reinicio total al fallar | No documentado | No documentado | No |
| **Uptime / SLA** | ≥ 99,5 % en ventanas de cierre | N/A | No publicado | No publicado | 99,9 % (Google) |
| **Multitenant con aislamiento de datos** | Sí — RLS PostgreSQL por tenant_id | No — datos en archivos locales | Limitado | Limitado | No |

### 6.2 Positioning statement

> Para **secretarías y directores de colegios privados y de convenio bolivianos**, que hoy **trabajan de madrugada con Excel y corren riesgo de multas ministeriales** por errores de consolidación, **EduSync** es **la plataforma SaaS de gestión académica hiper-localizada** que **cierra el trimestre en menos de 10 minutos con cero digitación manual y trazabilidad legal**, a diferencia de **Academium y Colegio360** que **trasladan el estrés operativo al usuario sin resolver el cuello de botella del SIE boliviano**.

### 6.3 Ventaja competitiva sostenible

- **Hiper-localización boliviana:** EduSync es el único producto construido desde el problema específico del SIE boliviano, el código RUDE y la Ley 070. Esta localización profunda es costosa de replicar para competidores regionales sin inversión en Discovery en Bolivia.
- **Datos propios del mercado:** Los archivos Excel reales de al menos 2 instituciones pilotos (Colegio Abaroa y una segunda unidad) son el insumo de calibración del motor algorítmico, que elimina los descuadres de decimales documentados. Esta evidencia diferencia la propuesta de cualquier competidor que generalice reglas de cálculo.
- **Efecto de red operativo:** Cuantos más colegios usen EduSync, más estable es el benchmark de rendimiento institucional anónimo entre pares — una funcionalidad de valor incremental sin costo marginal adicional.
- **Barreras de salida (switching cost):** El historial académico inmutable en EduSync, el log de auditoría y el formato de exportación SIE calibrado crean dependencia operativa y legal que aumenta con el tiempo de uso.

---

## 7. Propuesta de valor

### 7.1 Value Proposition Canvas resumido

| Dimensión | Contenido |
|-----------|-----------|
| **Gains (ganancias esperadas)** | Cierre trimestral en < 10 min · Cero digitación en SIE · Visibilidad en tiempo real para el director · Boletines PDF generados automáticamente · Auditoría ministerial lista sin preparación adicional |
| **Pains (dolores actuales)** | Jornadas de 2:00–4:00 AM en cierres trimestrales · Multas de hasta 5 días de sueldo por errores de datos · Desfase de listas cuando entra/sale un alumno · 10 ciclos de revisión manual por trimestre · Correcciones clandestinas sin trazabilidad |
| **Gain Relievers** | Motor de consolidación automático con `floor` · Dashboard de avance docente en tiempo real · Exportación masiva SIE one-click · Log de auditoría inalterable para cualquier inspección |
| **Pain Relievers** | Reintentos asíncronos ante fallos SIE (no más reinicio total) · Identificación exclusiva por RUDE (no más desfase de listas) · Bloqueo post-cierre con autorización jerárquica para correcciones · Interfaz Zero-Training (< 3 clics para completar tarea crítica) |
| **Products & Services** | Plataforma SaaS multitenant · Motor de consolidación algorítmico · Módulo de exportación SIE por RUDE · Dashboard institucional en tiempo real · Boletines PDF parametrizables · Log de auditoría append-only |

---

## 8. Pricing y modelo de negocio

### 8.1 Estructura de precios

| Componente | Descripción | Precio referencial |
|------------|-------------|-------------------|
| **Setup Fee** | Configuración inicial del tenant: carga de nómina, dimensiones, parámetros del Ministerio, capacitación Zero-Training (< 2 horas). | Bs 200 / institución (pago único) |
| **Suscripción anual — Tier Básico** | Hasta 300 estudiantes activos. Incluye los 3 módulos core (notas, SIE, boletines). Pago único o en 3 cuotas trimestrales. | Bs 1 800 / año (~USD 260) |
| **Suscripción anual — Tier Estándar** | Hasta 600 estudiantes activos. Incluye Básico + asistencia avanzada + reportería estadística. | Bs 3 000 / año (~USD 435) |
| **Suscripción anual — Tier Premium** | Estudiantes ilimitados. Incluye Estándar + API de integración + SLA garantizado 99,5 % + soporte prioritario en cierres trimestrales. | Bs 4 800 / año (~USD 695) |
| **Módulos adicionales (upselling)** | Comunicación con padres · Finanzas básicas · Módulo de matrícula digital | A cotizar por módulo |

### 8.2 Modelo y elasticidad

- **Modelo:** SaaS B2B con contrato anual renovable. La unidad de cobro es la institución (no el usuario individual), lo que simplifica la venta y la facturación.
- **Lógica de precio:** El costo anual del Tier Básico equivale a menos de 3 horas de trabajo nocturno de la secretaría valoradas a sueldo promedio mensual de Bs 2 500. El ROI se justifica solo con el ahorro de la primera semana de cierre trimestral.
- **Benchmarks:**
  - Academium: ~USD 150–300/año (sin integración SIE directa).
  - Colegio360: ~USD 500–800/año (mercado latinoamericano, sin hiper-localización boliviana).
  - EduSync Tier Básico: ~USD 260/año con resolución del cuello de botella SIE que los competidores no abordan.
- **Descuento por pago anticipado:** 10 % sobre el precio anual si se paga en una sola cuota antes del inicio del año escolar.

---

## 9. Go-to-Market

### 9.1 Canales de adquisición

- **Canal directo B2B:** Visitas presenciales a directores de colegios privados en Cochabamba (mercado de origen). Demo en vivo del cierre trimestral simulado es el argumento de venta principal. Objetivo: 20 instituciones en el primer año escolar.
- **Referidos entre secretarías:** Las secretarías forman redes informales de comunicación donde se comparten "trucos" para el SIE. Una secretaría satisfecha es el canal más efectivo hacia 3–5 colegios del mismo círculo. Programa de referidos: 1 mes gratis para el referidor + descuento para el nuevo cliente.
- **Presencia digital (orgánico):** Contenido en TikTok y YouTube dirigido a secretarías de colegios ("Cómo cerrar el trimestre sin trabajar de madrugada"). El formato de contenido educativo breve tiene alta penetración en el segmento.
- **Congresos y ferias educativas:** Exposición en eventos de la Federación de Administradores de Colegios de Bolivia y ferias de inicio de año escolar.
- **Alianza con Ministerio / SIE:** Relación estratégica para estar en la lista de herramientas recomendadas o compatibles con el SIE. Horizonte: año 2.

### 9.2 Estrategia de lanzamiento

- **Pre-launch (Q4 2026 — Dic 2026):**
  - Piloto cerrado con 3 instituciones (Colegio Abaroa + 2 adicionales de Cochabamba).
  - Validación del motor de consolidación `floor` con datos reales del año escolar 2026.
  - Ajuste de UX basado en feedback de Wendy y Marcela (secretaría y docente piloto).
  - Objetivo: 0 errores en exportación SIE en el cierre del 3er trimestre 2026.

- **Launch (Q1 2027 — Marzo 2027):**
  - Apertura comercial a nuevos clientes, con las 3 instituciones piloto como casos de éxito.
  - Demo pública en feria de inicio de año escolar.
  - Campaña de contenido: "Tu secretaria ya no trabaja de madrugada" (TikTok / Instagram).
  - Meta de ventas: 20 instituciones activas al cierre del 1er trimestre 2027.

- **Post-launch (Q2–Q4 2027):**
  - Activación de módulo de reportería estadística y indicadores institucionales.
  - Lanzamiento del programa de referidos con incentivos.
  - Inicio de upselling: módulo de comunicación con padres.
  - Expansión geográfica a La Paz y Santa Cruz.
  - Meta: 80 instituciones activas al cierre del año escolar 2027.

### 9.3 Funnel AARRR inicial

| Etapa | Métrica | Meta (Año 1) |
|-------|---------|--------------|
| **Acquisition** | Instituciones que solicitan demo | 60 demos realizadas |
| **Activation** | Instituciones que completan el Setup Fee y configuran su primer periodo | 25 instituciones onboarded |
| **Retention** | Renovación de suscripción anual | ≥ 85 % de renovación |
| **Revenue** | ARPU anual por institución | Bs 2 200 (USD ~320) promedio entre tiers |
| **Referral** | k-factor (nuevos clientes por referido) | ≥ 0,3 (1 referido nuevo por cada 3 clientes activos) |

---

## 10. Métricas de éxito del producto

- **North Star Metric:** Tiempo del ciclo de cierre operativo trimestral (minutos). Meta: < 10 minutos vs. línea base > 15 horas. Medido en telemetría de sesión desde el inicio de la exportación hasta la confirmación del SIE.

| KPI | Definición | Línea base | Meta | Horizonte |
|-----|-----------|------------|------|-----------|
| **NSM — Ciclo de cierre** | Tiempo total de la sesión de sincronización SIE (minutos) | > 900 min (15+ horas) | < 10 min | 1er cierre trimestral post-lanzamiento |
| **KPI-02 — Tasa de error SIE** | % de registros rechazados o duplicados en exportación al SIE | Alta (desfases y decimales) | 0 % | 1er cierre trimestral post-lanzamiento |
| **KPI-03 — Ciclos de revisión manual** | Número de veces que la secretaría debe corregir y volver a exportar | 10 ciclos/trimestre | 0 ciclos | Año escolar 1 |
| **KPI-04 — Adopción docente** | % de docentes que cierran su materia antes del plazo sin recordatorio manual | Sin medición | ≥ 95 % | Año escolar 1 |
| **KPI-05 — Retención de clientes** | % de instituciones que renuevan la suscripción anual | N/A (primer año) | ≥ 85 % | Año 2 |
| **KPI-06 — NPS institucional** | Net Promoter Score de directores y secretarías | N/A (primer año) | ≥ 50 | 6 meses post-lanzamiento |

---

## 11. Requerimientos de mercado (alto nivel)

> Los MRD-N-* son requerimientos **de mercado** (qué pide el segmento) independientes de la implementación técnica. Se trazan a BRD y PRD en §14.

| ID | Requerimiento de mercado | Prioridad | Justificación de mercado |
|----|--------------------------|-----------|--------------------------|
| MRD-N-01 | El producto debe soportar la integración directa con el SIE del Ministerio de Educación Bolivia, exportando masivamente por código RUDE sin digitación manual | Must | Es el dolor primario documentado en 3 de 3 entrevistas. Sin esto, EduSync no diferencia de Excel. |
| MRD-N-02 | El producto debe eliminar los errores de desfase de listas cuando hay altas, bajas o transferencias de estudiantes | Must | Causa raíz del 100 % de los errores de exportación SIE identificados en los Excel reales analizados. |
| MRD-N-03 | El producto debe ser operable sin capacitación formal (Zero-Training) para docentes con baja alfabetización digital | Must | La alta rotación y diversidad de perfiles docentes en colegios privados bolivianos impide costosas capacitaciones. |
| MRD-N-04 | El producto debe proveer al director visibilidad en tiempo real del avance de la carga de notas por curso y materia | Must | Wendy y Jeanneth coinciden: la ceguera de datos es el segundo dolor más grande después del trabajo nocturno. |
| MRD-N-05 | El producto debe garantizar la inmutabilidad de los registros post-cierre con log de auditoría, cumpliendo con la exigencia de trazabilidad ante inspecciones ministeriales | Must | Requisito legal implícito en la normativa SIE. Sin trazabilidad, el producto no es apto para auditorías. |
| MRD-N-06 | El producto debe generar boletines académicos oficiales en PDF sin digitación adicional, usando la plantilla ministerial vigente | Should | Elimina el último proceso manual post-consolidación y cierra el ciclo completo del trimestre en el sistema. |
| MRD-N-07 | El producto debe soportar al menos 300 estudiantes activos por institución en el tier base, escalable a instituciones de mayor tamaño | Should | El 80 % del SAM son instituciones con menos de 300 estudiantes; el 20 % restante requiere tiers superiores. |
| MRD-N-08 | El producto debe funcionar correctamente bajo conectividad de internet variable (no garantizada en zonas periurbanas bolivianas) | Should | Cochabamba y La Paz tienen zonas con conectividad intermitente. Una pérdida de conexión no debe perder datos en progreso. |
| MRD-N-09 | El producto debe integrarse con el formato de exportación SIE ante cambios ministeriales sin generar un nuevo desarrollo desde cero | Could | El Ministerio modifica el formato SIE sin previo aviso. La parametrización sin redespliegue es diferenciador de largo plazo. |
| MRD-N-10 | El producto debe soportar múltiples instituciones en una misma plataforma (multitenant) con aislamiento total de datos entre ellas | Must | Condición de arquitectura para escalar sin costos de infraestructura lineales. Requerimiento implícito de privacidad regulatoria. |

---

## 12. Supuestos e hipótesis a validar

| ID | Hipótesis | Cómo validar | Criterio de éxito |
|----|-----------|--------------|-------------------|
| H1 | El 80 % de los docentes puede registrar sus notas sin capacitación formal usando EduSync | Prueba de usabilidad con 5 docentes reales en sesión de 30 min (sin asistencia) | ≥ 4/5 docentes completan el flujo sin preguntar al facilitador |
| H2 | El director firma el contrato si ve la demo del cierre trimestral en menos de 10 minutos | Seguimiento de demos: tasa de conversión demo → contrato | ≥ 40 % de conversión demo → contrato firmado |
| H3 | El desfase de listas es la causa raíz del 100 % de los errores SIE reportados (ya validada) | Análisis de los Excel reales de 2 instituciones piloto (realizado: Colegio Abaroa + REGISTRO SECUNDARIA 2026) | **Validada** — confirmada con evidencia directa en los archivos |
| H4 | La secretaría puede exportar al SIE sin asistencia técnica usando EduSync | Prueba piloto en cierre real del 3er trimestre 2026 con Wendy (Colegio Abaroa) | 0 errores en la exportación + tiempo < 10 minutos |
| H5 | El precio de Bs 1 800/año (Tier Básico) es aceptable para el 70 % del SAM | Encuesta de disposición a pagar con 20 directores de colegios privados | ≥ 70 % responde que lo pagaría sin comparar con alternativas |
| H6 | La resiliencia ante fallos SIE (reintentos idempotentes) es el segundo diferenciador más valorado después de la consolidación automática | Encuesta de priorización de features con 15 secretarías | ≥ 60 % ubica "no reiniciar ante fallo SIE" en top 3 features |
| H7 | Las unidades educativas de convenio tienen disposición a pagar similar a las privadas si el ROI de ahorro de horas se demuestra | Piloto con 1 institución de convenio en el primer año | Conversión a contrato anual sin descuento adicional |
| H8 | El k-factor del programa de referidos entre secretarías es ≥ 0,3 | Medición de referidos activos durante el primer año escolar | ≥ 6 nuevos clientes provenientes de referidos en el año 1 |

---

## 13. Riesgos de mercado

| ID | Riesgo | Prob. | Impacto | Mitigación |
|----|--------|-------|---------|------------|
| MR-01 | El Ministerio de Educación modifica el formato de exportación SIE de forma incompatible con el motor actual | Media | Crítico | Arquitectura paramétrica: el formato SIE se almacena en base de datos sin redespliegue (DA-02). Monitoreo activo de comunicados ministeriales. |
| MR-02 | Academium o Colegio360 lanzan integración directa con SIE boliviano antes del go-to-market de EduSync | Baja | Alto | Acelerar el piloto y el primer cierre real como caso de éxito demostrable. La hiper-localización boliviana no se replica en 6 meses. |
| MR-03 | Resistencia docente a abandonar Excel por falta de confianza en el sistema | Media | Medio | Diseño Zero-Training + feedback inmediato de validación de rangos. Piloto con Marcela como embajadora interna. |
| MR-04 | El colapso del servidor SIE durante el piloto afecta la percepción de confiabilidad de EduSync | Alta | Alto | Mensajes de estado claros en UI ("El SIE está saturado, tus datos están seguros y se enviarán automáticamente"). Reintentos invisibles para el usuario. |
| MR-05 | El precio de Bs 1 800/año supera la disposición a pagar del segmento de convenio | Media | Medio | Tier introductorio de Bs 1 200/año para convenios con funcionalidades core. Validar H5 y H7 antes del lanzamiento comercial. |
| MR-06 | El equipo de desarrollo (1 persona) pierde el ritmo de Discovery por sobrecarga técnica | Alta | Medio | Cadencia mínima de 1 entrevista quincenal no negociable. Si se incumple 2 ciclos seguidos, escalar como riesgo al sponsor. |
| MR-07 | Fuga de datos entre tenants por error de configuración RLS en nuevas tablas | Baja | Crítico | El `multitenant-audit-agent` verifica política RLS en CI antes de cada despliegue. Cero excepciones a la regla de `tenant_id` obligatorio. |

---

## 14. Trazabilidad

| MRD ID | BRD ID (v2.0) | UC / DA relacionado | PRD ID (pendiente) |
|--------|---------------|---------------------|-------------------|
| MRD-N-01 | BR-004 | UC-04, DA-05 | TBD |
| MRD-N-02 | BR-001, BR-004 | UC-06, UC-04 | TBD |
| MRD-N-03 | BR-001, BR-008 | UC-01, UC-09 | TBD |
| MRD-N-04 | BR-010 | UC-10, UC-03 | TBD |
| MRD-N-05 | BR-005, BR-011 | UC-05, DA-03 | TBD |
| MRD-N-06 | BR-012 | UC-07, UC-03 | TBD |
| MRD-N-07 | BR-001 | UC-01, DA-01 | TBD |
| MRD-N-08 | BR-004 | UC-04, DA-05 | TBD |
| MRD-N-09 | BR-007 | DA-02, UC-09 | TBD |
| MRD-N-10 | BR-001, BR-011 | DA-01 | TBD |

---

## 15. Anexos

### 15.1 Entrevistas realizadas (resumen)

| Entrevistado | Perfil | Dolor principal validado | Hallazgo clave |
|--------------|--------|--------------------------|----------------|
| **Marcela** | Docente de aula, Colegio Abaroa | Miedo a descuentos salariales por error tipográfico en el SIE | Usa Excel por falta de alternativa, no por preferencia |
| **Wendy** | Secretaría académica, Colegio Abaroa | Trabajo de madrugada (2:00–4:00 AM) en cierres trimestrales | 10 ciclos de revisión manual por trimestre; reinicio total ante fallo SIE |
| **Jeanneth** | Directora institucional | Sin visibilidad de datos en tiempo real | Solo conoce el estado del colegio cuando la secretaría la llama; sin mecanismo de corrección retroactiva trazable |

### 15.2 Archivos Excel analizados

| Archivo | Institución | Hallazgos clave |
|---------|-------------|-----------------|
| `Centralizador2A_ColegioAbaroa.xlsx` | Colegio Abaroa | Desfase de notas por posición visual; decimales inconsistentes (`64.666…` → `22` vs `23`); filas fantasma de alumnos retirados |
| `REGISTRO SECUNDARIA 2026.xlsx` | Segunda institución (anónima) | Dimensión Autoevaluación presente (distinta de Colegio Abaroa); regla de nota AYUDA con `MAX(regular, ayuda)` distinta; confirma necesidad de parametrización sin redespliegue |

### 15.3 Benchmarks de mercado referenciados

- HolonIQ Latin America EdTech Report 2025 *(assumption — requiere acceso formal)*.
- Anuario Estadístico del Ministerio de Educación Bolivia 2025 *(assumption — cifra TAM extrapolada)*.
- Precios referenciados de Academium y Colegio360 son estimaciones basadas en información pública disponible. Requieren confirmación antes del lanzamiento comercial.

---

## 16. Registro de cambios

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v1.0 | 14/05/2026 | Equipo G-EduSync — Rodrigo Aspeti | Creación inicial del MRD. Basado en BRD_EduSync_v2.md (v2.0), arquitectura_funcional_EduSync.md, 01_vision_negocio.md y análisis de Excel reales. 10 MRD-N-*, 8 JTBD, 3 personas completas, 8 hipótesis a validar, TAM/SAM/SOM con notas de asunción, funnel AARRR y go-to-market para el año escolar 2027. |

---

## Checklist de entrega

- [x] TAM/SAM/SOM con fuentes y notas de asunción explícitas.
- [x] ≥ 2 personas completas (3 personas: Wendy, Marcela, Jeanneth).
- [x] ≥ 3 JTBD (8 JTBD documentados).
- [x] ≥ 2 competidores en matriz (5 alternativas: Excel, Academium, Colegio360, Google Sheets, SIE).
- [x] Positioning statement en 1 frase.
- [x] Pricing y go-to-market esbozados con tiers y estrategia de lanzamiento.
- [x] North Star + 5 KPIs fechados.
- [x] Requerimientos MRD-N-* priorizados (10 requerimientos con MoSCoW).
- [x] ≥ 3 hipótesis a validar con criterio de éxito (8 hipótesis H1–H8).
- [x] Trazabilidad a BRD v2.0 iniciada (tabla completa MRD-N → BR → UC/DA).
