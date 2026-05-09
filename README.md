# EduSync - Plataforma de Gestión de Calificaciones

EduSync es una plataforma SaaS B2B multitenant diseñada para revolucionar la gestión, centralización y sincronización de calificaciones en instituciones educativas. Su objetivo principal es erradicar la ineficiencia, la vulnerabilidad de los datos y el caos generado por el uso de hojas de cálculo fragmentadas.

## 🚀 El Problema que Resuelve
El proceso educativo actual sufre de un grave cuello de botella debido a la "triple digitación" manual de datos. Esto desencadena fallas críticas como:
* **Corrupción de datos por "Copiar y Pegar":** La consolidación manual provoca que el orden de las listas se desfase (por alumnos nuevos o retirados), asignando notas a estudiantes incorrectos.
* **El Cuello de Botella del SIE:** La transcripción obligatoria y manual al Sistema de Información Educativa (SIE) del Estado exige que secretarias y asesores trabajen en horarios de madrugada (2:00 AM - 4:00 AM) para evitar la saturación de los servidores.
* **Inconsistencias y Alteraciones Ocultas:** El uso de formatos libres arrastra decimales invisibles que generan reprobaciones injustas, además de permitir la modificación retroactiva clandestina de notas en periodos ya cerrados.

## 🎯 Perfiles de Usuario
La arquitectura de EduSync está diseñada en torno a tres actores clave:
1. **Docente (Generador de Datos):** Ingresa las calificaciones de forma autónoma y descentralizada únicamente para su área, librándose de consolidar notas de terceros y del estrés por errores tipográficos.
2. **Secretaría (Consolidador):** Monitoriza la entrega en tiempo real, genera libretas automáticas y sincroniza la información al sistema ministerial SIE sin transcribir manualmente, devolviéndole su tiempo personal.
3. **Director (Nivel Estratégico):** Obtiene visibilidad en tiempo real para la toma de decisiones, accediendo a reportes estadísticos (índices de reprobación, cuadros de honor) y autorizando de forma jerárquica cualquier modificación excepcional.

## ⚙️ Capacidades Core y Reglas de Negocio
* **Validación Proactiva (Zero-Training):** La interfaz detecta y bloquea anomalías en tiempo real, impidiendo que el docente ingrese calificaciones fuera de los rangos normativos (ej. valores mayores a 35 o 100).
* **Single Source of Truth (Identidad Única):** La exportación y consolidación de datos mapea incondicionalmente a cada estudiante mediante su código RUDE, eliminando el riesgo de desfases visuales en listas.
* **Congelamiento Temporal (Inmutabilidad):** Una vez concluido un periodo académico y realizado el cierre operativo, las celdas pasan a estado de "Solo Lectura" (Read-Only), bloqueando modificaciones retroactivas.
* **Sincronización SIE "One-Click":** Módulo de exportación directa que formatea masivamente las sábanas de calificaciones hacia los requerimientos del Ministerio de Educación, suprimiendo por completo la necesidad de digitación nocturna.
* **Log de Auditoría:** Trazabilidad inalterable que detalla quién, cuándo y qué calificación fue modificada.

## 🎨 Sistema de Diseño y UX
EduSync está construido bajo los principios de **Atomic Design** y el uso de **Design Tokens** (semánticos y primitivos) documentados en Figma, asegurando una plataforma escalable y coherente. 
* **Accesibilidad y Baja Carga Cognitiva:** Cumplimiento de estándares WCAG 2.2 con uso de tipografía *Inter* y semáforos visuales (alertas en rojo para notas de reprobación menores a 51) para un escaneo ágil.
* **Microinteracciones y Prevención de Errores:** Uso de variables interactivas y Smart Animate para proveer feedback inmediato sin recargar páginas, guiando el modelo mental del usuario paso a paso.

## 📂 Estructura del Repositorio
La documentación clave del negocio y producto se encuentra en el directorio `docs/`:
* `01_vision_negocio.MD`: Definición estratégica, propuesta de valor y problemas que resuelve la plataforma.
* `02_parte_dificil.MD`: Detalle técnico y de negocio sobre la funcionalidad más compleja (Sincronización automatizada y mapeo por RUDE con el SIE).
* `03_Prompt.MD`: Recursos de prompts e integración.
