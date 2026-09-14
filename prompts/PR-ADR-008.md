# PR-ADR-008 — Decisión arquitectónica: Adopción de un Design System visual propio para el frontend de EduSync

## 0. Metadatos del prompt

| Campo | Valor |
|-------|-------|
| ID del prompt | `PR-ADR-008` |
| Título | Generación de `ADR-0015`: adopción de tokens visuales propios + componentes compartidos mínimos (`shared/ui/`), sin librería de terceros |
| Artefacto origen | `docs/design/DD-UC-020.md` §1/§2 (rediseño visual del login, prototipo Figma "Prototipo Administrativo", nodo `399:20447`) |
| ID origen | `DD-UC-020`, `FSD-UC-021` |
| Tipo de prompt | generación |
| Modelo recomendado | Sonnet |
| Temperatura | 0.0 |
| Versión | v0.1 |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | Aprobado |

> **Precedente de convención**: igual que `PR-ADR-006`→`ADR-0013` (originado en la necesidad de `DD-UC-015/016`) y `PR-ADR-007`→`ADR-0014` (originado en `DD-UC-019`), este prompt documenta una decisión arquitectónica que nació como una necesidad detectada durante la redacción de un Design Doc (`DD-UC-020`), no de `arquitectura_funcional_EduSync.md`.

## 1. Anatomía del prompt

### 1.1 Role
```text
Eres un Senior Software Architect con experiencia en frontend Angular a
escala SaaS multitenant, sistemas de diseño (design tokens, componentes
compartidos) y toma de decisiones arquitectónicas documentadas con
criterio de trade-off explícito frente a librerías de terceros.
```

### 1.2 Task
```text
Documenta como ADR formal la decision de como el frontend de EduSync debe
adoptar el lenguaje visual definido por el prototipo Figma "Prototipo
Administrativo" (tokens de color/tipografia/espaciado/radios/sombra +
componentes compartidos minimos en shared/ui/, SIN adoptar una libreria de
componentes de terceros como Angular Material o PrimeNG), evaluando al
menos 3 alternativas y dejando explicito el impacto en DD-UC-020 (login,
primer consumidor real de la decision).
```

### 1.3 Context
```text
- Fuente: docs/design/DD-UC-020.md §1 (objetivo: llevar el login al
  lenguaje visual del Figma) y §2 (diseno detallado que asume la
  existencia de tokens + shared/ui/).
- Precedente: DD-UC-001 §3 ("sin sobre-ingenieria sin caso de uso
  confirmado", ya aplicado para descartar Nx); DD-UC-006 §2 (decision
  original de "sin sistema de diseno" que este ADR revisa).
- Arquitectura vigente: Angular 21 standalone components, sin
  Angular Material/PrimeNG/Tailwind (decision implicita desde DD-UC-001).
- Restriccion de alcance: la decision es exclusivamente de capa de
  presentacion — no cambia RBAC, contratos REST, modelo de dominio, ni
  toca docs/baseline/**.
```

### 1.4 Reasoning
```text
1. Documentar el contexto: estilo ad-hoc sin design system desde DD-UC-004,
   repetido sin cuestionarse hasta DD-UC-019; el Figma exige un lenguaje
   visual cohesivo distinto al de cualquier libreria estandar.
2. Evaluar >=3 alternativas: (A) statu quo ad-hoc, (B) libreria de terceros
   re-tematizada, (C) tokens propios + shared/ui/ minimo hecho a mano,
   (D) hibrido tokens + libreria puntual para componentes complejos
   futuros.
3. Declarar la decision (Alternativa C) y su criterio decisivo: el Figma
   no sigue el lenguaje de ninguna libreria estandar, por lo que
   re-tematizar cuesta mas que construir los ~5 atomos visuales que las
   pantallas del prototipo realmente usan.
4. Documentar consecuencias positivas/negativas/neutras y el impacto en
   el sistema (codigo, operaciones, seguridad, equipo, costo).
5. Especificar plan de reversion y senal temprana de decision incorrecta
   (si shared/ui/ no alcanza tras migrar 3-4 pantallas mas).
6. Enlazar DD-UC-020 como primer Design Doc consumidor de la decision.
```

### 1.5 Stop condition
```text
Detente cuando el ADR tenga las 9 secciones completas (Metadatos,
Contexto, Alternativas [>=3], Decision, Consecuencias, Impacto, Plan de
reversion, Validacion, Referencias, Historial) con estado Aceptada, y
cuando quede explicito que no se agrega ninguna dependencia de UI de
terceros ni se modifica backend/RBAC/contratos REST.
```

