---
name: quantum-opportunity-scout
description: >
  Identifica oportunidades realistas de aplicar computación cuántica
  al dominio de un producto. Mapea problemas clásicos que escalan
  mal (optimización combinatoria, simulación cuántica, búsqueda,
  factorización, ML) a familias de algoritmos cuánticos (QAOA,
  Grover, Shor, VQE, HHL, QSVM/QNN), recomienda servicios de
  Amazon Braket (simuladores SV1/DM1/TN1 + QPUs IonQ/Rigetti/IQM/
  QuEra), evalúa madurez NISQ vs producción, levanta riesgo
  cripto post-cuántica (PQC) y produce 5 bullets ejecutivos +
  POC sugerida + criterios de NO usar quantum.
allowed-tools:
  - read
  - edit
model-tier: sonnet
fsd-version-min: v0.1
status: stable
owner: Módulo 4 – UMSS
---

# Skill: quantum-opportunity-scout (problemas clásicos → algoritmos cuánticos + AWS Braket)

> Skill canónica del módulo 4. Para activarla en Claude Code o Claude Desktop,
> copia esta carpeta a `~/.claude/skills/quantum-opportunity-scout/` o a
> `.claude/skills/quantum-opportunity-scout/` en la raíz del repo del grupo.

## 1. Cuándo activarlo (triggers)

- DURANTE: identificación de POCs cuánticas exploratorias, sección §9 o §16 del DTI (capa IA / visión 2027+), conversación sobre cripto post-cuántica, evaluación de riesgo cripto a 5+ años.
- ARRANCA cuando: el usuario invoca `"@quantum-opportunity-scout"` o pide "oportunidad cuántica / Amazon Braket / problema cuántico de mi producto / impacto cripto post-cuántica".
- NO ACTIVAR cuando: el problema es claramente clásico y resoluble con heurísticas estándar (greedy, programación dinámica, MILP, ML clásico) en tiempo razonable; cuando no hay presupuesto exploratorio definido para POC.

## 2. Entradas obligatorias

El usuario MUST proporcionar:

- **Dominio del producto** (frase corta: e-commerce, marketplace, logística, fintech, healthcare, etc.).
- **Lista de 2-5 problemas computacionales** que el producto enfrenta y que **escalan mal** (tiempo crece superlinealmente con el tamaño) o son **inherentemente difíciles** (NP-hard, NP-completo, simulación física, etc.).
- **Tamaño actual del problema** (ej. 50 nodos para TSP, 1 000 acciones en portfolio, 200 features en ML).
- **Tamaño objetivo en 2027+** (cuánto debería crecer).
- **Presupuesto exploratorio** para POC quantum (típico: 200–2 000 USD para correr en Braket simuladores + 1 corrida QPU pequeña).
- **Horizonte temporal**: ¿la POC es para el módulo (exploratoria) o para producción en 2027+ (con riesgo de NISQ)?

Si falta cualquiera de dominio / 2 problemas concretos / tamaño / presupuesto, responder:
`"Necesito al menos 2 problemas computacionales concretos del producto que escalen mal, con tamaño actual aproximado y presupuesto exploratorio (incluso 0 USD si es solo análisis) antes de proponer oportunidades cuánticas."`

## 3. Fuentes de verdad (orden de precedencia)

1. NFRs del PRD (latencias críticas, restricciones de cómputo).
2. PRD y FSD del producto (qué problemas resuelve y dónde).
3. `AGENTS.md` del repo del producto (si existe; políticas de criptografía, regulación).
4. Literatura recomendada opcional: Nielsen & Chuang, *Quantum Computation and Quantum Information*; AWS Braket docs; NIST PQC Standardization (CRYSTALS-Kyber, CRYSTALS-Dilithium, FALCON, SPHINCS+).

## 4. Procedimiento

