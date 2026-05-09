# Business Requirements Document (BRD) – EduSync

#### 0. Metadatos
| Campo | Valor |
| ------ | ------ |
| Producto | EduSync |
| Grupo | G013 |
| Versión | v1.0 |
| Fecha | 09/05/2026 |
| Sponsor de negocio | Dirección Institucional / Propietarios de Unidades Educativas |
| Stakeholders | Docentes, Asesores de Curso, Secretarías, Ministerio de Educación (SIE) |
| Autores | Equipo de Consultoría Estratégica y Negocio |
| Revisores | Docente + 1 grupo par |
| Estado | Aprobado |

#### 1. Resumen ejecutivo
**Problema:** La gestión de calificaciones en colegios depende de una "triple digitación" manual ("copiar y pegar") mediante hojas de cálculo fragmentadas, lo que genera errores de desajuste de alumnos, pérdida de integridad de datos y obliga al personal a trabajar en horarios de madrugada (2:00 AM) para cargar información al colapsado sistema estatal (SIE). 
**Propuesta:** EduSync es una plataforma SaaS B2B multitenant con validación antierrores en tiempo real que descentraliza el ingreso de notas y automatiza la consolidación y sincronización masiva con el SIE.
**Valor esperado:** Reducción del ciclo administrativo de cierre de días a minutos, eliminación total de auditorías humanas redundantes y erradicación de multas o sanciones laborales por inconsistencias.
**Métricas clave de éxito:** Tasa de error del 0% en la consolidación de datos y reducción del *Time-on-Task* de sincronización de madrugadas enteras a solo clics.
**Llamada a la acción:** Aprobación del *Business Case* para iniciar el desarrollo del módulo de "Sincronización SIE por Código RUDE" y despliegue del sistema de diseño validado.

#### 2. Contexto del negocio
*   **Organización:** Unidades Educativas Privadas y de Convenio (Mercado Boliviano).
*   **Unidad impactada:** Dirección Académica, Secretaría / Administración y Plantel Docente.
*   **Proceso(s) de negocio afectado(s)**: Cierre de operativo trimestral, registro pedagógico de aula, consolidación de centralizadores y reporte al Sistema de Información Educativa (SIE).
*   **Estrategia de la organización:** Digitalización integral *Zero-Training*, garantizando la seguridad de datos inmutables y el cumplimiento regulatorio estricto con el Estado.

#### 3. Problema y oportunidad de negocio
##### 3.1 Problema
Actualmente, las instituciones educativas sufren de una grave ineficiencia debido al proceso manual de consolidar calificaciones de múltiples profesores en un solo centralizador de Excel. Este proceso provoca desfases de alumnos, corrupciones de datos e inconsistencias matemáticas por decimales invisibles. Adicionalmente, existe un cuello de botella crítico: la transcripción obligatoria, nota por nota, hacia el sistema gubernamental SIE. Para evadir la saturación de los servidores del Estado, las secretarias y asesores se ven forzados a trabajar de madrugada, exponiéndose a un altísimo estrés, auditorías redundantes y miedo a sanciones laborales. 

##### 3.2 Oportunidad
*  **Valor económico estimado:** Ahorro masivo en horas extras del personal administrativo, eliminación de multas pecuniarias por parte del Ministerio por envío de datos corruptos.
*  **Valor estratégico:** EduSync se posiciona como el estándar de "Tranquilidad Administrativa", logrando un "Océano Azul" en usabilidad frente a sistemas legados obsoletos.
*  **Ventana de oportunidad:** Implementación preferencial antes de la temporada de inscripciones y el primer cierre de operativo trimestral.

#### 4. Usuarios objetivo / Personas clave
##### 4.1 Persona principal
| Atributo | Valor |
| ------ | ------ |
| Nombre / rol | Docente / Asesor de Curso |
| Contexto | Cierre de trimestre, bajo presión, lidiando con múltiples hojas Excel. |
| *Jobs‑to‑be‑done* | 1. Ingresar notas por dimensiones con validación. 2. Registrar asistencia. 3. Ejecutar "cierre operativo" de su materia. |
| Dolores principales | Carga administrativa por auditar y consolidar notas de colegas, miedo a descuentos salariales por errores tipográficos en el SIE. |
| Ganancia esperada | Registrar de forma autónoma solo su materia, liberándose del proceso de transcripción general y carga al Ministerio. |

