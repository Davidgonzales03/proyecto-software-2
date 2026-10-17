import { ChangeDetectionStrategy, Component, computed, inject, signal, viewChild } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideShoppingCart, LucideTrash2, LucideWrench } from '@lucide/angular';

import { comoErrorApi } from '../../core/api/autenticacion.service';
import { CarritoService } from '../../core/api/carrito.service';
import { mensajeDeCampo, mensajeDeError } from '../../core/api/mensajes-error';
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
	imports: [RouterLink, DialogoConfirmacion, LucideShoppingCart, LucideTrash2, LucideWrench],
	templateUrl: './carrito.page.html',
	styleUrl: './carrito.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CarritoPage {
	private readonly servicio = inject(CarritoService);
	private readonly dialogo = viewChild.required(DialogoConfirmacion);

	protected readonly recurso = this.servicio.carrito;
	protected readonly carrito = computed(() => (this.recurso.hasValue() ? this.recurso.value() : undefined));
	protected readonly errorDeCarga = computed(() => {
		const error = this.recurso.error();
		return error === undefined ? '' : mensajeDeError(comoErrorApi(error));
	});

	// El motivo por línea (una cantidad inválida o rechazada) y el de quitar o vaciar
	protected readonly errores = signal<Readonly<Record<number, string>>>({});
	protected readonly aviso = signal('');

	protected readonly avisos = AVISOS_DE_LINEA;
	protected readonly precio = precio;
	protected readonly cantidad = cantidad;

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
			campo.value = cantidad(linea.cantidad);
			this.ponerError(linea.idProducto, mensajeDelRechazo(rechazo as ErrorApi));
		}
	}

	protected async quitar(linea: LineaDeCarrito): Promise<void> {
		this.aviso.set('');
		try {
			await this.servicio.quitar(linea.idProducto);
			this.ponerError(linea.idProducto, '');
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
		} catch (rechazo: unknown) {
			this.aviso.set(mensajeDeError(rechazo as ErrorApi));
		}
	}

	private ponerError(idProducto: number, mensaje: string): void {
		this.errores.update((actuales) => ({ ...actuales, [idProducto]: mensaje }));
	}
}

function mensajeDelRechazo(error: ErrorApi): string {
	const primero = error.errores?.[0];
	return primero === undefined ? mensajeDeError(error) : mensajeDeCampo(primero.campo, primero.mensaje);
}