1. **Verificar inputs**. Si faltan, STOP con el mensaje del paso 2.
2. **Compartir el panorama 2026 (resumen ejecutivo, máx 4 bullets)**:
   - **Era NISQ** (Noisy Intermediate-Scale Quantum): hardware actual tiene decenas a unos cientos de qubits **ruidosos**; ventaja cuántica práctica (quantum advantage) demostrada en problemas muy específicos, **no general**.
   - **Hibridación clásico-cuántico**: el patrón productivo actual es **VQA (Variational Quantum Algorithms)**: bucle iterativo CPU clásica + QPU para sub-problema; el clásico orquesta, el QPU acelera el "núcleo duro".
   - **Quantum advantage real hoy**: en ciertos problemas de optimización combinatoria con estructura aprovechable, simulación química/materiales, y ML cuántico con kernels específicos. **Shor a escala criptográfica útil aún NO** (requiere miles de qubits con corrección de errores).
   - **Riesgo cripto**: criptografía asimétrica clásica (RSA, ECC) será rota por Shor en ~10-20 años. **Hoy mismo** se debe planear migración a **algoritmos PQC estandarizados por NIST** (CRYSTALS-Kyber para key encapsulation, CRYSTALS-Dilithium / FALCON / SPHINCS+ para firmas). Riesgo "Harvest Now, Decrypt Later" ya está activo para datos sensibles a largo plazo.

3. **Mapear cada problema del producto** a una familia de algoritmo cuántico:

   | Patrón de problema clásico | Algoritmo cuántico | Mecanismo | Madurez 2026 | Cuándo realmente conviene |
   |---------------------------|--------------------|-----------| -------------|---------------------------|
   | **Optimización combinatoria NP-hard** (TSP, vehicle routing, knapsack, scheduling, portfolio optimization, max-cut, graph coloring) | **QAOA** (Quantum Approximate Optimization Algorithm) | parametriza un Hamiltoniano del problema y optimiza variacionalmente | NISQ-friendly, ~20-100 qubits útiles | problemas medianos con estructura (no aleatorios); benchmarks vs heurísticas clásicas (simulated annealing, tabu search) deben ganarle |
   | **Simulación cuántica** (materiales, química molecular, drug discovery, baterías, catalizadores) | **VQE** (Variational Quantum Eigensolver) | encuentra el estado fundamental de un Hamiltoniano (energía mínima) | NISQ-friendly, ya hay quantum advantage en moléculas pequeñas | dominio claramente físico-químico; pharma, materiales, energía |
   | **Búsqueda no estructurada** (search en base masiva sin índice) | **Grover** | speedup cuadrático O(√N) vs O(N) clásico | requiere fault-tolerant QC para N gigantescos; demos en N pequeño | colecciones masivas sin índice clásico posible (raro: si puedes indexar, indexa) |
   | **Factorización entera / logaritmo discreto** (cripto RSA, ECC) | **Shor** | exponencial vs subexponencial mejor clásico | requiere miles de qubits lógicos con corrección de errores; **estimado 2030-2040** para RSA-2048 | NO usar para "resolver" hoy; SÍ usar como **amenaza** para planificar migración PQC |
   | **Sistemas lineales gigantes** (Ax=b con A sparse muy grande) | **HHL** (Harrow-Hassidim-Lloyd) | speedup exponencial con asunciones fuertes (cómo se cargan y leen datos) | impráctico hoy fuera de demos; data loading es cuello de botella | poco aplicable a productos típicos; relevante en ML / física |
   | **Machine Learning con kernels complejos** | **QSVM** (Quantum SVM), **QNN** (Quantum Neural Net), **Quantum Kernel Methods** | espacio de features cuántico para clasificación / regresión | NISQ-friendly, hay benchmarks pero la **ventaja clásica frecuente** | datasets pequeños/medianos donde el kernel cuántico captura estructura no accesible a kernels clásicos |
   | **Sampling de distribuciones complejas** (Monte Carlo financiero) | **Quantum Amplitude Estimation (QAE)** | speedup cuadrático en pricing de derivados, value-at-risk | NISQ-promising, requiere ~50-100 qubits útiles | fintech, riesgo, simulación financiera donde Monte Carlo clásico es caro |

