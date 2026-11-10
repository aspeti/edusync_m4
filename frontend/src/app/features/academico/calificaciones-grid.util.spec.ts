import {
  calcularCambios,
  estadoDeCelda,
  mensajeEstadoCelda,
} from './calificaciones-grid.util';

describe('estadoDeCelda', () => {
  it('SIN_NOTA cuando no hay guardada ni editada', () => {
    expect(estadoDeCelda(null, null, 100)).toBe('SIN_NOTA');
  });

  it('GUARDADA cuando el editado coincide con el guardado', () => {
    expect(estadoDeCelda(37.5, 37.5, 100)).toBe('GUARDADA');
  });

  it('MODIFICADA cuando hay un valor nuevo válido', () => {
    expect(estadoDeCelda(null, 10, 100)).toBe('MODIFICADA');
    expect(estadoDeCelda(10, 20, 100)).toBe('MODIFICADA');
  });

  it('acepta 0 y el máximo como válidos', () => {
    expect(estadoDeCelda(null, 0, 100)).toBe('MODIFICADA');
    expect(estadoDeCelda(null, 100, 100)).toBe('MODIFICADA');
  });

  it('INVALIDA fuera de rango', () => {
    expect(estadoDeCelda(null, -1, 100)).toBe('INVALIDA');
    expect(estadoDeCelda(null, 100.01, 100)).toBe('INVALIDA');
  });

  it('INVALIDA si se borra una nota ya guardada', () => {
    expect(estadoDeCelda(85, null, 100)).toBe('INVALIDA');
  });
});

describe('mensajeEstadoCelda', () => {
  it('explica el borrado de una nota guardada', () => {
    expect(mensajeEstadoCelda(85, null, 100)).toContain('no se puede borrar');
  });

  it('explica el rango cuando el valor está fuera', () => {
    expect(mensajeEstadoCelda(null, 120, 100)).toBe('Debe estar en [0, 100]');
  });
});

describe('calcularCambios', () => {
  const filas = [
    { estudianteId: 'a', valor: 10 },
    { estudianteId: 'b', valor: null },
    { estudianteId: 'c', valor: 20 },
  ];

  it('devuelve solo las modificadas', () => {
    const r = calcularCambios(filas, { a: 10, b: 15, c: 25 }, 100);
    expect(r.invalidos).toEqual([]);
    expect(r.items).toEqual([
      { estudianteId: 'b', valor: 15 },
      { estudianteId: 'c', valor: 25 },
    ]);
  });

  it('no envía celdas vacías que siguen vacías', () => {
    const r = calcularCambios(filas, { a: 10, b: null, c: 20 }, 100);
    expect(r.items).toEqual([]);
    expect(r.invalidos).toEqual([]);
  });

  it('marca inválidos y no los incluye en items', () => {
    const r = calcularCambios(filas, { a: null, b: -5, c: 20 }, 100);
    expect(r.items).toEqual([]);
    expect(r.invalidos).toEqual(['a', 'b']);
  });

  it('acepta 0 como cambio válido', () => {
    const r = calcularCambios(filas, { a: 0, b: null, c: 20 }, 100);
    expect(r.invalidos).toEqual([]);
    expect(r.items).toEqual([{ estudianteId: 'a', valor: 0 }]);
  });
});