##### 4.2 Persona secundaria
| Atributo | Valor |
| ------ | ------ |
| Nombre / rol | Secretaría / Administrativo |
| Contexto | Madrugadas previas al plazo límite ministerial para exportar datos del colegio al SIE. |
| *Jobs‑to‑be‑done* | 1. Monitorizar progreso. 2. Generar boletines en PDF. 3. Sincronizar masivamente con el SIE. |
| Dolores principales | Trabajo de madrugada (2:00 AM), estrés por "salvar" el colegio ante errores de docentes rezagados, alteración clandestina de notas pasadas. |
| Ganancia esperada | Obtener una Única Fuente de Verdad automatizada, sincronizando al SIE sin digitar manualmente y recuperando su vida personal (fines de semana). |

#### 5. Propuesta de valor
| Eje | Contenido |
| ------ | ------ |
| **Para quién** | Colegios B2B (Directores, Secretarías, Docentes). |
| **Que necesita** | Cumplir con la obligación estatal de reportar calificaciones libres de errores, sin paralizar la operatividad del colegio. |
| **Nuestra propuesta es** | EduSync: SaaS multitenant de gestión académica y sincronización automática. |
| **Que le aporta** | Cero consolidación en Excel, validación normativa proactiva que bloquea errores en tiempo real y exportación automatizada al formato SIE. |
| **A diferencia de** | Procesos legados en Excel y sistemas como Academium o Colegio360. |
| **Nuestro diferencial es** | Experiencia "Zero-Training", automatización del cuello de botella gubernamental e inmutabilidad de datos post-cierre. |

#### 6. Panorama competitivo (resumen)
| Competidor / alternativa | Tipo | Fortaleza percibida | Debilidad percibida |
| ------ | ------ | ------ | ------ |
| **Excel + Transcripción Manual** | *do‑nothing* | Familiaridad absoluta y costo monetario cero. | Fragmentación, desfases visuales, 10 ciclos de auditoría manual requerida. |
| **Sistema SIE (Ministerio)** | Directo/Obligatorio | Es el estándar gubernamental obligatorio. | Sin opciones de "copiar/pegar", colapsa en horario laboral, UX hostil. |
| **Academium** | Directo | Costo accesible. | Usabilidad deficiente, traslada la carga cognitiva y estrés al docente. |
| **Colegio360** | Directo | Robusto en funciones. | Soporte técnico deficiente, alto costo y baja hiper-localización regional. |

#### 7. Business Model Canvas
| Bloque | Mínimo 3 elementos concretos |
| ------ | ------ |
| **1. Segmentos de clientes** | 1. Directores/Compradores B2B. <br> 2. Secretarías (operadores/consolidadores). <br> 3. Docentes (generadores de datos). |
| **2. Propuesta de valor** | 1. Cero digitación manual en la sincronización SIE. <br> 2. Tranquilidad legal ante inmutabilidad de registros. <br> 3. Interfaz antierrores en tiempo real. |
| **3. Canales** | 1. Venta Directa B2B. <br> 2. Marketing Digital vía TikTok. <br> 3. Demostraciones en Congresos Educativos. |
| **4. Relación con clientes** | 1. Certificaciones "Colegio Digital". <br> 2. Módulos de Valor Incremental (Upselling). <br> 3. Benchmark de Rendimiento anónimo entre colegios. |
| **5. Fuentes de ingresos** | 1. Setup Fee inicial (200 Bs). <br> 2. Costo anual por estudiante (3 cuotas o desc. 10% anual). <br> 3. Cobros extra por módulos adicionales futuros. |
| **6. Recursos clave** | 1. Motor algorítmico de mapeo RUDE. <br> 2. Infraestructura Cloud y DB inmutable. <br> 3. Equipo de Diseño UX/UI y Desarrollo. |
| **7. Actividades clave** | 1. Mantenimiento del Sistema de Diseño. <br> 2. Mapeo e integración constante del formato SIE. <br> 3. Actualización de normativas ministeriales. |
| **8. Socios clave** | 1. Sistema de Información Educativa (SIE). <br> 2. Federación de Administrativos de Cochabamba. <br> 3. Proveedores Cloud (AWS/Google). |
| **9. Estructura de costos** | 1. Infraestructura Cloud y Servidores. <br> 2. Planilla del talento (Desarrollo, UX, Soporte). <br> 3. Costos de Adquisición de Clientes (Marketing). |

