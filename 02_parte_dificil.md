**NOMBRE DEL SISTEMA:** EduSync

**PARTE DEL SISTEMA QUE CONSIDERAMOS MÁS DIFÍCIL:** Sincronización Automática, Mapeo Estricto y Cierre Operativo con el Sistema SIE.

**¿POR QUÉ ES DIFÍCIL?**
Esta es la parte que más impacta al negocio si sale mal, ya que exige interactuar con una plataforma gubernamental (el SIE) que se satura con facilidad y no permite "copiar y pegar" datos. Un error en la exportación, como un desajuste causado por estudiantes retirados o nuevos, corrompe la información oficial y resulta directamente en estrés extremo, procesos administrativos, memorándums y fuertes descuentos económicos (de hasta 5 días de sueldo) para el personal.

**ESCENARIO CONCRETO:**
- **Actor:** Secretaría / Administrativo (Consolidador de datos).
- **Punto de partida (datos o situación):** Todos los docentes han finalizado el ingreso de sus calificaciones trimestrales en EduSync. El centralizador interno del curso está completo y validado, pero las notas aún deben enviarse al Ministerio de Educación antes de que colapsen los servidores.
- **Pasos que ocurren:**
  1. El usuario administrativo ingresa al módulo de "Sincronización SIE", selecciona el curso a reportar y verifica el estado de notas completas.
  2. Al presionar "Exportar al SIE", el sistema procesa toda la sábana de notas y mapea incondicionalmente cada calificación con el estudiante correcto vinculándolo mediante su código RUDE.
  3. El sistema confirma la transmisión exitosa y genera automáticamente el boletín oficial en PDF para que pueda ser impreso, firmado y archivado como respaldo legal.
- **Resultado esperado:** Todas las calificaciones del curso se cargan de forma exacta en el sistema estatal SIE sin la necesidad de que la secretaria transcriba dígito por dígito durante la madrugada, obteniendo simultáneamente el comprobante legal de cierre.

**REGLAS / RESTRICCIONES QUE NO PUEDEN ROMPERSE:**
1. **Mapeo por Identidad Única:** La exportación de datos al SIE debe vincularse estrictamente utilizando el código único del estudiante (RUDE), jamás dependiendo del orden de la lista visual o alfabético, para evitar que el ingreso o retiro de un alumno desplace las calificaciones de sus compañeros.
2. **Estandarización y Límites:** El sistema debe truncar o redondear los decimales desde la base de datos central antes de la exportación, asegurando que ninguna nota supere los límites paramétricos establecidos (ej. la dimensión del "Saber" no puede ser mayor a 35).
3. **Bloqueo de Inmutabilidad:** Una vez completado el cierre operativo y la exportación al SIE, el periodo académico pasa inmediatamente a estado de "Solo Lectura" (Read-Only), impidiendo cualquier alteración retroactiva clandestina sin autorización directa de la dirección.

**CASO DE PRUEBA CONCRETO (obligatorio):**
- **DATOS DE ENTRADA:** 
  * Acción: Sincronización al SIE del curso 6to de Secundaria "A".
  * Estudiante 1 (Antiguo): Arce Quinteros Gabriel (RUDE: 806700122023) - Notas: Matemáticas 85, Lenguaje 92.
  * Estudiante 2 (Nueva/Transferida): Blanco Ruiz Maria (RUDE: 806700142023) - Notas: Matemáticas 94, Lenguaje 96.
  * Estudiante 3 (Retirado a mitad de trimestre): Callejas Marin Mario.
  
- **DATOS DE SALIDA ESPERADOS:** 
  * EduSync se conecta y asigna automáticamente al SIE un 85 y 92 a las casillas del RUDE de Arce Quinteros Gabriel.
  * Identifica a la alumna transferida (Blanco Ruiz Maria) a través de su RUDE, cargando correctamente su 94 y 96 en el SIE, ignorando que fue agregada recientemente al final de los cuadernos pedagógicos.
  * Omite enviar datos de Callejas Marin Mario (alumna retirada), sin provocar que las notas de los alumnos siguientes se desplacen a una fila equivocada.
  * Muestra una alerta verde ("Validado") y emite el boletín PDF listo para descargar.

**¿CÓMO MEDIRÍAMOS QUE FUNCIONA?**
Sabremos que el sistema funciona cuando el tiempo dedicado a transcribir notas hacia el sistema del Ministerio de Educación se reduzca de madrugadas enteras (entre las 2:00 AM y las 4:00 AM) a una acción que tome solo unos segundos o minutos. Además, la métrica de éxito definitiva será alcanzar un 0% de errores de "copiar y pegar", eliminando por completo los memorándums y sanciones económicas al personal administrativo y docente por inconsistencias de datos.
