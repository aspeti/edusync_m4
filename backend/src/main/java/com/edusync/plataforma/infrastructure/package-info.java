/**
 * Adaptadores de entrada/salida del modulo {@code plataforma} (REST, scheduler, persistencia
 * JPA, puertos publicos hacia otros modulos). Incluye
 * {@code adapter.out.port.TenantConsultaPortImpl}, la implementacion real del puerto
 * publico {@code identidad.TenantConsultaPort} (ver su Javadoc: el puerto vive en
 * {@code identidad} para evitar un ciclo de modulos, pero la implementacion vive aqui).
 * Poblado por {@code DD-UC-003} (docs/design/DD-UC-003.md).
 */
package com.edusync.plataforma.infrastructure;
