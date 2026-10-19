import { HttpClient, httpResource } from '@angular/common/http';
import { computed, inject, Injectable } from '@angular/core';

import { Carrito } from '../modelos/carrito';
import { SesionService } from '../sesion/sesion.service';
import { escribir } from './escribir';

const CARRITO = '/api/carrito';

// El carrito del Cliente (HU-11). Uno para toda la aplicación: lo leen el
// encabezado y la página del carrito. Solo se pide con rol Cliente (RF-7); al
// cerrar sesión queda sin pedido, y el cliente que entra después pide el suyo
// (entre dos cuentas siempre hay un cierre: «Iniciar sesión» es solo para
// visitantes). Cada escritura devuelve el carrito entero y se deja en el
// recurso, sin pedirlo otra vez.
@Injectable({ providedIn: 'root' })
export class CarritoService {
	private readonly http = inject(HttpClient);
	private readonly sesion = inject(SesionService);

	private readonly esCliente = computed(() => this.sesion.rol() === 'CLIENTE');

	readonly carrito = httpResource<Carrito>(() => (this.esCliente() ? CARRITO : undefined));

	// null si el carrito no cargó: no se sabe cuántos hay (el encabezado no afirma que está vacío)
	readonly cantidadLineas = computed<number | null>(() => {
		if (!this.esCliente()) {
			return 0;
		}
		return this.carrito.hasValue() ? this.carrito.value().cantidadLineas : null;
	});

	// Las escrituras van una después de la otra: el servidor las aplica en orden, y así la respuesta
	// más vieja nunca pisa a la más nueva. Una rechazada no traba las siguientes. Sin otra en curso,
	// sale en el acto
	private enCurso: Promise<unknown> | null = null;

	agregar(idProducto: number, cantidad: number): Promise<Carrito> {
		return this.guardar(this.http.post<Carrito>(`${CARRITO}/lineas`, { idProducto, cantidad }));
	}

	cambiarCantidad(idProducto: number, cantidad: number): Promise<Carrito> {
		return this.guardar(this.http.put<Carrito>(`${CARRITO}/lineas/${idProducto}`, { cantidad }));
	}

	quitar(idProducto: number): Promise<Carrito> {
		return this.guardar(this.http.delete<Carrito>(`${CARRITO}/lineas/${idProducto}`));
	}

	vaciar(): Promise<Carrito> {
		return this.guardar(this.http.delete<Carrito>(CARRITO));
	}

	private guardar(pedido: Parameters<typeof escribir<Carrito>>[0]): Promise<Carrito> {
		const aplicar = async (): Promise<Carrito> => {
			const carrito = await escribir(pedido);
			this.carrito.set(carrito);
			return carrito;
		};
		const escritura = this.enCurso === null ? aplicar() : this.enCurso.then(aplicar, aplicar);
		this.enCurso = escritura;
		// Se registra antes que quien espera la escritura: al seguir, ya no figura en curso
		const terminar = (): void => {
			if (this.enCurso === escritura) {
				this.enCurso = null;
			}
		};
		escritura.then(terminar, terminar);
		return escritura;
	}
}
