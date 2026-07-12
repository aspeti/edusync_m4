# FTGO Architecture Documentation — Examen Módulo 4

**Maestrante:** [Tu nombre]  
**Branch:** `release/exam-lab`  
**Fecha de entrega:** 2026-05-22  
**Caso de estudio:** FTGO (Food To Go) — Richardson, Microservices Patterns, Manning 2019

---

## Estructura del Repositorio

```
ftgo-exam/
├── README.md                          ← Este archivo
├── docs/
│   ├── PRD.md                         ← Artefacto 1: PRD ligero de FTGO
│   ├── FSD.md                         ← Artefacto 2: FSD con ≥5 UCs Given/When/Then
│   └── adr/
│       ├── 0001-estilo-arquitectonico-microservicios.md   ← Artefacto 3: ADR estilo arquitectónico
│       └── 0002-ipc-mensajeria-asincrona-kafka.md         ← Artefacto 4: ADR mecanismo IPC
│   └── diagrams/
│       ├── c4_context.mmd             ← Artefacto 5: C4 Nivel 1 (Context)
│       └── c4_container.mmd           ← Artefacto 6: C4 Nivel 2 (Container)
└── prompts_mejorados/
    ├── prd_mejorado.md                ← Artefacto 7a: Prompt PRD mejorado (v0.2)
    └── adr_mejorado.md                ← Artefacto 7b: Prompt ADR mejorado (v0.2)
```

---

## Trazabilidad de Artefactos

| Artefacto | Fuente | Trazabilidad |
|---|---|---|
| PRD.md | Brief §A.1–A.5, Richardson Cap 1–2 | Cada NFR cita [Brief §A.4] |
| FSD.md | PRD.md, Brief §A.5 (US-01–US-03) | Cada UC mapeado a capacidad PRD + origen |
| ADR-0001 | PRD NFR-01/03/07, Brief §A.1/A.4, Richardson Cap 1–2 | Opciones trazadas a NFRs; decisión cita Cap 2 y 13 |
| ADR-0002 | PRD NFR-01/02/04/05, FSD UC-01–UC-05, Richardson Cap 3–4 | Opciones trazadas a NFRs; decisión cita Cap 3 y 4 |
| c4_context.mmd | Brief §A.2 (stakeholders), §A.4 (sistemas externos) | Todos los stakeholders y externos del brief |
| c4_container.mmd | ADR-0001 (microservicios), ADR-0002 (Kafka) | Contenedores coherentes con decisiones ADR |
| prd_mejorado.md | Prompt semilla B.1, mejoras D4 | 4 TODOs rellenados + Anti-patterns + Changelog + Métrica |
| adr_mejorado.md | Prompt semilla B.3, mejoras D4 | 4 TODOs rellenados + Anti-patterns + Verification + Changelog + Métrica |

---

## Comandos para Invocar los Prompts Mejorados

### Prerrequisitos
- Acceso a Claude.ai (Sonnet o Opus).
- Los documentos `docs/PRD.md` y `docs/FSD.md` ya generados (se usan como contexto).

### Comando 1 — Generar PRD con el prompt mejorado

```
@prompts_mejorados/prd_mejorado.md

Genera el PRD ligero de FTGO siguiendo exactamente las instrucciones del prompt.
Contexto del dominio disponible en el Anexo A del examen.
```

**Qué produce:** `docs/PRD.md` con las 5 secciones obligatorias, ≥5 NFRs con métrica y origen, y las 7 capacidades de negocio.

**Tiempo estimado:** 2–3 minutos con Claude Sonnet.

---

### Comando 2 — Generar ADR de estilo arquitectónico

```
@prompts_mejorados/adr_mejorado.md

Parámetro de decisión: "estilo arquitectónico"

Contexto disponible:
- docs/PRD.md (ya generado)
- docs/FSD.md (ya generado)
- Brief §A.1–A.5 del examen

Genera el ADR-0001 siguiendo exactamente las instrucciones del prompt mejorado.
```

