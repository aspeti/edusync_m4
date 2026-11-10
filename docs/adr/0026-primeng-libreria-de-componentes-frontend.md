# Architecture Decision Record (ADR)

## ADR-0026: PrimeNG como única librería de componentes del frontend (incluida la grilla de notas y los centralizadores)

### Metadatos

| Campo | Valor |
|-------|-------|
| Numero | `0026` |
| Titulo | PrimeNG (línea 21.x) como única librería de UI de terceros; `p-table` para la grilla de registro de notas y los centralizadores |
| Fecha | 10/11/2026 |
| Autor(es) | G-EduSync |
| Estado | **Aceptada** |
| Alcance | Frontend Angular (`frontend/`). No afecta al backend, a RBAC, a contratos REST ni al modelo de dominio. No toca `docs/baseline/**`. **Modifica** `ADR-0015` (Alternativa C → PrimeNG sobre los tokens propios) y lo declara, en parte, superado; respeta `ADR-0008` (Angular 21 LTS). |
| ADR relacionado | `ADR-0015` (design system propio, que este ADR enmienda), `ADR-0008` (stack vivo), `ADR-0025` (el centralizador queda diferido), `DD-UC-018` (matriz de calificaciones actual) |

### 1. Contexto

`ADR-0015` (13/09/2026) eligió tokens visuales propios y tres componentes hechos a mano (`card`, `form-field`, `button`), sin librería de terceros. Rechazó PrimeNG y Angular Material porque el Figma tenía un lenguaje de marca propio y porque "ningún Design Doc actual requería un componente que no se pudiera construir a mano". Dejó abierta la puerta (Alternativa D, §4.2 y §6): si aparecía un componente complejo, se evaluaría una librería.

Esa condición ya se cumple:

- La **matriz de registro de notas** (`evaluacion-calificaciones.page.ts`, `DD-UC-018`) es hoy una `<table>` con un `<input type="number">` por fila, estilos en línea y un botón "Guardar" para todo el lote. No tiene navegación con teclado entre celdas, validación por celda ni columnas fijas. Es la pantalla de mayor uso del producto.
- Los **centralizadores** (`FSD-UC-003`, hoy reducido por `ADR-0025` a la nota final de periodo, con la tabla `PROVISIONAL`/`OFICIAL` diferida) serán una tabla ancha de estudiantes × materias × periodos, con columnas fijas, orden, filtro y exportación.
- El resto de la UI (`features/plataforma`, `usuarios`, `academico`) sigue con estilos en línea y tablas HTML. `ADR-0015` §4.2 ya anticipó que `shared/ui/` no tiene comunidad que corrija bugs de accesibilidad.

El responsable del producto decidió (10/11/2026) adoptar **PrimeNG como única librería** para los componentes de UI. En una primera versión de este ADR se había propuesto además AG Grid Community para las grillas; esa parte **se descartó** el mismo día. Las grillas de notas y de centralizadores se resuelven con el componente de tabla de PrimeNG.

Hechos verificados contra el registro npm el 10/11/2026:

| Paquete | Última versión | Compatibilidad |
|---------|----------------|----------------|
| `primeng` | 22.1.4 | **Exige Angular 22** (`@angular/core ^22.1.0`). **No es utilizable**: `ADR-0008` fijó Angular 21 LTS |
| `primeng` línea 21.x | 21.1.10 | `@angular/core ^21.0.7`, `@angular/cdk ^21.0.0`. Es la línea a usar |
| `@primeuix/themes` | 3.1.0 | Motor de temas de PrimeNG (presets, `definePreset`). Compatibilidad con la 21.x a verificar al instalar |

Licencia: PrimeNG publica sus versiones *community* bajo MIT; sus plantillas y bloques de pago son productos aparte y no se usan.

### 2. Alternativas consideradas

