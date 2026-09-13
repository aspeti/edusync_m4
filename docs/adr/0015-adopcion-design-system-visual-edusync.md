# Architecture Decision Record (ADR)

## ADR-0015: Adopción de un Design System visual propio (tokens + componentes compartidos) para el frontend de EduSync

### Metadatos

| Campo | Valor |
|-------|-------|
| Número | `0015` |
| Título | Adopción de un Design System visual propio (tokens + componentes compartidos) para el frontend de EduSync |
| Fecha | 13/09/2026 |
| Autor(es) | Rodrigo Aspeti |
| Estado | **Aceptada** |
| Alcance | Frontend Angular (`frontend/src/app/shared/`). No afecta al backend, a RBAC, a contratos REST ni al modelo de dominio. No afecta al baseline congelado de M4 (`docs/baseline/`, tag `release/2.0.0`). No supersede a ningún ADR anterior. |
| Stakeholders consultados | Rodrigo Aspeti (Dev Lead / PM, único integrante de G-EduSync) |
| ADR relacionado | Ninguno — primera decisión de diseño visual del proyecto. Sirve de base a `DD-UC-020` (login) y a los Design Docs de UI posteriores que adopten el mismo lenguaje visual. |

### 1. Contexto

Desde `DD-UC-004` (primer frontend real, login + consola SysAdmin) el proyecto tomó la decisión explícita — documentada en `DD-UC-006` §2 — de "reutilizar el patrón sin sistema de diseño de `features/plataforma/`": estilos en línea o por componente, sin paleta de color ni tipografía centralizadas, sin biblioteca de componentes compartida. Esa decisión fue razonable para llegar rápido a un *vertical slice* funcional con un equipo de un desarrollador, y se repitió sin cuestionarse en `DD-UC-006/007/009/011/012/013/014/015/016/017/018/019`.

Se recibió un prototipo de Figma ("Prototipo Administrativo", `PdeTWx28MJYxcQ0hrXhhRZ`) que define un lenguaje visual cohesivo y consistente entre pantallas: paleta primaria azul marino (`#0F2A5C` aprox.) para navegación y acciones primarias, tarjetas blancas con sombra suave y esquinas redondeadas (`Container`/`Card` con `shadow` propio), microetiquetas de formulario en mayúsculas (`CORREO ELECTRÓNICO`, `RUDE`), barra superior de navegación de marca (`EDUSYNC` + navegación horizontal + iconos + menú de usuario) y pie de página institucional repetido en todas las pantallas. Este lenguaje visual es sustancialmente más elaborado que el estilo ad-hoc actual y el negocio (vía el usuario, rol Dev Lead/PM) pidió alinear la UI existente y futura a él, empezando por el login (`DD-UC-020`).

Fuerzas en tensión: consistencia visual y percepción de producto profesional (crítico para un SaaS B2B que se vende a colegios) vs. costo de introducir una dependencia nueva o un proceso de tokens en un equipo de un desarrollador; mantener Angular 21 *standalone components* sin librerías de UI (decisión implícita desde `DD-UC-001`, nunca se agregó Angular Material/PrimeNG/Tailwind) vs. la velocidad que una librería de terceros da para no reinventar componentes (inputs, tablas, badges, diálogos).