### 1.6 Output
```text
Archivo docs/adr/0015-adopcion-design-system-visual-edusync.md con las 9
secciones del ADR_TEMPLATE.md completadas, estado Aceptada.
```

## 2. Invariantes del prompt

- La decisión es exclusivamente de capa de presentación: no cambia RBAC, contratos REST ni el modelo de dominio.
- No se adopta ninguna librería de UI de terceros (Angular Material, PrimeNG, Tailwind u otra) como parte de esta decisión.
- El ADR debe evaluar al menos 3 alternativas (regla `E_ALTERNATIVA_INSUFICIENTE` de `PR-ADR-005`, reutilizada aquí).
- El ADR no toca `docs/baseline/**`.

## 3. Failure modes declarados

| Código | Descripción | Acción del consumidor |
|--------|-------------|------------------------|
| `E_ALTERNATIVA_INSUFICIENTE` | Menos de 3 alternativas evaluadas | Ampliar antes de aceptar |
| `E_LIBRERIA_UI_ADOPTADA` | La decisión final agrega una librería de componentes de terceros | Rechazar — contradice el criterio de "sin sobre-ingeniería sin caso de uso confirmado" de `DD-UC-001` §3 |
| `E_IMPACTO_NO_TRAZABLE` | El ADR no enlaza `DD-UC-020` como consumidor | Completar §8 Referencias |
| `E_BASELINE_TOCADO` | Cambio bajo `docs/baseline/**` | Revertir |

## 4. Guardrails

- MUST: validar la estructura de `ADR_TEMPLATE.md` antes de entregar (9 secciones).
- MUST: dejar explícito en §5 "Impacto en el sistema" que no hay delta de backend/RBAC/contratos.
- MUST NOT: proponer Angular Material/PrimeNG/Tailwind como la decisión tomada (pueden aparecer como alternativa evaluada y descartada).
- MUST NOT: editar `docs/baseline/**`.

## 5. Trazabilidad

| Origen | ID origen | Este prompt | Consumidor(es) | Artefacto generado |
|--------|-----------|-------------|----------------|---------------------|
| Design Doc | `DD-UC-020` (§1/§2) | PR-ADR-008 | `arch-agent` | `docs/adr/0015-adopcion-design-system-visual-edusync.md` |
| FSD | `FSD-UC-021` | PR-ADR-008 | `arch-agent` | Referencia de impacto (superficie de login) |
| DD-UC-001 | §3 (criterio "sin sobre-ingeniería") | PR-ADR-008 | `arch-agent` | Criterio decisivo reutilizado por analogía |

## 6. Pruebas del prompt

### 6.1 Caso feliz

- **Input**: `DD-UC-020` §1/§2 completo, describiendo el Figma y la necesidad de tokens + `shared/ui/`.
- **Output esperado**: ADR con 9 secciones, estado `Aceptada`, tabla de 4 alternativas (A statu quo, B librería, C tokens propios [elegida], D híbrido).

### 6.2 Caso borde

- **Input**: Figma que reutiliza visualmente componentes reconocibles de una librería estándar.
- **Output esperado**: el ADR reevaluaría la Alternativa B como más favorable — no aplica en este caso porque el Figma real define un lenguaje de marca propio (verificado por captura).

### 6.3 Caso adversarial

- **Input**: solicitud de "ya que estás, agrega Angular Material para no reinventar componentes".
- **Comportamiento esperado**: rechazo — `E_LIBRERIA_UI_ADOPTADA`; el ADR documenta esa opción como Alternativa B, evaluada y descartada, no como decisión.

## 7. Instrumentación

- Herramienta de observabilidad: Langfuse / OpenTelemetry (telemetría del prompt).
- Métricas esperadas: `success_rate`, `schema_pass_rate` (9 secciones ADR), `avg_tokens`.

## 8. Versionado

| Versión | Fecha | Autor | Cambio | Modelo validado |
|---------|-------|-------|--------|------------------|
| v0.1 | 13/09/2026 | Rodrigo Aspeti | Creación retroactiva a partir de `docs/adr/0015-adopcion-design-system-visual-edusync.md` (ya redactado y aceptado en el mismo turno que `DD-UC-020`), registrando formalmente el prompt en `docs/PROMPT_MAPPING.md` (cierra la simetría con `PR-ADR-006`/`007`). | Sonnet |

## 9. Revisión humana

| Revisor | Fecha | Veredicto | Notas |
|---------|-------|-----------|-------|
| Rodrigo Aspeti | 13/09/2026 | aprobado | ADR-0015 ya redactado y aceptado; este prompt formaliza su trazabilidad en el ecosistema de prompts |