| Alternativa | Pros | Contras |
|-------------|------|---------|
| A. Mantener `ADR-0015` tal cual (tabla HTML + `shared/ui/`) | Cero dependencias | La matriz de notas sigue sin teclado ni validación por celda. Cada componente complejo se construye a mano. Contradice la decisión del responsable |
| B. **Solo PrimeNG** (`p-table` con edición en celda para notas y centralizadores) | Una sola dependencia nueva y un solo lenguaje de componentes, tematizable con los tokens de `ADR-0015`. Sin segundo mapeo de tema. Menos bundle y menos superficie a mantener | `p-table` es menos potente que una grilla dedicada para edición masiva: navegación con teclado entre celdas más limitada y virtualización de muchas filas con edición a validar al implementar |
| C. PrimeNG + AG Grid Community | Grilla de edición masiva más completa | Segunda dependencia y segundo mapeo de tema. **Descartada por el responsable** |
| D. Angular Material | Estándar de Angular | Ya descartado en `ADR-0015`. El responsable eligió PrimeNG |

### 3. Decisión

Alternativa B.

**3.1 PrimeNG es la única librería de UI de terceros.** Se usa para formularios (input, select, multiselect, checkbox, date picker), diálogos, toasts, badges/tags, paginador y tablas. La matriz de registro de notas (reemplaza la `<table>` de `evaluacion-calificaciones.page.ts`) y los futuros centralizadores se construyen con `p-table`, usando sus capacidades de edición en celda, columnas fijas (*frozen*), orden, filtro, desplazamiento virtual y exportación a CSV. **No** se agrega AG Grid ni otra librería de grillas.

**3.2 Versiones.** `primeng` en la línea **21.x** (`^21.1`), `@angular/cdk` `^21` y `@primeuix/themes` en la versión compatible. **No** se usa `primeng` 22.x mientras el stack siga en Angular 21 (`ADR-0008`). Las versiones exactas se fijan al ejecutar el prompt de implementación, tras comprobar que `ng build` pasa en Angular 21.2. Solo la edición gratuita: **no** se usan plantillas ni bloques de pago.

**3.3 Relación con `ADR-0015`.** Los tokens de `frontend/src/styles/_tokens.scss` **siguen siendo la fuente de verdad visual**. PrimeNG se tematiza con un *preset* propio (`definePreset`) que mapea los tokens de marca (azul marino, radios, sombra, tipografía), de modo que la librería muestra el lenguaje visual del Figma sin estilos ad-hoc por pantalla. Los tres componentes de `shared/ui/` (`card`, `form-field`, `button`) se conservan mientras `login.page.ts` (`DD-UC-020`) los consuma y se reemplazan por equivalentes PrimeNG pantalla a pantalla, en el Design Doc que migre cada una.

**3.4 Migración incremental.** No hay migración masiva. Toda pantalla **nueva** usa PrimeNG, y la matriz de notas migra a `p-table` en su propio Design Doc. Las pantallas existentes se migran una a una, cada una con su Design Doc de mejora visual, para no mezclar riesgo de regresión funcional con cambio de estilos (criterio ya fijado en `ADR-0015` §4.2).

**3.5 Invariantes que la tabla de notas no puede romper.**

- **Ningún cálculo en el frontend.** La tabla de notas solo captura y muestra. Promedios, redondeo y nota de periodo viven únicamente en `CalculoNotas` (backend). Una columna de "nota provisional" se rellena con la respuesta de `GET .../nota-provisional`, nunca con una fórmula de la tabla.
- **La validación de rango en la celda es solo ayuda de UX.** La autoridad sigue siendo el backend (`E_RANGO_INVALIDO`, `E_PERIODO_NO_ABIERTO`). Una celda marcada válida en el cliente puede ser rechazada por el servidor, y la UI debe mostrar ese error.
- **Identificación por `estudianteId`.** La fila se identifica por `estudianteId` (como hoy). El `rude` se muestra, pero no se usa como clave de fila ni se escribe en consola, logs, analítica ni mensajes de error.
- **RBAC y tenant sin cambios.** Las rutas, guards y endpoints siguen igual. El cambio de librería no amplía el acceso a ningún dato.
- **El contrato del lote se mantiene.** El guardado sigue siendo `PUT /evaluaciones/{id}/calificaciones` con `items[]`. La tabla solo envía las celdas modificadas.

### 4. Consecuencias

**Positivas.** Una sola dependencia de UI, un solo mapeo de tema contra los tokens de `ADR-0015`, y componentes de formulario accesibles y mantenidos por una comunidad. La tabla de notas gana edición por celda y validación visual sin construirlas a mano.

**Negativas / riesgos aceptados.**

