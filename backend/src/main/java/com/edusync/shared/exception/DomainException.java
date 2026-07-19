package com.edusync.shared.exception;

/**
 * Excepcion base de dominio para EduSync. Todo error de negocio con un codigo
 * identificable (p. ej. {@code E_RANGO_INVALIDO}, {@code E_TENANT_NO_ACTIVO}) debe
 * extender esta clase en el modulo que lo declara, nunca lanzar excepciones genericas
 * de Java para reglas de negocio.
 */
public class DomainException extends RuntimeException {

  private final String errorCode;

  public DomainException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
