/**
 * Modulo {@code shared} (Spring Modulith) - shared kernel: {@code TenantContext},
 * {@code audit_log}, excepciones comunes y utilidades transversales.
 *
 * <p>Sin Aggregate Root propio. Declarado {@code OPEN}: visible para todos los demas
 * modulos ({@code plataforma}, {@code identidad}, {@code academico}, {@code notassie}),
 * que si pueden depender de el; {@code shared} nunca depende de ellos en sentido
 * inverso (ADR-0011 &sect;3, punto 2).
 *
 * <p>Bootstrap creado por {@code DD-UC-001} / {@code PR-IMPL-001}; {@code TenantContext}
 * es un placeholder hasta la implementacion real en {@code DD-UC-002} (ADR-0001).
 */
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package com.edusync.shared;