- **Techo de `p-table` para edición masiva.** Si en la práctica la matriz de notas resultara lenta con cursos grandes, o la navegación con teclado entre celdas se quedara corta para el docente, habría que revisitar la decisión. Se registra como señal de reversión (§6). Se valida con un curso de tamaño realista en el Design Doc de la matriz, antes de dar la migración por cerrada.
- **No hay exportación a Excel ni pegado masivo de rangos** de serie. PrimeNG exporta a CSV; cualquier otra cosa necesitaría una librería adicional y un ADR nuevo.
- **Acoplamiento a versiones.** `primeng` está atado a la versión mayor de Angular (22.x ya exige Angular 22). Subir de Angular 21 a 22 obligaría a subir PrimeNG a 22 a la vez. Queda como condición de la futura decisión de subir de LTS.
- Mientras no estén migradas todas las pantallas conviven tres estilos: el ad-hoc heredado, `shared/ui/` y PrimeNG.
- **Bundle.** PrimeNG añade peso. Se mitiga importando solo los componentes usados (sus componentes son *standalone*) y con las rutas ya cargadas de forma diferida.
- Los tests de frontend siguen sin poder ejecutarse en este entorno (Vitest sin navegador, documentado desde `DD-UC-004`). La verificación es `ng build` más prueba manual, hasta que se resuelva esa limitación.

### 5. Impacto en otros documentos

| Documento | Cambio |
|-----------|--------|
| `docs/adr/0015-adopcion-design-system-visual-edusync.md` | Nota de modificación apuntando a este ADR (el cuerpo histórico no se reescribe) |
| `AGENTS.md` | §4: fila de PrimeNG + `@angular/cdk` + `@primeuix/themes`, marcada "aún no está en el `package.json`" hasta ejecutar el prompt |
| `docs/product/DTP.md` | §A.1 fila, §A.2 delta 14 (UI de `ADR-0015` → PrimeNG), §21 ADRs vigentes |
| `docs/design/DD-UC-032.md`, `docs/prompts/impl/PR-IMPL-032.md`, `prompts/PR-ADR-013.md` | Diseño y prompt de la base de PrimeNG + migración de la matriz; prompt del ADR. Ejecución pendiente de este ADR *Aceptada* |

### 6. Reversión

Costo bajo antes de ejecutar el prompt de implementación (solo documentos). Después, PrimeNG se importa por pantalla y los tokens siguen vivos en `_tokens.scss`: revertir implica volver pantalla por pantalla a la tabla HTML y a `shared/ui/`, sin tocar backend ni dominio. Señales tempranas de que fue un error: el preset no logra reproducir el Figma sin estilos ad-hoc, o `p-table` no da una experiencia aceptable de captura de notas con un curso de tamaño real. En el segundo caso, el plan B es añadir una librería de grilla con un ADR propio, sin descartar PrimeNG para el resto.

### 7. Validación

- `ng build` en verde con las dependencias nuevas.
- La matriz de notas conserva el contrato `PUT /evaluaciones/{id}/calificaciones` y los códigos de error `E_RANGO_INVALIDO` y `E_PERIODO_NO_ABIERTO` visibles al usuario.
- Revisión manual con un curso de tamaño realista: captura de notas con teclado, celda fuera de rango marcada, errores del servidor mostrados, ningún `rude` ni nota en la consola del navegador.
- Verificación visual contra el Figma de `ADR-0015` en cada pantalla migrada.
- `mvn test` no cambia (sin cambios de backend).

### 8. Referencias

`ADR-0008`, `ADR-0015`, `ADR-0025`, `DD-UC-018`, `DD-UC-020`, `FSD-UC-003`, `FSD-UC-016`, `BR-004`, `NFR-007`.

### 9. Historial

| Versión | Fecha | Autor | Cambio |
|---------|-------|-------|--------|
| v0.1 | 10/11/2026 | G-EduSync | Propuesta inicial: PrimeNG + AG Grid Community |
| v0.2 | 10/11/2026 | G-EduSync | Se descarta AG Grid Community por decisión del responsable: PrimeNG es la única librería de UI, y la grilla de notas y los centralizadores usan `p-table`. Se renombra el archivo y el título. Alternativa B elegida |
| v1.0 | 10/11/2026 | G-EduSync | Aceptada. Habilita la ejecución de `PR-IMPL-032` (`DD-UC-032`) |
