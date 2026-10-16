import { httpResource, HttpResourceRef } from '@angular/common/http';
import { Injectable, Signal } from '@angular/core';

import { FiltrosDeCatalogo, Portada, ProductoEnTienda } from '../modelos/catalogo';
import { Pagina } from '../modelos/pagina';

const PRODUCTOS = '/api/productos';

// Llamadas al catálogo público (HU-09), con o sin sesión. Solo lecturas
@Injectable({ providedIn: 'root' })
export class CatalogoService {
	// httpResource necesita un contexto de inyección: se llama al construir la pantalla
	portada(): HttpResourceRef<Portada | undefined> {
		return httpResource<Portada>(() => `${PRODUCTOS}/portada`);
	}

	listado(filtros: Signal<FiltrosDeCatalogo>): HttpResourceRef<Pagina<ProductoEnTienda> | undefined> {
		return httpResource<Pagina<ProductoEnTienda>>(() => ({ url: PRODUCTOS, params: parametros(filtros()) }));
	}

	marcas(): HttpResourceRef<string[] | undefined> {
		return httpResource<string[]>(() => `${PRODUCTOS}/marcas`);
	}
}

// Solo viaja lo que filtra, como en las otras listas
export function parametros(filtros: FiltrosDeCatalogo): Record<string, string> {
	const resultado: Record<string, string> = {};
	const q = filtros.q.trim();
	if (q !== '') {
		resultado['q'] = q;
	}
	if (filtros.categoria !== null) {
		resultado['categoria'] = String(filtros.categoria);
	}
	if (filtros.marca !== null) {
		resultado['marca'] = filtros.marca;
	}
	if (filtros.precioMin !== null) {
		resultado['precioMin'] = String(filtros.precioMin);
	}
	if (filtros.precioMax !== null) {
		resultado['precioMax'] = String(filtros.precioMax);
	}
	if (filtros.disponibles) {
		resultado['disponibles'] = 'true';
	}
	if (filtros.orden !== 'RECIENTES') {
		resultado['orden'] = filtros.orden;
	}
	if (filtros.pagina > 0) {
		resultado['pagina'] = String(filtros.pagina);
	}
	return resultado;
}
