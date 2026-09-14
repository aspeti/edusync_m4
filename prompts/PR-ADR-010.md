# PR-ADR-010 — Decisión arquitectónica: Adopción de Spring AI como cliente LLM detrás de `LlmPort`

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-010` |
| Título | Generación de `ADR-0017`: Spring AI solo en `infrastructure`, detrás del puerto hexagonal `LlmPort` |
| Artefacto origen | `docs/design/DD-UC-022.md` §1/§2 (refactor del spike LLM) |
| ID origen | `DD-UC-022`, `NFR-007` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

> **Precedente de convención**: igual que `PR-ADR-008`→`ADR-0015`, este prompt documenta una decisión que nació en un Design Doc (`DD-UC-022`), no en `arquitectura_funcional_EduSync.md`.
>
> **Numeración**: `PR-ADR-009` está reservado por `DD-UC-021` (`ADR-0016`). Este ADR es `0017` ↔ `PR-ADR-010`.

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Software Architect con experiencia en Spring Boot 4,
arquitectura hexagonal (Ports & Adapters) y adopción de librerías de
cliente HTTP/LLM sin filtrar el framework hacia el dominio.
```

### 1.2 Task
```text
Documenta como ADR formal la decision de adoptar Spring AI como cliente
HTTP/LLM detras del puerto existente LlmPort (sin reemplazar el puerto
por tipos de Spring AI, sin cambiar POST /api/v1/ai/chat ni edusync.ai.*,
sin cambiar el proveedor default Ollama llama3.1:latest), evaluando al
menos 3 alternativas y dejando explicito el impacto en DD-UC-022.
```

### 1.3 Context
```text
- Fuente: spike com.edusync.shared.ai (RestClient ad-hoc a Ollama y
  Open WebUI); DTI §9 historico "sin IA en runtime"; AGENTS.md §4/§5/§7.
- Stack vivo: Java 25, Spring Boot 4.1.0 (ADR-0008). Dependencia nueva
  exige ADR y verificacion Jakarta EE 11 / Spring Framework 7.0.8.
- Restriccion: no tocar docs/baseline/**. La version del BOM se fija
  en la ejecucion de PR-IMPL-022, no en el ADR.
```

### 1.4 Reasoning
```text
1. Contexto: cliente JSON propio vs. DTI §9; hexagono ya tiene LlmPort.
2. Alternativas: (A) statu quo RestClient, (B) ChatClient en application/,
   (C) Spring AI solo en adaptadores, (D) cambiar a proveedor cloud.
3. Decidir C y criterio: el dolor es el I/O HTTP, no el puerto.
4. Consecuencias, impacto, reversion (BOM incompatible -> volver a A).
5. Validacion: mvn test, cero imports Spring AI en domain/application.
6. Enlazar DD-UC-022 / PR-IMPL-022.
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones del template, estado
Aceptada, Alternativa C explicita, y quede claro que domain/application
no importan Spring AI ni se edita docs/baseline/**.
```

### 1.6 Output
```text
Archivo docs/adr/0017-adopcion-spring-ai-cliente-llm.md, estado Aceptada.
```

## 2. Invariantes del prompt

- Spring AI no entra a `domain/` ni `application/`.
- El contrato `POST /api/v1/ai/chat` y `edusync.ai.*` no se redefinen en el ADR como “a cambiar”.
- Al menos 3 alternativas.
- Sin editar `docs/baseline/**`.

## 3. Failure modes declarados

| Código | Descripción | Acción |
|--------|-------------|--------|
| `E_ALTERNATIVA_INSUFICIENTE` | Menos de 3 alternativas | Ampliar |
| `E_SPRING_AI_EN_DOMAIN` | La decisión filtra Spring AI al dominio | Revertir |
| `E_BOM_PINNEADO_A_CIEGAS` | El ADR fija un BOM no verificado contra Boot 4.1.0 | Corregir: el pin queda para `PR-IMPL-022` |
| `E_BASELINE_TOCADO` | Edición de `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: evaluar ≥3 alternativas y declarar Alternativa C.
- MUST NOT: pinnear versión de Spring AI sin verificación de BOM en la ejecución.
- MUST NOT: editar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-022` | PR-ADR-010 | `arch-agent` | `docs/adr/0017-adopcion-spring-ai-cliente-llm.md` |
| NFR | `NFR-007` | PR-ADR-010 | `arch-agent` | Invariante de logs en el ADR |

## 6. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 13/09/2026 | Rodrigo Aspeti | Creación y aprobación; `ADR-0017` aceptado en el mismo turno. | Sonnet |