4. **Mapear a servicios de Amazon Braket**:

   | Recurso Braket | Cuándo | Costo aproximado (orden de magnitud) |
   |----------------|--------|--------------------------------------|
   | **Simulador SV1** (state vector) | hasta ~34 qubits, simulación ideal sin ruido | $0.075–$0.30 / min |
   | **Simulador DM1** (density matrix) | hasta ~17 qubits, simulación con ruido modelado | $0.075–$0.30 / min |
   | **Simulador TN1** (tensor network) | hasta ~50 qubits con circuitos profundos limitados | similar |
   | **Simulador local** (en notebook / Braket SDK) | hasta ~25 qubits, debugging y dev | gratis |
   | **QPU IonQ** (trapped ions, ~32 qubits, all-to-all connectivity) | algoritmos que necesitan conectividad alta (QAOA, VQE) | $0.01 / shot + $0.30 / task |
   | **QPU Rigetti** (superconducting, ~80 qubits) | volumen alto, latencia baja | $0.00035–$0.00075 / shot + $0.30 / task |
   | **QPU IQM** (superconducting, ~20 qubits) | QAOA, VQE en problemas pequeños | similar |
   | **QPU QuEra** (neutral atoms, hasta ~256 qubits analógicos) | optimización combinatoria a mayor escala vía Hamiltonianos de Ising | tarificado por tarea |
   | **Braket Hybrid Jobs** | bucle clásico-cuántico orquestado (VQA) sin reservar QPU manualmente | costo de QPU + costo del orquestador |

5. **Filtrar agresivamente las propuestas**. Aplicar criterios de **NO usar quantum**:
   - El problema **ya tiene solución clásica buena** (heurística probada, MILP, programación dinámica) en tiempo aceptable → **NO** quantum.
   - **Tamaño del problema en producción es pequeño** (< 20 variables / nodos) → simulado clásicamente o resuelto exactamente; **NO** quantum.
   - El problema **no tiene estructura aprovechable** (variables aleatorias, sin correlación) → quantum no gana; **NO** quantum.
   - **Latencia crítica < 1 s** en producción → QPU actuales tienen latencia decenas a centenas de ms + cola; **NO** producción, solo POC offline o batch.
   - **No hay benchmark contra mejor solver clásico actual** → **NO** justificable; primero hay que medir cuánto pierde el clásico.
   - **Datos masivos cargados al QPU en cada llamada** (data loading dominante) → HHL y similares pierden; **NO** quantum.

6. **Producir 1-3 oportunidades reales** (no forzar 5 si no las hay) y **1 alerta cripto PQC** si el producto maneja datos sensibles a largo plazo.

7. **Recomendar POC concreta** con presupuesto y resultado esperado.

## 5. Salida esperada

### 5.1 Resumen del panorama 2026 (3-4 bullets, máximo medio párrafo)

Una declaración breve para el DTI §9 o §16 del producto que ubique al lector en el contexto NISQ + hibridación + riesgo PQC.

### 5.2 Bullets por oportunidad (≥ 1, máx 5)

Para cada oportunidad seleccionada:

```markdown
**<Nombre del problema en lenguaje de negocio>**
- Problema clásico: <descripción + por qué escala mal: NP-hard, exponencial en X, …>.
- Tamaño actual / objetivo: <ej. 50 / 500 nodos>.
- Algoritmo cuántico propuesto: <QAOA / VQE / QAE / etc.>.
- Servicio AWS Braket recomendado: <SV1 para POC simulada → IonQ para validación pequeña>.
- Madurez 2026: <NISQ promising / NISQ early / lejos de producción / amenaza cripto>.
- POC sugerida: <objetivo medible, p. ej. "QAOA vs simulated annealing sobre 20 nodos, target: gap a óptimo < 5 %; presupuesto ~200 USD en Braket SV1">.
- Criterio de éxito: <métrica concreta: tiempo / calidad / costo vs baseline clásico>.
- Riesgos: <ruido NISQ, calibración, depth budget, cuello de botella en data loading, etc.>.
```

### 5.3 Alerta de cripto post-cuántica (si aplica)

Si el producto guarda datos sensibles con vida útil > 5-10 años (financieros, médicos, contratos, identidad) o usa criptografía asimétrica clásica (RSA, ECDSA) para firmar / cifrar:

```markdown
**Riesgo cripto post-cuántica**
- Datos sensibles a largo plazo: <ej. historiales médicos, contratos firmados, transacciones financieras>.
- Riesgo "Harvest Now, Decrypt Later" activo: un atacante puede capturar tráfico cifrado hoy y descifrarlo cuando haya QPU suficiente (~2030-2040).
- Recomendación: planificar migración a algoritmos NIST PQC en horizonte 2027-2030:
  - **Key encapsulation**: CRYSTALS-Kyber.
  - **Firmas digitales**: CRYSTALS-Dilithium (general), FALCON (firmas pequeñas), SPHINCS+ (basado en hash, conservador).
- Estrategia incremental: **crypto-agility** (capa de abstracción de algoritmos cripto que permita reemplazar primitivas sin reescribir la app).
```

