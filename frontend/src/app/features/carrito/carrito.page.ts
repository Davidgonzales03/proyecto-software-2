import { ChangeDetectionStrategy, Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideCircleSlash, LucideShoppingCart, LucideTrash2, LucideTriangleAlert, LucideWrench } from '@lucide/angular';

import { comoErrorApi } from '../../core/api/autenticacion.service';
import { CarritoService } from '../../core/api/carrito.service';
import { mensajeDeError, mensajeDelRechazo } from '../../core/api/mensajes-error';
import { AVISOS_DE_LINEA, CANTIDAD_MAXIMA_EN_CARRITO, LineaDeCarrito } from '../../core/modelos/carrito';
import { urlDeImagen } from '../../core/modelos/catalogo';
import { ErrorApi } from '../../core/modelos/error-api';
import { DECIMALES_DE_UNIDAD } from '../../core/modelos/inventario';
import { DialogoConfirmacion } from '../../shared/dialogo-confirmacion/dialogo-confirmacion';
import { leerCantidad, problemaDeCantidad } from '../../shared/formato/cantidades';
import { cantidad, precio } from '../../shared/formato/precios';

// El carrito del Cliente (HU-11): cada línea con su foto, el precio de hoy, la
// cantidad que se cambia en el lugar y el subtotal; lo que ya no se puede
// comprar queda marcado y fuera del total. El botón para confirmar el pedido
// llega con HU-12
@Component({
	selector: 'app-carrito',
	imports: [
		RouterLink,
		DialogoConfirmacion,
		LucideCircleSlash,
		LucideShoppingCart,
		LucideTrash2,
		LucideTriangleAlert,
		LucideWrench,
	],
	templateUrl: './carrito.page.html',
	styleUrl: './carrito.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CarritoPage {
	private readonly servicio = inject(CarritoService);
	private readonly dialogo = viewChild.required(DialogoConfirmacion);
	private readonly titulo = viewChild.required<ElementRef<HTMLElement>>('titulo');

	protected readonly recurso = this.servicio.carrito;
	protected readonly carrito = computed(() => (this.recurso.hasValue() ? this.recurso.value() : undefined));
	protected readonly errorDeCarga = computed(() => {
		const error = this.recurso.error();
		return error === undefined ? '' : mensajeDeError(comoErrorApi(error));
	});

	// El motivo por línea (una cantidad inválida o rechazada) y el de quitar o vaciar
	protected readonly errores = signal<Readonly<Record<number, string>>>({});
	protected readonly aviso = signal('');
	// Lo que se quitó o se vació, para los lectores de pantalla
	protected readonly anuncio = signal('');

	protected readonly avisos = AVISOS_DE_LINEA;
	protected readonly precio = precio;
	protected readonly cantidad = cantidad;

	constructor() {
		// El carrito se cargó al entrar: al abrir la página se pide de nuevo, con el precio y la
		// disponibilidad de hoy y lo agregado desde otro dispositivo (RF-8, RF-9, RF-13)
		this.recurso.reload();
	}

	protected foto(linea: LineaDeCarrito): string | null {
		return linea.imagen === null ? null : urlDeImagen(linea.idProducto, linea.imagen);
	}

	// «Unidad» no se escribe, como en el detalle
	protected unidad(linea: LineaDeCarrito): string | null {
		return linea.unidad === 'UNIDAD' ? null : linea.unidad.toLowerCase();
	}

	// Se guarda al confirmar el campo (Enter o al salir). Si la cantidad no
	// sirve, no se manda; si la API la rechaza, vuelve la anterior (RF-10)
	protected async cambiar(linea: LineaDeCarrito, evento: Event): Promise<void> {
		const campo = evento.target as HTMLInputElement;
		const texto = campo.value;
		const problema =
			texto.trim() === ''
				? 'Escriba la cantidad.'
				: problemaDeCantidad(texto, {
						decimales: DECIMALES_DE_UNIDAD[linea.unidad],
						maximo: CANTIDAD_MAXIMA_EN_CARRITO,
						positiva: true,
					});
		if (problema !== null) {
			this.ponerError(linea.idProducto, problema);
			return;
		}
		const nueva = leerCantidad(texto) ?? 0;
		this.ponerError(linea.idProducto, '');
		if (nueva === linea.cantidad) {
			return;
		}
		try {
			await this.servicio.cambiarCantidad(linea.idProducto, nueva);
		} catch (rechazo: unknown) {
			const error = rechazo as ErrorApi;
			campo.value = cantidad(linea.cantidad);
			// La línea ya no existe (se quitó en otro dispositivo): el carrito se pide de nuevo
			if (error.status === 404) {
				this.recurso.reload();
				return;
			}
			this.ponerError(linea.idProducto, mensajeDelRechazo(error));
		}
	}

	protected async quitar(linea: LineaDeCarrito): Promise<void> {
		this.aviso.set('');
		try {
			await this.servicio.quitar(linea.idProducto);
			this.ponerError(linea.idProducto, '');
			this.despuesDeQuitar(`Se quitó ${linea.nombre} del carrito.`);
		} catch (rechazo: unknown) {
			this.aviso.set(mensajeDeError(rechazo as ErrorApi));
		}
	}

	protected async vaciar(): Promise<void> {
		const confirmado = await this.dialogo().abrir({
			titulo: '¿Vaciar el carrito?',
			mensaje: 'Se quitan todos los productos del carrito.',
			confirmar: 'Vaciar',
		});
		if (!confirmado) {
			return;
		}
		this.aviso.set('');
		try {
			await this.servicio.vaciar();
			this.errores.set({});
			this.despuesDeQuitar('Se vació el carrito.');
		} catch (rechazo: unknown) {
			this.aviso.set(mensajeDeError(rechazo as ErrorApi));
		}
	}

	// El botón que tenía el foco ya no existe: el foco va al título, sin desplazar
	private despuesDeQuitar(anuncio: string): void {
		this.anuncio.set(anuncio);
		this.titulo().nativeElement.focus({ preventScroll: true });
	}

	private ponerError(idProducto: number, mensaje: string): void {
		this.errores.update((actuales) => ({ ...actuales, [idProducto]: mensaje }));
	}
}
