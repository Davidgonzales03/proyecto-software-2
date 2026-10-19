import type { LineaRechazada } from './pedido';

// El backend responde los errores como ProblemDetail (RFC 9457), desde
// ManejadorErrores: {type, title, status, detail} más dos extensiones propias,
// `codigo` y, en los datos inválidos, `errores`.
// Los errores no controlados (500) no llevan `codigo`.
export interface ErrorCampo {
	campo: string;
	mensaje: string;
}

export interface ErrorApi {
	status: number;
	detail: string;
	codigo?: string;
	errores?: ErrorCampo[];
	// datos para explicar un rechazo de la compra (EDAD_NO_PERMITIDA,
	// LIMITE_COMPRA_EXCEDIDO)
	edadMinima?: number;
	limite?: number;
	puedeAgregar?: number;
	// los rechazos del pedido (HU-12): el pedido pendiente, las líneas que no se
	// pueden confirmar y el total nuevo
	idPedido?: number;
	lineas?: LineaRechazada[];
	total?: number;
}

export function esErrorApi(valor: unknown): valor is ErrorApi {
	if (valor === null || typeof valor !== 'object') {
		return false;
	}
	const posible = valor as Partial<ErrorApi>;
	return typeof posible.status === 'number' && typeof posible.detail === 'string';
}

// Los errores por campo vienen como lista de {campo, mensaje}, no como objeto
export function errorDelCampo(error: ErrorApi | null, campo: string): string | null {
	return error?.errores?.find((cada) => cada.campo === campo)?.mensaje ?? null;
}

// Los servicios rechazan con un ErrorApi ya armado; cualquier otra cosa (un
// error de programación, una promesa rechazada sin cuerpo) se trata como falta
// de conexión, para que la pantalla siempre tenga un mensaje que mostrar
export function comoError(error: unknown): ErrorApi {
	return esErrorApi(error) ? error : { ...SIN_CONEXION };
}

// Lo que se muestra cuando no hay respuesta del servidor. Congelado, y cada
// llamada devuelve una copia: una pantalla que la cambie no afecta a otra
export const SIN_CONEXION: Readonly<ErrorApi> = Object.freeze({
	status: 0,
	detail: 'No se pudo contactar al servidor',
	codigo: 'SIN_CONEXION',
});