#### 8. Métricas clave de éxito (North Star + apoyo)
| ID | KPI | North Star? | Línea base | Meta | Horizonte | Fuente del dato |
| ------ | ------ | ------ | ------ | ------ | ------ | ------ |
| KPI-01 | Time-on-Task Sincronización SIE | sí | 15+ horas/madrugada | < 10 min | Q1 Lanzamiento | Telemetría del Sistema |
| KPI-02 | Tasa de Error (Integridad de Datos) | no | Alta (desfases) | 0% | Q1 Lanzamiento | Reportes de Auditoría |
| KPI-03 | Ciclos de Revisión Manual | no | 10 ciclos | 0 ciclos | Q1 Lanzamiento | Entrevistas UX a Secretarías |

#### 9. Objetivos de negocio (SMART)
| ID | Objetivo | Métrica | Línea base | Meta | Horizonte |
| ------ | ------ | ------ | ------ | ------ | ------ |
| BO-01 | Reducir tiempo del cierre operativo trimestral. | Tiempo (minutos) | Días/Horas | ≤ 10 min | Cierre 1er Trimestre |
| BO-02 | Erradicar la corrupción de datos por desfases de lista. | Tasa de error de mapeo | N/D | 0% | Cierre 1er Trimestre |
| BO-03 | Incrementar la adopción del registro directo por docentes. | % de carga en sistema | 0% | ≥ 95% | Año Escolar 1 |

#### 10. Stakeholders y roles (modelo RACI)
| Stakeholder | Interés | R / A / C / I |
| ------ | ------ | ------ |
| Director de la Institución | Estratégico y Cumplimiento | A |
| Secretaría / Asesores | Operativo (Sincronización SIE) | R |
| Docentes de Materia | Experiencia y Generación | C |
| Ministerio de Educación | Cumplimiento Normativo | I |

#### 11. Requerimientos de negocio
| ID | Requerimiento de negocio | Prioridad (MoSCoW) | Justificación | Métrica de aceptación |
| ------ | ------ | ------ | ------ | ------ |
| BR-001 | Carga de notas descentralizada con RBAC. | Must | Aísla la responsabilidad operativa al docente. | 100% de accesos restringidos a materias propias. |
| BR-002 | Validación antierrores y paramétrica en tiempo real. | Must | Evita notas inválidas (fuera de rango) desde el tipeo. | 0% de notas fuera de norma almacenadas. |
| BR-003 | Consolidación y estandarización algorítmica. | Must | Previene decimales invisibles y descuadres matemáticos. | Coincidencia matemática exacta en boletines. |
| BR-004 | Exportación y sincronización masiva al SIE por RUDE. | Must | Es el cuello de botella que causa el trabajo de madrugadas. | Carga exitosa al SIE sin tipeo manual. |
| BR-005 | Congelamiento temporal post-cierre (Inmutabilidad). | Must | Bloquea alteraciones clandestinas retroactivas. | 0 cambios sin "Log de Auditoría" en periodos cerrados. |