### 5.4 Sugerencia de bullets para descartar (anti-oportunidades)

Lista breve de "qué problemas del producto **NO** son buen fit para quantum y por qué". Esto es tan valioso como las oportunidades.

## 6. Verificación (criterios de "bien hecho")

- Al menos **una oportunidad** propuesta tiene **POC concreta** con presupuesto declarado < 2 000 USD.
- Cada oportunidad declara **algoritmo cuántico específico** (no "computación cuántica" en abstracto).
- Cada oportunidad declara **madurez** (NISQ promising / NISQ early / lejos / amenaza).
- Cada oportunidad declara **baseline clásico** contra el cual se mide el beneficio.
- Si el producto maneja datos sensibles a largo plazo, **hay alerta PQC** con algoritmos NIST específicos.
- Hay **lista de anti-oportunidades** (problemas que se evalúan y se descartan, con motivo).
- Ninguna oportunidad propone usar Shor para "romper criptografía hoy" (anti-patrón).
- Ninguna oportunidad propone QPU en latencia crítica de producción (< 1 s end-to-end requerido).

## 7. Anti-patrones específicos

- **"Vamos a usar IA cuántica para X"** sin entender qué algoritmo se aplica → el skill debe forzar a nombrar QAOA / VQE / Grover / etc.
- **Quantum para todo**: sobre-vendido como bala de plata. Mitigación: aplicar criterios de NO usar (paso 5).
- **Shor mañana**: anunciar que se va a usar Shor en producción este año. Mitigación: marcar madurez real (lejos de producción para criptografía útil).
- **HHL para resolver Ax=b en producción**: olvida el data loading exponencial. Mitigación: marcar como impráctico hoy.
- **POC sin baseline clásico**: imposible saber si el quantum ganó. Mitigación: SIEMPRE declarar el solver clásico contra el cual se compara.
- **PQC ignorado** en sistemas con datos sensibles a largo plazo. Mitigación: levantar alerta y proponer crypto-agility.
- **Confundir "quantum-inspired" con quantum**: simulated annealing / tabu search inspirados en quantum NO usan QPU. Mitigación: declarar explícitamente si la solución es clásica inspirada o cuántica real.
- **Encerrarse en un proveedor de QPU**: hardware muy heterogéneo (trapped ion, superconducting, neutral atoms). Mitigación: empezar en simulador, abstraer con Braket SDK.

## 8. Mini ejemplo de invocación

> "Producto: marketplace logístico B2B en Bolivia. Dominio: ruteo de entregas urbanas. Problemas que escalan mal: (1) Vehicle Routing Problem con 200 paradas/día por ciudad, hoy resolvemos con greedy + 2-opt y damos rutas sub-óptimas (~10 % peor que óptimo); (2) asignación de 50 conductores a 200 rutas con restricciones (turnos, vehículos, zonas); (3) firmamos digitalmente contratos de transporte con ECDSA (vida útil del contrato 10 años). Presupuesto exploratorio: 500 USD. Horizonte: POC para módulo, producción 2028 si va. Usa el skill `quantum-opportunity-scout` y genera oportunidades + alerta PQC."

## 9. Modos de fallo conocidos

- Sin problemas que escalen mal declarados → STOP, pedir 2 concretos antes de seguir.
- Tamaño actual muy pequeño (< 20 variables) → desincentivar quantum y proponer mejor solver clásico.
- Latencia crítica de producción → POC sí, producción no en NISQ era.
- Restricción regulatoria que prohíbe enviar datos a Braket (residencia de datos sensibles) → POC con simulador local o región AWS compatible.
- "Quiero usar quantum porque suena bien" sin problema → STOP, exigir problema concreto que escala mal.

## 10. Registro de cambios

| Versión | Fecha       | Autor                  | Cambio          |
|---------|-------------|------------------------|-----------------|
| 0.1.0   | 21/05/2026  | M.Sc. Edson Terceros   | versión inicial; familias de algoritmos cuánticos + AWS Braket + criterios NO usar + alerta PQC |
