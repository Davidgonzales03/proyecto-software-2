import { UnidadVenta } from './producto';

// Espejo de CarritoResponse y LineaCarritoResponse del backend (HU-11). Sin stock: la tienda nunca lo muestra

export type EstadoDeLinea = 'DISPONIBLE' | 'AGOTADO' | 'NO_DISPONIBLE' | 'CANTIDAD_INVALIDA';

// Lo que dice una línea que no se puede comprar (HU-11 RF-9)
export const AVISOS_DE_LINEA: Record<Exclude<EstadoDeLinea, 'DISPONIBLE'>, string> = {
	AGOTADO: 'Agotado',
	NO_DISPONIBLE: 'No disponible',
	CANTIDAD_INVALIDA: 'Corrija la cantidad',
};

export interface LineaDeCarrito {
	idProducto: number;
	nombre: string;
	// null si el producto no tiene fotos
	imagen: string | null;
	precio: number;
	unidad: UnidadVenta;
	cantidad: number;
	// null si la línea no se puede comprar: no suma al total
	subtotal: number | null;
	estado: EstadoDeLinea;
}

export interface Carrito {
	lineas: LineaDeCarrito[];
	total: number;
	cantidadLineas: number;
}

// La cantidad por línea (HU-11 RF-3); los decimales dependen de la unidad
export const CANTIDAD_MAXIMA_EN_CARRITO = 999;
