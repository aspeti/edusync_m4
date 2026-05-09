Rol: Actúa como un Arquitecto de Software y Analista de Sistemas Senior.

Contexto: El usuario está desarrollando EduSync, una plataforma SaaS multitenant para unidades educativas de Bolivia, enfocada en eliminar errores de consolidación manual y acelerar el cierre académico con trazabilidad y cumplimiento normativo ante el Ministerio de Educación (SIE).

## 1) Visión General del Producto
**Nombre:** EduSync.

**Problema central:**  
La gestión académica depende de hojas de cálculo y procesos manuales de triple digitación, lo que provoca desfases de listas, errores de decimales, alteraciones retroactivas no trazadas y sobrecarga operativa (incluyendo jornadas de madrugada para carga al SIE).

**Objetivo principal:**  
Convertir a EduSync en la **fuente única de verdad académica**, reduciendo el cierre administrativo de días/madrugadas a minutos y garantizando integridad, auditabilidad e inmutabilidad de datos.

## 2) Perfiles de Usuario y Capacidades
### Docente (Usuario Operativo)
- Registrar calificaciones por dimensión (Ser, Saber, Hacer, Decidir) con validación de rangos.
- Controlar asistencia en su materia.
- Ejecutar cierre operativo de su área para liberar consolidación institucional.

### Secretaría / Administrativo (Usuario Gestor)
- Monitorear avance de carga docente por curso y periodo.
- Consolidar automáticamente centralizadores y generar boletines oficiales.
- Sincronizar/exportar al SIE por RUDE con trazabilidad de resultado.

### Director (Usuario Estratégico)
- Supervisar el estado global de carga y cierre académico.
- Revisar indicadores institucionales (rendimiento, reprobación, cumplimiento).
- Autorizar/rechazar cambios retroactivos en periodos cerrados bajo control jerárquico.

## 3) Requerimientos Funcionales Críticos
1. **Ingreso descentralizado con RBAC estricto:** cada docente solo opera su materia/curso asignado.
2. **Validación en tiempo real:** bloqueo de valores fuera de norma y prevención de errores de captura.
3. **Consolidación automática:** cálculo inmediato de centralizadores trimestrales/anuales.
4. **Gestión inmutable de nóminas y periodos:** listas protegidas y periodos cerrados en Solo Lectura.
5. **Auditoría completa:** registro de quién, cuándo, qué cambió y valor anterior/nuevo.
6. **Exportación/Sincronización SIE:** mapeo por RUDE (nunca por posición), manejo de altas/bajas y confirmación de resultado.
7. **Reportería oficial:** boletines, reportes estadísticos y salidas listas para trámite estatal.

## 4) Reglas de Negocio (Constraints)
1. **Mapeo por identidad única (RUDE):** obligatorio para toda exportación al SIE.
2. **Rangos de calificación por dimensión:** validación paramétrica estricta.
3. **Estandarización decimal:** redondeo/truncado uniforme para evitar descuadres.
4. **Inmutabilidad por cierre:** periodo cerrado = Solo Lectura; cambios solo por flujo autorizado.
5. **Integridad institucional:** docentes no pueden alterar nóminas ni notas fuera de su alcance.

## 5) Escenario Crítico (Parte más difícil)
**Proceso:** Sincronización automática al SIE con cierre operativo.

**Flujo esperado:**
1. Secretaría selecciona curso y periodo con estado “completo”.
2. EduSync valida estructura, rangos y consistencia por estudiante.
3. Se exportan notas asociadas por RUDE (incluye alumnos nuevos, excluye retirados).
4. El sistema registra evidencia de sincronización (resultado, timestamp, actor).
5. Se emite boletín oficial PDF y el periodo queda bloqueado en Solo Lectura.

## 6) Criterios de Éxito (KPIs)
- Reducción del tiempo de cierre y carga ministerial: de jornadas extensas a minutos.
- 0% de errores de transcripción manual.
- Eliminación de ciclos redundantes de revisión humana.
- Disminución de incidencias/sanciones por inconsistencias académicas.
- Trazabilidad completa en auditorías internas y externas.

## 7) Instrucción para el asistente
Con base en esta visión:
- Diseña arquitectura funcional y técnica priorizando seguridad, trazabilidad e integración SIE.
- Propón modelo de datos, flujos de cierre, reglas de validación y estrategia de auditoría.
- Define casos límite (transferidos, retirados, cambios retroactivos, reconexión SIE).
- Sugiere pruebas funcionales y no funcionales para garantizar confiabilidad operativa.