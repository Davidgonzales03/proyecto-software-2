import { leerCantidad, problemaDeCantidad } from './cantidades';

const POR_UNIDAD = { decimales: 0, maximo: 99_999.99, positiva: true };
const POR_METRO = { decimales: 2, maximo: 99_999.99, positiva: true };
const CONTADA = { decimales: 0, maximo: 9_999_999.99, positiva: false };

// HU-08: cantidades de stock
describe('leerCantidad', () => {
  it('acepta coma o punto para los decimales', () => {
    expect(leerCantidad('12,5')).toBe(12.5);
    expect(leerCantidad(' 12.50 ')).toBe(12.5);
    expect(leerCantidad('0')).toBe(0);
    expect(leerCantidad('9999999,99')).toBe(9_999_999.99);
    // Ceros de más: los acepta igual que problemaDeCantidad
    expect(leerCantidad('5,000')).toBe(5);
    expect(leerCantidad('012')).toBe(12);
  });

  it('no es una cantidad con signo, letras, separador de miles o vacía', () => {
    for (const texto of ['-1', 'abc', '1.234,5', '', '1e3']) {
      expect(leerCantidad(texto)).toBeNull();
    }
  });
});

// HU-08 RF-5: los decimales según la unidad y los límites
describe('problemaDeCantidad', () => {
  it('no hay problema con una cantidad válida ni con el campo vacío', () => {
    expect(problemaDeCantidad('10', POR_UNIDAD)).toBeNull();
    expect(problemaDeCantidad('25,5', POR_METRO)).toBeNull();
    expect(problemaDeCantidad('2,00', POR_UNIDAD)).toBeNull();
    expect(problemaDeCantidad('0', CONTADA)).toBeNull();
    expect(problemaDeCantidad('', POR_UNIDAD)).toBeNull();
  });

  it('explica cada caso', () => {
    expect(problemaDeCantidad('2,5', POR_UNIDAD)).toBe('Este producto se cuenta en números enteros.');
    expect(problemaDeCantidad('25,555', POR_METRO)).toBe('La cantidad admite hasta dos decimales.');
    expect(problemaDeCantidad('100000', POR_UNIDAD)).toBe('La cantidad admite hasta 99.999,99.');
    expect(problemaDeCantidad('0', POR_UNIDAD)).toBe('La cantidad tiene que ser mayor que 0.');
    expect(problemaDeCantidad('-3', CONTADA)).toBe('Escribe la cantidad con números, por ejemplo 12,5.');
  });
});
