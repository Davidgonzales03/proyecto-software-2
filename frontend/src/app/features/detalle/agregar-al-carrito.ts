import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { form, FormField, required, submit, validate } from '@angular/forms/signals';
import { Router, RouterLink } from '@angular/router';
import { LucideShoppingCart } from '@lucide/angular';

import { CarritoService } from '../../core/api/carrito.service';
import { mensajeDeCampo, mensajeDeError, primerMensaje } from '../../core/api/mensajes-error';
import { CANTIDAD_MAXIMA_EN_CARRITO } from '../../core/modelos/carrito';
import { ProductoEnDetalle } from '../../core/modelos/catalogo';
import { ErrorApi } from '../../core/modelos/error-api';
import { DECIMALES_DE_UNIDAD } from '../../core/modelos/inventario';
import { SesionService } from '../../core/sesion/sesion.service';
import { leerCantidad, problemaDeCantidad } from '../../shared/formato/cantidades';

// La cantidad y «Agregar al carrito» del detalle del producto (HU-11). Compra
// el Cliente; el visitante ve el botón, que lo lleva a iniciar sesión y lo
// devuelve al producto (RF-6); el personal no lo ve (RF-7: usabilidad, lo
// valida el backend)
@Component({
	selector: 'app-agregar-al-carrito',
	imports: [FormField, RouterLink, LucideShoppingCart],
	templateUrl: './agregar-al-carrito.html',
	styleUrl: './agregar-al-carrito.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AgregarAlCarrito {
	readonly producto = input.required<ProductoEnDetalle>();

	private readonly sesion = inject(SesionService);
	private readonly carrito = inject(CarritoService);
	private readonly router = inject(Router);

	protected readonly esCliente = computed(() => this.sesion.rol() === 'CLIENTE');
	protected readonly esVisitante = computed(() => this.sesion.rol() === null);

	// «Unidad» no se escribe, como junto al precio
	protected readonly unidad = computed(() => {
		const unidad = this.producto().unidad;
		return unidad === 'UNIDAD' ? null : unidad.toLowerCase();
	});

	private readonly datos = signal({ cantidad: '1' });
	protected readonly compra = form(this.datos, (ruta) => {
		required(ruta.cantidad, { message: 'Escriba la cantidad.' });
		validate(ruta.cantidad, ({ value }) => {
			const problema = problemaDeCantidad(value(), {
				decimales: DECIMALES_DE_UNIDAD[this.producto().unidad],
				maximo: CANTIDAD_MAXIMA_EN_CARRITO,
				positiva: true,
			});
			return problema === null ? null : { kind: 'cantidad', message: problema };
		});
	});
	protected readonly errorDeCantidad = computed(() => {
		const campo = this.compra.cantidad();
		return campo.touched() && campo.invalid() ? primerMensaje(campo.errors()) : '';
	});
	protected readonly agregando = signal(false);
	protected readonly agregado = signal(false);
	protected readonly error = signal('');
	// Otro producto empieza de cero sin hacer nada: mientras carga, el detalle
	// muestra «Cargando…» y este componente se vuelve a crear

	protected async agregar(evento: Event): Promise<void> {
		evento.preventDefault();
		const producto = this.producto();
		if (!producto.disponible || this.agregando()) {
			return;
		}
		if (this.esVisitante()) {
			await this.router.navigate(['/inicio-sesion'], { queryParams: { destino: this.router.url } });
			return;
		}
		this.agregado.set(false);
		this.error.set('');
		this.agregando.set(true);
		await submit(this.compra, {
			action: async (campos) => {
				try {
					await this.carrito.agregar(producto.idProducto, leerCantidad(campos().value().cantidad) ?? 0);
					this.agregado.set(true);
				} catch (rechazo: unknown) {
					this.error.set(mensajeDelRechazo(rechazo as ErrorApi));
				}
				return undefined;
			},
		});
		this.agregando.set(false);
	}
}

// Un dato rechazado se explica con su campo; el producto que dejó de venderse,
// con las palabras de la historia; lo demás, con el diccionario
function mensajeDelRechazo(error: ErrorApi): string {
	const primero = error.errores?.[0];
	if (primero !== undefined) {
		return mensajeDeCampo(primero.campo, primero.mensaje);
	}
	return error.codigo === 'PRODUCTO_NO_ENCONTRADO' ? 'Este producto ya no está disponible.' : mensajeDeError(error);
}
