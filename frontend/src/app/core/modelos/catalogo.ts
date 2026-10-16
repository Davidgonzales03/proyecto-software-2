import { DatosDeTarjeta } from '../../shared/tarjeta-producto/tarjeta-producto';
import { UnidadVenta } from './producto';

// Espejo de ProductoTarjetaResponse, PortadaResponse y de los parámetros de
// GET /api/productos (HU-09). La API nunca dice cuánto stock hay: solo si hay

export type OrdenCatalogo = 'RECIENTES' | 'PRECIO_ASC' | 'PRECIO_DESC' | 'NOMBRE';

export const ORDENES: readonly OrdenCatalogo[] = ['RECIENTES', 'PRECIO_ASC', 'PRECIO_DESC', 'NOMBRE'];

export const NOMBRES_DE_ORDEN: Record<OrdenCatalogo, string> = {
	RECIENTES: 'Más recientes',
	PRECIO_ASC: 'Precio: menor a mayor',
	PRECIO_DESC: 'Precio: mayor a menor',
	NOMBRE: 'Nombre (A-Z)',
};

// El mismo límite que CatalogoService.BUSQUEDA_MAXIMA
export const BUSQUEDA_MAXIMA = 100;

export interface ProductoEnTienda {
	idProducto: number;
	nombre: string;
	marca: string | null;
	precio: number;
	unidad: UnidadVenta;
	imagenPrincipal: string | null;
	disponible: boolean;
}

export interface Seccion {
	idCategoria: number;
	nombre: string;
	productos: ProductoEnTienda[];
}

// Una lista vacía es una sección que no se muestra
export interface Portada {
	destacados: ProductoEnTienda[];
	recientes: ProductoEnTienda[];
	categorias: Seccion[];
}

// null es «todos»; la página empieza en 0, como en el backend
export interface FiltrosDeCatalogo {
	q: string;
	categoria: number | null;
	marca: string | null;
	precioMin: number | null;
	precioMax: number | null;
	disponibles: boolean;
	orden: OrdenCatalogo;
	pagina: number;
}

export const SIN_FILTROS: FiltrosDeCatalogo = {
	q: '',
	categoria: null,
	marca: null,
	precioMin: null,
	precioMax: null,
	disponibles: false,
	orden: 'RECIENTES',
	pagina: 0,
};

// La ruta del detalle (HU-10)
export function rutaDelProducto(idProducto: number): string {
	return `/producto/${idProducto}`;
}

// Lo que pinta la tarjeta de shared/. «Unidad» no se escribe junto al precio
export function aTarjeta(producto: ProductoEnTienda): DatosDeTarjeta {
	return {
		nombre: producto.nombre,
		marca: producto.marca,
		precio: producto.precio,
		unidad: producto.unidad === 'UNIDAD' ? null : producto.unidad.toLowerCase(),
		imagen:
			producto.imagenPrincipal === null
				? null
				: `/api/productos/${producto.idProducto}/imagenes/${producto.imagenPrincipal}`,
		agotado: !producto.disponible,
		enlace: rutaDelProducto(producto.idProducto),
	};
}