Lo que se sabe: el Figma no usa un lenguaje visual "de librería" reconocible (no es Material Design ni un tema de PrimeNG *out of the box*) — es un sistema de marca propio (tipografía, radios, sombras y paleta específicos de EduSync). Lo que no se sabe todavía: si en un release futuro se necesitarán componentes complejos (date pickers, autocomplete) que sí conviene resolver con una librería; este ADR no cierra esa puerta.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras | Costo aproximado |
|-------------|------|---------|-------------------|
| A. Mantener el statu quo: estilos ad-hoc por feature, sin tokens ni componentes compartidos | Cero costo de migración; cada Design Doc sigue estilando su propia pantalla | La inconsistencia visual ya es visible entre features (`features/plataforma/` vs `features/academico/`); no resuelve el pedido del Figma; cada nueva pantalla reinventa color/tipografía | Bajo hoy, alto acumulado (cada pantalla nueva diverge más) |
| B. Adoptar una librería de componentes de terceros (Angular Material o PrimeNG) y re-temizarla con los tokens del Figma | Componentes complejos (date picker, tabla con sorting) ya resueltos; comunidad grande | El Figma no sigue el lenguaje visual de ninguna librería estándar — re-temizar Material para que luzca como el Figma exacto (radios, sombras, microetiquetas mayúsculas) es más trabajo que construir los pocos átomos que se usan hoy; agrega una dependencia nueva y su curva de aprendizaje a un proyecto que hasta ahora es 100 % Angular *standalone* sin librerías de UI | Medio-alto (dependencia nueva + re-tematización) |
| C. Extraer los tokens visuales del Figma (color, tipografía, espaciado, radios, sombra) como variables SCSS globales en `frontend/src/styles/` y un set mínimo de componentes compartidos hechos a mano en `frontend/src/app/shared/ui/` (card, input con label+icono, botón primario/secundario, badge de estado, shell de navegación), sin librería de terceros | Coincide exactamente con el Figma sin re-tematizar nada ajeno; cero dependencias nuevas, consistente con la arquitectura *standalone components* ya elegida (`DD-UC-001` §3, alternativa D); alcance acotado y revisable pantalla por pantalla, igual que el resto de los Design Docs del proyecto | Los componentes complejos que no existen todavía (si aparecen) se seguirán construyendo a mano | Bajo — mismo patrón de "construir solo lo que se usa" ya aplicado en `academico` |
| D. Híbrido: tokens propios (como en C) + Angular Material solo para 1-2 componentes complejos que hoy no existen (ej. un futuro date range picker) | Flexibilidad a futuro | Prematuro: ningún Design Doc actual ni el Figma revisado hasta ahora requieren un componente que no se pueda construir a mano; agregar la dependencia sin un caso de uso concreto es sobre-ingeniería (mismo criterio que descartó Nx en `DD-UC-001` §3) | — |

### 3. Decisión

> **Elegimos la Alternativa C.** Se crean tokens visuales propios (`frontend/src/styles/_tokens.scss`: color, tipografía, espaciado, radios, sombra, extraídos 1:1 del Figma) y un set mínimo de componentes compartidos *standalone* en `frontend/src/app/shared/ui/` (card, form-field con label mayúscula + icono, botón primario/secundario, badge de estado, shell de navegación con topbar de marca). No se agrega Angular Material, PrimeNG ni Tailwind.

El criterio decisivo es que el Figma define un lenguaje visual propio de marca, no el de una librería estándar: adoptar Material o PrimeNG obligaría a pelear contra los estilos por defecto de la librería para lograr el aspecto exacto pedido (microetiquetas mayúsculas, tarjetas con sombra suave específica, topbar azul marino de marca), lo cual cuesta más que construir los ~5 átomos visuales que las pantallas del prototipo realmente usan. Es además el mismo criterio de "construir solo lo necesario, sin dependencias nuevas sin caso de uso confirmado" que el proyecto ya aplicó al descartar Nx (`DD-UC-001` §3) y que mantiene la arquitectura 100 % *standalone components* de Angular 21 sin librerías de UI de terceros.

### 4. Consecuencias

#### 4.1 Positivas

- Un solo lugar (`_tokens.scss`) define color/tipografía/espaciado — cambios de marca futuros (ej. un cliente white-label) se resuelven ahí, no pantalla por pantalla.
- Cero dependencias nuevas; el bundle de producción no crece por una librería de componentes completa.
- Cada Design Doc de UI posterior (`DD-UC-020` en adelante) puede enlazar este ADR en vez de re-justificar su propia paleta de colores.

#### 4.2 Negativas / costos

