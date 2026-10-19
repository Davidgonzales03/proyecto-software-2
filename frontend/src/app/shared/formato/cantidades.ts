import { cantidad } from './precios';

// Una cantidad de stock como la escribe una persona: «12», «12,5» o «12.50»,
// sin signo ni separador de miles. null si no tiene esa forma. Los decimales y
// el máximo los revisa problemaDeCantidad, con la misma forma: un texto que
// pasa esa revisión siempre se puede leer
export function leerCantidad(texto: string): number | null {
	const limpio = texto.trim();
	return FORMA.test(limpio) ? Number(limpio.replace(',', '.')) : null;
}

const FORMA = /^(\d+)(?:[.,](\d+))?$/;

export interface ReglasDeCantidad {
	// Los decimales que admite la unidad del producto: 0 o 2
	decimales: number;
	maximo: number;
	// La entrada tiene que ser mayor que 0; la cantidad contada y el mínimo pueden ser 0
	positiva: boolean;
}

// Por qué lo escrito no es una cantidad válida (HU-08 RF-5); null si lo es o
// si está vacío, que es cosa de la regla de obligatorio
export function problemaDeCantidad(texto: string, reglas: ReglasDeCantidad): string | null {
	const limpio = texto.trim();
	if (limpio === '') {
		return null;
	}
	const partes = FORMA.exec(limpio);
	if (partes === null) {
		return 'Escriba la cantidad con números, por ejemplo 12,5.';
	}
	const decimales = (partes[2] ?? '').replace(/0+$/, '').length;
	if (decimales > reglas.decimales) {
		return reglas.decimales === 0
			? 'Este producto se cuenta en números enteros.'
			: 'La cantidad admite hasta dos decimales.';
	}
	const valor = Number(limpio.replace(',', '.'));
	if (valor > reglas.maximo) {
		return `La cantidad admite hasta ${cantidad(reglas.maximo)}.`;
	}
	if (reglas.positiva && valor === 0) {
		return 'La cantidad tiene que ser mayor que 0.';
	}
	return null;
}

// La cantidad que dejan los botones − y + (HU-11 RF-15): de a 1. Con más
// decimales de los que admite la unidad, va a la válida de al lado (2,5 por
// unidad: + da 3 y − da 2); por encima del máximo, − lo deja en el máximo. null
// si quedaría en 0 o menos, o pasaría del máximo
export function cantidadConPaso(actual: number, paso: 1 | -1, decimales: number, maximo: number): number | null {
	const escala = 10 ** decimales;
	// El margen absorbe el error de coma flotante: 2,3 × 100 da 229,99999999999997
	const escalada = actual * escala;
	const base = paso > 0 ? Math.floor(escalada + 1e-6) : Math.ceil(escalada - 1e-6);
	const nueva = (base + paso * escala) / escala;
	if (paso < 0 && nueva > maximo) {
		return maximo;
	}
	return nueva <= 0 || nueva > maximo ? null : nueva;
}