**Qué produce:** `docs/adr/0001-*.md` con ≥3 opciones reales, tabla de impacto en NFRs, decisión con referencia a Richardson, y consecuencias positivas + negativas.

**Tiempo estimado:** 3–5 minutos con Claude Opus.

---

### Comando 3 — Generar ADR de IPC

```
@prompts_mejorados/adr_mejorado.md

Parámetro de decisión: "mecanismo IPC predominante"

Contexto disponible:
- docs/PRD.md
- docs/FSD.md
- docs/adr/0001-*.md (ya generado)
- Brief §A.4

Genera el ADR-0002 siguiendo exactamente las instrucciones del prompt mejorado.
```

**Qué produce:** `docs/adr/0002-*.md` evaluando REST síncrono vs Kafka async vs gRPC con trade-offs y referencia a Richardson Cap 3–4.

---

### Comando 4 — Renderizar diagramas C4

Los archivos `.mmd` en `docs/diagrams/` pueden renderizarse con:

```bash
# Con Mermaid CLI (node.js)
npx @mermaid-js/mermaid-cli -i docs/diagrams/c4_context.mmd -o docs/diagrams/c4_context.png
npx @mermaid-js/mermaid-cli -i docs/diagrams/c4_container.mmd -o docs/diagrams/c4_container.png
```

O pegando el contenido en: [https://mermaid.live](https://mermaid.live)

---

## Métricas Declaradas

### Prompt PRD (prd_mejorado.md)

**Indicador:** % de secciones del PRD completas y correctas (5 secciones × criterios mínimos).

| Corrida | Versión | Score |
|---|---|---|
| 1 | v0.1-seed | 55% |
| 2 | v0.1-seed | 65% |
| 3 | v0.1-seed | 75% |
| 4 | v0.2-mejorado | 100% |
| 5 | v0.2-mejorado | 97% |
| 6 | v0.2-mejorado | 100% |

**Mejora:** +34 puntos porcentuales (de 65% promedio a 99% promedio).

---

### Prompt ADR (adr_mejorado.md)

**Indicador:** % de criterios de calidad del checklist de Verification cumplidos (6 criterios).

| Corrida | Versión | Score |
|---|---|---|
| 1 | v0.1-seed | 50% |
| 2 | v0.1-seed | 33% |
| 3 | v0.1-seed | 67% |
| 4 | v0.2-mejorado | 100% |
| 5 | v0.2-mejorado | 100% |
| 6 | v0.2-mejorado | 100% |

**Mejora:** +50 puntos porcentuales (de 50% promedio a 100% promedio).

---

## Self-Check de Entrega

```
[x] docs/PRD.md — 5 secciones, ≥5 NFRs con métrica y origen, 7 capacidades
[x] docs/FSD.md — ≥5 UCs con Given/When/Then, cada UC mapeado a capacidad PRD
[x] docs/adr/0001-*.md — ≥3 opciones, decisión con Richardson, consecuencias +/-
[x] docs/adr/0002-*.md — ≥3 opciones, decisión con Richardson, consecuencias +/-
[x] docs/diagrams/c4_context.mmd — ≥1 Person, ≥2 System_Ext, 1 System FTGO
[x] docs/diagrams/c4_container.mmd — ≥5 contenedores, relaciones con tecnología/protocolo
[x] prompts_mejorados/prd_mejorado.md — 4 TODOs rellenados, Anti-patterns, Changelog, Métrica 3 corridas
[x] prompts_mejorados/adr_mejorado.md — 4 TODOs rellenados, Anti-patterns + Verification, Changelog, Métrica 3 corridas
[x] README.md — estructura del repo, comandos invocables, métricas declaradas
[x] Branch: release/exam-lab
```

---

## Referencias

- Richardson, Chris. *Microservices Patterns*. Manning, 2019. [Repositorio oficial FTGO](https://github.com/microservices-patterns/ftgo-application)
- [Microservices Pattern Language](https://microservices.io/)
- C4 Model: [https://c4model.com](https://c4model.com)
- Mermaid C4 Syntax: [https://mermaid.js.org/syntax/c4.html](https://mermaid.js.org/syntax/c4.html)