- Los componentes compartidos (`shared/ui/`) son responsabilidad del proyecto: no hay comunidad externa que corrija bugs de accesibilidad o *edge cases* de un `<select>` o un `date input` complejo.
- Migrar las pantallas ya construidas (`features/plataforma`, `features/usuarios`, `features/academico`) al nuevo lenguaje visual es trabajo incremental, no automático — cada una necesita su propio Design Doc de "mejora visual" (ver `DD-UC-021` y siguientes) para no mezclar riesgo de regresión funcional con el cambio de estilos.
- Si en el futuro aparece un componente realmente complejo (ej. un calendario de rango de fechas para reportería), este ADR no lo resuelve — se evaluará entonces si conviene una alternativa D acotada a ese componente puntual.

#### 4.3 Neutras / observables

- No cambia RBAC, contratos REST, ni el modelo de dominio — es una decisión exclusivamente de capa de presentación.
- El árbol de `frontend/src/app/shared/` gana un subdirectorio `ui/` nuevo; `shared/layout/shell.component.ts` (ya existente desde `DD-UC-006`) es el primer consumidor obligado de los tokens (topbar de marca).

### 5. Impacto en el sistema

- **Código**: nuevo `frontend/src/styles/_tokens.scss` (o equivalente `:root` con custom properties CSS); nuevo `frontend/src/app/shared/ui/` (card, form-field, button, badge, page-shell). `shared/layout/shell.component.ts` se actualiza para usar el shell de marca (topbar navy + nav + footer) en vez de su navegación mínima actual.
- **Operaciones**: ninguna — no afecta despliegue, `docker-compose.yml` ni CI (`ng build` sigue siendo el único gate).
- **Seguridad**: ninguna — sin cambios de superficie de ataque, RBAC ni datos.
- **Equipo**: ninguna curva de aprendizaje externa (no hay librería nueva); sí una convención nueva a seguir en cada Design Doc de frontend futuro (usar los tokens/componentes de `shared/ui/`, no estilos ad-hoc nuevos).
- **Costo**: sin impacto en factura AWS ni licencias (no hay librería de pago ni servicio nuevo).

### 6. Plan de reversión

- Señal temprana de que la decisión fue incorrecta: si, al migrar 3-4 pantallas más, los componentes de `shared/ui/` no alcanzan a cubrir los casos reales del Figma y cada pantalla termina reintroduciendo estilos ad-hoc alrededor de ellos (señal de que faltó un átomo en el set mínimo, o de que se necesita una librería).
- Costo estimado de revertir: bajo — los tokens son variables CSS/SCSS aisladas y los componentes compartidos son *standalone*, sin acoplar el resto del código (dominio/aplicación intactos); revertir implica dejar de consumirlos pantalla por pantalla, no una migración estructural.
- Plan B si se revierte: adoptar la Alternativa D (tokens propios + una librería puntual para el componente complejo que motive la reversión), sin descartar los tokens ya construidos.

### 7. Validación

- `ng build` en verde tras cada Design Doc de UI que consuma `shared/ui/` (mismo gate que el resto del proyecto).
- Verificación visual manual contra las capturas del Figma (`PdeTWx28MJYxcQ0hrXhhRZ`) por pantalla migrada — no hay *visual regression testing* automatizado en este proyecto (fuera de alcance, un solo desarrollador).
- Métrica de adopción: número de pantallas migradas a `shared/ui/` vs. total de features existentes, reportado en `docs/product/DTP.md` conforme cada Design Doc de UI se ejecute.

### 8. Referencias

- Figma "Prototipo Administrativo" — `https://www.figma.com/design/PdeTWx28MJYxcQ0hrXhhRZ/EduSync` (nodo de referencia inicial: `399:20447`, "Iniciar Sesión").
- `docs/design/DD-UC-001.md` §3 (criterio de "sin sobre-ingeniería sin caso de uso confirmado", aplicado aquí por analogía).
- `docs/design/DD-UC-006.md` §2 (decisión original de "sin sistema de diseño", que este ADR revisa).
- `docs/design/DD-UC-020.md` (primer Design Doc que aplica esta decisión — login).

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| 1 | 13/09/2026 | Rodrigo Aspeti | Propuesta y aceptación en el mismo turno: adopción de tokens visuales propios + componentes compartidos mínimos (`shared/ui/`), sin librería de terceros, a partir del prototipo Figma "Prototipo Administrativo". Habilita `DD-UC-020` (rediseño visual del login). |
