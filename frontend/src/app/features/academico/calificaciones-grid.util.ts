/**
 * Funciones puras de estado y cambios de la matriz de calificaciones
 * (DD-UC-032 / FSD-UC-016). Sin I/O ni Angular.
 */

export type EstadoCelda = 'SIN_NOTA' | 'GUARDADA' | 'MODIFICADA' | 'INVALIDA';

export interface FilaCalificacion {
  estudianteId: string;
  valor: number | null;
}

export interface CambioCalificacion {
  estudianteId: string;
  valor: number;
}

export interface ResultadoCambios {
  items: CambioCalificacion[];
  invalidos: string[];
}

function esVacio(editado: number | null | undefined): boolean {
  return editado === null || editado === undefined || Number.isNaN(editado as number);
}

function mismosValores(a: number | null, b: number | null): boolean {
  if (a === null && b === null) {
    return true;
  }
  if (a === null || b === null) {
    return false;
  }
  return Math.abs(a - b) < 1e-9;
}

/**
 * Clasifica el estado de una celda según valor guardado, editado y máximo.
 * Borrar una nota ya guardada (editado vacío con guardado no nulo) es INVALIDA.
 */
export function estadoDeCelda(
  guardado: number | null,
  editado: number | null,
  max: number
): EstadoCelda {
  if (esVacio(editado)) {
    if (guardado === null || guardado === undefined) {
      return 'SIN_NOTA';
    }
    return 'INVALIDA';
  }

  const valor = editado as number;
  if (valor < 0 || valor > max) {
    return 'INVALIDA';
  }

  if (mismosValores(guardado, valor)) {
    return 'GUARDADA';
  }

  return 'MODIFICADA';
}

/**
 * Devuelve solo las celdas modificadas válidas y la lista de estudianteId inválidos.
 */
export function calcularCambios(
  filas: FilaCalificacion[],
  editados: Record<string, number | null>,
  max: number
): ResultadoCambios {
  const items: CambioCalificacion[] = [];
  const invalidos: string[] = [];

  for (const fila of filas) {
    const editado = Object.prototype.hasOwnProperty.call(editados, fila.estudianteId)
      ? editados[fila.estudianteId]
      : fila.valor;
    const estado = estadoDeCelda(fila.valor, editado, max);
    if (estado === 'INVALIDA') {
      invalidos.push(fila.estudianteId);
      continue;
    }
    if (estado === 'MODIFICADA' && !esVacio(editado)) {
      items.push({ estudianteId: fila.estudianteId, valor: editado as number });
    }
  }

  return { items, invalidos };
}

export function mensajeEstadoCelda(
  guardado: number | null,
  editado: number | null,
  max: number
): string | null {
  const estado = estadoDeCelda(guardado, editado, max);
  if (estado !== 'INVALIDA') {
    return null;
  }
  if (esVacio(editado) && guardado !== null && guardado !== undefined) {
    return 'Una nota guardada no se puede borrar; ingrese un valor';
  }
  return `Debe estar en [0, ${max}]`;
}

export function etiquetaEstado(estado: EstadoCelda): string {
  switch (estado) {
    case 'SIN_NOTA':
      return 'sin nota';
    case 'GUARDADA':
      return 'guardada';
    case 'MODIFICADA':
      return 'modificada';
    case 'INVALIDA':
      return 'inválida';
  }
}