#### 12. Reglas de negocio y políticas
| ID | Regla | Tipo | Origen |
| ------ | ------ | ------ | ------ |
| RB-01 | **Mapeo por Identidad Única (RUDE):** La vinculación de notas debe realizarse exclusivamente por RUDE, nunca por nombre u orden visual. | Política Interna | Diseño Antierrores |
| RB-02 | **Límites de Dimensión:** Ninguna calificación puede guardarse si supera los máximos normativos (Ej: Ser/Decidir, Saber/Hacer). | Normativa | Ministerio de Educación |
| RB-03 | **Inmutabilidad de Nóminas:** Los docentes tienen prohibido alterar, agregar o eliminar alumnos de las listas de evaluación. | Política Interna | Seguridad de Datos |

#### 13. Supuestos, restricciones y dependencias
*   **Supuestos**: La experiencia guiada UI/UX vencerá la resistencia natural al cambio en docentes con baja alfabetización digital sin necesidad de capacitaciones costosas ("Zero-Training").
*   **Restricciones**: El mapeo de datos al formato del Estado es inquebrantable; el código RUDE es de uso obligatorio para la identificación.
*   **Dependencias**: La exportación final del cierre operativo depende críticamente de la disponibilidad de red y *uptime* de los servidores del Ministerio de Educación (SIE).

#### 14. Alcance de negocio
##### 14.1 En alcance
*  Gestión descentralizada de calificaciones y asistencia.
*  Consolidación de centralizadores internos y reportes PDF.
*  Integración/Sincronización automatizada hacia el sistema SIE.
*  Log inmutable de auditoría y bloqueos de periodos.
##### 14.2 Fuera de alcance
*  Gestión financiera, facturación o cobro de pensiones escolares.
*  Módulos de comunicación externa y chat con padres de familia (previsto para Upselling a futuro).

#### 15. Beneficios esperados y *business case* resumido
| Tipo | Año 1 | Año 2 | Año 3 |
| ------ | ------ | ------ | ------ |
| Ahorro operativo (H-Hombre Secretarías) | Estimado 15% | Estimado 25% | Estimado 35% |
| Ingresos adicionales (Nuevos Colegios) | +20 colegios | +50 colegios | +100 colegios |
| Inversión (CAPEX / Desarrollo UX) | $X,XXX | - | - |
| Costo operación (OPEX Cloud + Soporte)| $X,XXX | $X,XXX | $X,XXX |

#### 16. Riesgos de negocio
| Riesgo | Probabilidad | Impacto | Mitigación | Responsable |
| ------ | ------ | ------ | ------ | ------ |
| Caída de los servidores del SIE durante la exportación | Alta | Alta | Botón de reintentos asíncronos y guardado de estado | PM / Dev Lead |
| Cambios sorpresivos en los formatos/dimensiones del Ministerio | Media | Alta | Arquitectura paramétrica de la base de datos | PM |
| Resistencia docente al ingreso de notas web | Baja | Media | Interfaz "Zero-Training" amigable y responsiva | UX Design |

#### 17. Criterios de éxito del proyecto de negocio
*  Reducción demostrable de los 10 ciclos de auditoría manual a una simple verificación visual.
*  Desaparición del trabajo administrativo en jornadas de madrugada (2:00 AM - 4:00 AM).
*  Cumplimiento del 100% en la exportación de información libre de errores de "copiar y pegar" al SIE.
*  *Business case* y rentabilidad positiva en el primer ciclo anual.

#### 18. Trazabilidad a documentos hijos
| BRD ID | MRD relacionado | PRD relacionado | Caso de uso FSD |
| ------ | ------ | ------ | ------ |
| BR-004 | TBD | TBD | TBD (Sincronización SIE) |
| BR-002 | TBD | TBD | TBD (Validación Antierrores) |

#### 19. Aprobaciones
| Rol | Nombre | Firma | Fecha |
| ------ | ------ | ------ | ------ |
| Sponsor | Dirección Académica | | 09/05/2026 |
| PM | Product Manager | | 09/05/2026 |
| Arquitecto/UX | Lead Designer | | 09/05/2026 |

#### 20. Registro de cambios
| Versión | Fecha | Autor | Cambio |
| ------ | ------ | ------ | ------ |
| v1.0 | 09/05/2026 | Consultor Estratégico | Creación inicial y consolidación del BMC. |
