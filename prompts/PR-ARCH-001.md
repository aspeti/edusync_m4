# PR-ARCH-001 — Generación de arquitectura funcional del core EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ARCH-001` |
| Título | Diseño de la arquitectura funcional del core EduSync (10 UCs + 5 DAs) |
| Artefacto origen | BRD + vision de negocio |
| ID origen | `BR-001..BR-008`, `01_vision_negocio.md` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 14/05/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Solution Architect especializado en plataformas SaaS multitenant
para el sector educativo latinoamericano, con dominio de Java 21, Spring Boot 3,
PostgreSQL y normativa del Ministerio de Educacion de Bolivia (SIE).
```

### 1.2 Task
```text
Diseña la arquitectura funcional del core de EduSync cubriendo los 10 procesos
criticos de registro de calificaciones y gestion academica centralizada,
asegurando escalabilidad para multiples unidades educativas (tenants).
```

### 1.3 Context
```text
- Producto: EduSync — plataforma SaaS B2B multitenant para Bolivia.
- Problema central: triple digitacion manual (Excel → Excel → SIE).
- Stack autoritativo: Java 21, Spring Boot 3.3, PostgreSQL 15, Angular 17, AWS.
- Restricciones: aislamiento multitenant (tenant_id + RLS), RBAC por rol,
  identificacion de estudiantes solo por RUDE.
- Stakeholders UX: Marcela (Docente), Wendy (Secretaria), Jeanneth (Directora).
- Entradas esperadas: vision de negocio (01_vision_negocio.md), BRD_EduSync.md.
```

### 1.4 Reasoning
```text
1. Mapear los flujos principales a 10 UCs criticos (UC-01..UC-10).
2. Por cada UC: definir Actores, Entradas, Invariantes de negocio, Salidas.
3. Identificar 5 decisiones arquitectonicas (DA-01..DA-05).
4. Establecer trazabilidad entre necesidades UX y componentes del sistema.
5. Verificar que ningun UC proponga implementacion, codigo o esquema de tablas.
```

### 1.5 Stop condition
```text
Detente al cubrir los 10 UCs y listar las 5 DAs con justificacion tecnica.
No propongas codigo, esquemas de tablas ni mapeos a servidores AWS.
```

### 1.6 Output
```text
Markdown con tres secciones:
1. "Encuadre del Core EduSync" (1 parrafo).
2. "Diez casos de uso criticos" (tablas Actores/Entradas/Invariantes/Salidas).
3. "Cinco decisiones arquitectonicas" (justificacion DA-01..DA-05).
```

## 2. Invariantes del prompt

- Ningun UC puede proponer codigo de implementacion.
- El RUDE es la unica clave de identificacion de estudiantes.
- Toda invariante de negocio debe ser verificable sin acceder al codigo.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_MISSING_CONTEXT` | Falta vision_negocio.md o BRD | STOP, solicitar el artefacto |
| `E_CODE_PROPOSED` | El output contiene fragmentos de codigo | Rechazar y regenerar |
| `E_UC_INCOMPLETO` | Algun UC no tiene los 4 campos | STOP, completar |

## 4. Guardrails

- MUST: validar que ningún UC contiene código o DDL.
- MUST: registrar `promptId`, `versión`, `modelo`, `tokens`, `latencia`.
- MUST NOT: proponer stack técnico en la arquitectura funcional (pertenece al FSD/DTI).

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| BRD | `BR-001..BR-008` | PR-ARCH-001 | `docs-agent` | `docs/arquitectura_funcional_EduSync.md` |
| Vision de negocio | `01_vision_negocio.md` | PR-ARCH-001 | `docs-agent` | Encuadre del core y 10 UCs |

## 6. Pruebas del prompt

### 6.1 Caso feliz
- **Input**: `01_vision_negocio.md` completo + `BRD_EduSync.md` con ≥6 BRs.
- **Output esperado**: 10 UCs con 4 campos cada uno, 5 DAs con justificación técnica.

### 6.2 Caso borde
- **Input**: BRD con solo 3 BRs documentados.
- **Output esperado**: el agente infiere UCs adicionales de la visión de negocio; alerta sobre BRs faltantes.

### 6.3 Caso adversarial
- **Input**: solicitud de incluir DDL de tablas PostgreSQL en la arquitectura funcional.
- **Comportamiento esperado**: rechazo con `E_CODE_PROPOSED`; el DDL pertenece al FSD/LFSD.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry.
- Métricas esperadas: `success_rate`, `schema_pass_rate`, `avg_tokens`, `p95_latency`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 14/05/2026 | Rodrigo Aspeti | Creación desde contrato inline PROMPT_MAPPING.md v0.9 | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 28/05/2026 | aprobado | Materializado por skill `materialize-prompt-files` |
