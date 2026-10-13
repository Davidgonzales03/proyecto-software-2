import { HttpClient, httpResource, HttpResourceRef } from '@angular/common/http';
import { inject, Injectable, Signal } from '@angular/core';

import { Categoria } from '../modelos/categoria';
import { FiltrosDeInventario, MotivoAjuste, Movimiento, ProductoEnInventario } from '../modelos/inventario';
import { Pagina } from '../modelos/pagina';
import { escribir } from './escribir';

const INVENTARIO = '/api/inventario';

// Llamadas a /api/inventario (HU-08). Leen el Empleado, el Administrador y el
// Contador; registran los dos primeros: el backend lo valida en cada petición.
@Injectable({ providedIn: 'root' })
export class InventarioService {
	private readonly http = inject(HttpClient);

	// httpResource necesita un contexto de inyección: se llama al construir la pantalla
	listado(filtros: Signal<FiltrosDeInventario>): HttpResourceRef<Pagina<ProductoEnInventario> | undefined> {
		return httpResource<Pagina<ProductoEnInventario>>(() => ({ url: INVENTARIO, params: parametros(filtros()) }));
	}

	// Todas las categorías, también las ocultas, para el filtro
	categorias(): HttpResourceRef<Categoria[] | undefined> {
		return httpResource<Categoria[]>(() => `${INVENTARIO}/categorias`);
	}

	// Sin id válido no pide nada
	producto(idProducto: Signal<number | null>): HttpResourceRef<ProductoEnInventario | undefined> {
		return httpResource<ProductoEnInventario>(() => {
			const id = idProducto();
			return id === null ? undefined : `${INVENTARIO}/${id}`;
		});
	}

	historial(idProducto: Signal<number | null>, pagina: Signal<number>): HttpResourceRef<Pagina<Movimiento> | undefined> {
		return httpResource<Pagina<Movimiento>>(() => {
			const id = idProducto();
			if (id === null) {
				return undefined;
			}
			const numero = pagina();
			const params: Record<string, string> = numero > 0 ? { pagina: String(numero) } : {};
			return { url: `${INVENTARIO}/${id}/movimientos`, params };
		});
	}

	registrarEntrada(idProducto: number, cantidad: number, nota: string): Promise<ProductoEnInventario> {
		return escribir(
			this.http.post<ProductoEnInventario>(`${INVENTARIO}/${idProducto}/entradas`, { cantidad, nota: opcional(nota) }),
		);
	}

	registrarAjuste(idProducto: number, contada: number, motivo: MotivoAjuste, nota: string): Promise<ProductoEnInventario> {
		return escribir(
			this.http.post<ProductoEnInventario>(`${INVENTARIO}/${idProducto}/ajustes`, {
				contada,
				motivo,
				nota: opcional(nota),
			}),
		);
	}

	// null quita el mínimo
	cambiarMinimo(idProducto: number, minimo: number | null): Promise<ProductoEnInventario> {
		return escribir(this.http.put<ProductoEnInventario>(`${INVENTARIO}/${idProducto}/minimo`, { minimo }));
	}
}

function opcional(texto: string): string | null {
	const limpio = texto.trim();
	return limpio === '' ? null : limpio;
}

// Solo viaja lo que filtra, como en las otras listas
function parametros(filtros: FiltrosDeInventario): Record<string, string> {
	const resultado: Record<string, string> = {};
	const texto = filtros.texto.trim();
	if (texto !== '') {
		resultado['texto'] = texto;
	}
	if (filtros.categoria !== null) {
		resultado['categoria'] = String(filtros.categoria);
	}
	if (filtros.activo !== null) {
		resultado['activo'] = String(filtros.activo);
	}
	if (filtros.soloBajo) {
		resultado['bajo'] = 'true';
	}
	if (filtros.pagina > 0) {
		resultado['pagina'] = String(filtros.pagina);
	}
	return resultado;
}
