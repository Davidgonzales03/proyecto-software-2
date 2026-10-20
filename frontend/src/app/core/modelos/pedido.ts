import { AVISOS_DE_LINEA } from './carrito';
import { UnidadVenta } from './producto';

// Espejo de PedidoResponse, LineaPedidoResponse, RetiroResponse, CobroResponse y CancelacionResponse del
// backend (HU-12, HU-13)

export type EstadoDePedido = 'PENDIENTE_PAGO' | 'PAGADO' | 'CANCELADO' | 'VENCIDO' | 'PAGO_A_DEVOLVER';

export const NOMBRES_DE_ESTADO: Record<EstadoDePedido, string> = {
	PENDIENTE_PAGO: 'Pendiente de pago',
	PAGADO: 'Pagado',
	CANCELADO: 'Cancelado',
	VENCIDO: 'Vencido',
	PAGO_A_DEVOLVER: 'Pago a devolver',
};

// Dónde y cuándo retirar (RF-17); null si no está configurado
export interface Retiro {
	direccion: string | null;
	horario: string | null;
}

// Como se confirmó: no cambia si el producto cambia después (RF-2)
export interface LineaDePedido {
	idProducto: number;
	nombre: string;
	unidad: UnidadVenta;
	precio: number;
	cantidad: number;
	subtotal: number;
}

export interface Pedido {
	idPedido: number;
	estado: EstadoDePedido;
	lineas: LineaDePedido[];
	total: number;
	nombreRetira: string;
	telefonoRetira: string;
	nota: string | null;
	confirmado: string;
	vence: string;
	// Los cuenta el servidor; 0 si ya no espera el pago (RF-8)
	segundosParaPagar: number;
	retiro: Retiro;
	// Cuándo se pagó; null si no (HU-13 RF-5)
	pagado: string | null;
	// El QR pendiente mientras espera el pago; null si no hay (HU-13 RF-1)
	cobro: Cobro | null;
}

// El QR que el Cliente paga y, si el sistema de pago la da, su página de pago.
// simulador: se muestran «Simular pago» y «Simular error» (HU-13 RF-12)
export interface Cobro {
	codigoQr: string;
	urlPasarela: string | null;
	simulador: boolean;
}

// Lo que se fuerza en el simulador
export type ResultadoSimulado = 'PAGADO' | 'ERROR';

export interface Cancelacion {
	pedido: Pedido;
	// Las que no entraron porque el carrito ya tenía 50 (RF-16)
	lineasQueNoVolvieron: number;
}

// Lo que escribe el Cliente en la confirmación; el total es el que vio (RF-15)
export interface DatosDeConfirmacion {
	nombreRetira: string;
	telefonoRetira: string;
	nota: string;
	totalEsperado: number;
}

// Por qué una línea impide confirmar (RF-3): nunca cuánto stock hay
export type MotivoDeRechazo = 'AGOTADO' | 'NO_DISPONIBLE' | 'CANTIDAD_INVALIDA' | 'NO_ALCANZA';

export interface LineaRechazada {
	idProducto: number;
	motivo: MotivoDeRechazo;
}

export const AVISOS_DE_RECHAZO: Record<MotivoDeRechazo, string> = {
	...AVISOS_DE_LINEA,
	NO_ALCANZA: 'No hay stock suficiente',
};
