import {
	afterRenderEffect,
	ChangeDetectionStrategy,
	Component,
	computed,
	DestroyRef,
	ElementRef,
	inject,
	linkedSignal,
	signal,
	viewChild,
} from '@angular/core';
import { Params } from '@angular/router';

import { comoErrorApi } from '../../core/api/autenticacion.service';
import { CatalogoService } from '../../core/api/catalogo.service';
import { mensajeDeError } from '../../core/api/mensajes-error';
import { aTarjeta, ProductoEnTienda } from '../../core/modelos/catalogo';
import { Carrusel } from '../../shared/carrusel/carrusel';
import { DatosDeTarjeta, TarjetaProducto } from '../../shared/tarjeta-producto/tarjeta-producto';

interface Seccion {
	clave: string;
	titulo: string;
	// null: sin «Ver todo», porque el catálogo no filtra por destacados
	parametros: Params | null;
	tarjetas: DatosDeTarjeta[];
}

// La portada de la tienda (HU-09 RF-3): el letrero de ruta. La ventana queda
// fija arriba y muestra la sección que se está mirando; la cinta lleva a cada
// una. Debajo van todas las secciones en carrusel, en orden: Destacados,
// Recientes y una por categoría. Las vacías no salen
@Component({
	selector: 'app-portada',
	imports: [Carrusel, TarjetaProducto],
	templateUrl: './portada.page.html',
	styleUrl: './portada.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PortadaPage {
	protected readonly recurso = inject(CatalogoService).portada();
	private readonly anfitrion = inject(ElementRef<HTMLElement>);
	private readonly marcador = viewChild<ElementRef<HTMLElement>>('marcador');

	protected readonly secciones = computed<Seccion[]>(() => {
		const portada = this.recurso.hasValue() ? this.recurso.value() : undefined;
		if (portada === undefined) {
			return [];
		}
		return [
			seccion('destacados', 'Destacados', null, portada.destacados),
			seccion('recientes', 'Recientes', {}, portada.recientes),
			...portada.categorias.map((categoria) =>
				seccion(`categoria-${categoria.idCategoria}`, categoria.nombre, { categoria: categoria.idCategoria }, categoria.productos),
			),
		].filter((cada) => cada.tarjetas.length > 0);
	});

	protected readonly vacia = computed(() => this.recurso.hasValue() && this.secciones().length === 0);

	// La primera sección, hasta que se mire o se elija otra
	protected readonly activa = linkedSignal<Seccion[], string | null>({
		source: this.secciones,
		computation: (secciones, previo) =>
			previo?.value != null && secciones.some((cada) => cada.clave === previo.value)
				? previo.value
				: (secciones[0]?.clave ?? null),
	});
	private readonly seccionActiva = computed(() => this.secciones().find((cada) => cada.clave === this.activa()));
	protected readonly leyenda = computed(() => this.seccionActiva()?.titulo ?? '');
	// La posición en el rollo: «2 / 5»
	protected readonly posicion = computed(
		() => this.secciones().findIndex((cada) => cada.clave === this.activa()) + 1,
	);

	// Fijo arriba, el letrero se achica para dejar lugar a los productos
	protected readonly compacto = signal(false);

	protected readonly errorDeCarga = computed(() => {
		const error = this.recurso.error();
		return error === undefined ? '' : mensajeDeError(comoErrorApi(error));
	});

	// Mientras baja por un clic en la cinta, la lectura del scroll se pausa: la
	// leyenda da un solo paso, no uno por cada sección que cruza
	private pausadoHasta = 0;
	private finDelDesplazamiento: (() => void) | null = null;

	constructor() {
		inject(DestroyRef).onDestroy(() => this.dejarDeEsperarElFin());
		// La sección activa es la última cuya placa ya pasó por debajo del letrero
		// fijo; al llegar al final de la página, la última
		afterRenderEffect((limpiar) => {
			if (this.secciones().length === 0) {
				return;
			}
			const raiz = this.anfitrion.nativeElement as HTMLElement;
			let cuadro = 0;
			const leer = () => {
				cuadro = 0;
				if (Date.now() < this.pausadoHasta) {
					return;
				}
				// La misma línea donde deja una sección la cinta (scroll-margin-top de .placa)
				const letrero = raiz.querySelector('.letrero')?.getBoundingClientRect();
				const linea = (letrero?.bottom ?? 0) + 24;
				const placas = Array.from(raiz.querySelectorAll<HTMLElement>('.secciones > .placa'));
				const alFinal = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2;
				const activa = alFinal
					? placas[placas.length - 1]
					: placas.filter((placa) => placa.getBoundingClientRect().top <= linea).pop();
				if (activa !== undefined) {
					this.activa.set(activa.id);
				} else if (placas[0] !== undefined) {
					this.activa.set(placas[0].id);
				}
			};
			const alDesplazar = () => {
				if (cuadro === 0) {
					cuadro = requestAnimationFrame(leer);
				}
			};
			window.addEventListener('scroll', alDesplazar, { passive: true });

			// Lo que tapa el letrero fijo: el foco y las anclas quedan debajo
			// (scroll-padding-top en styles.scss)
			const letrero = raiz.querySelector<HTMLElement>('.letrero');
			// Al achicarse, el letrero deja su lugar ocupado con un margen: si la
			// página se acortara, el navegador corregiría el scroll, el marcador
			// volvería a verse y el letrero parpadearía entre los dos tamaños
			let altoGrande = 0;
			const medida =
				letrero === null || typeof ResizeObserver === 'undefined'
					? null
					: new ResizeObserver(() => {
							const alto = letrero.offsetHeight;
							if (letrero.classList.contains('compacto')) {
								letrero.style.marginBottom = `${Math.max(0, altoGrande - alto)}px`;
							} else {
								altoGrande = alto;
								letrero.style.marginBottom = '';
							}
							document.documentElement.style.setProperty('--app-alto-letrero', `${alto}px`);
						});
			if (medida !== null && letrero !== null) {
				medida.observe(letrero);
			}

			const marcador = this.marcador()?.nativeElement;
			const fijacion =
				typeof IntersectionObserver === 'undefined' || marcador === undefined
					? null
					: new IntersectionObserver(([entrada]) => this.compacto.set(!entrada.isIntersecting), {
							rootMargin: `-${altoDeLaCabecera()}px 0px 0px 0px`,
						});
			if (fijacion !== null && marcador !== undefined) {
				fijacion.observe(marcador);
			}
			limpiar(() => {
				window.removeEventListener('scroll', alDesplazar);
				cancelAnimationFrame(cuadro);
				fijacion?.disconnect();
				medida?.disconnect();
				document.documentElement.style.removeProperty('--app-alto-letrero');
			});
		});

		// En una cinta larga, la sección actual se trae a la vista, sin mover la página
		afterRenderEffect(() => {
			this.activa();
			const cinta = (this.anfitrion.nativeElement as HTMLElement).querySelector<HTMLElement>('.cinta');
			const actual = cinta?.querySelector<HTMLElement>('.actual');
			if (cinta && actual && typeof cinta.scrollTo === 'function') {
				cinta.scrollTo({ left: actual.offsetLeft - (cinta.clientWidth - actual.offsetWidth) / 2 });
			}
		});
	}

	// La leyenda avanza un paso y la página baja hasta esa sección
	protected elegir(clave: string): void {
		this.activa.set(clave);
		// Hasta que termine de bajar (scrollend), con un tope por si no llega a
		// desplazarse; donde no hay scrollend, 700 ms
		this.pausadoHasta = Date.now() + ('onscrollend' in window ? 3000 : 700);
		this.dejarDeEsperarElFin();
		this.finDelDesplazamiento = () => {
			this.pausadoHasta = 0;
			this.finDelDesplazamiento = null;
		};
		window.addEventListener('scrollend', this.finDelDesplazamiento, { once: true });
		const reducido = typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
		const placa = document.getElementById(clave);
		placa?.scrollIntoView({ behavior: reducido ? 'auto' : 'smooth', block: 'start' });
		// El foco va con la sección: el siguiente Tab sigue en su contenido
		placa?.focus({ preventScroll: true });
	}

	private dejarDeEsperarElFin(): void {
		if (this.finDelDesplazamiento !== null) {
			window.removeEventListener('scrollend', this.finDelDesplazamiento);
			this.finDelDesplazamiento = null;
		}
	}
}

function seccion(clave: string, titulo: string, parametros: Params | null, productos: ProductoEnTienda[]): Seccion {
	return { clave, titulo, parametros, tarjetas: productos.map(aTarjeta) };
}

// Lo publica el encabezado (AppComponent); 0 cuando no queda fijo
function altoDeLaCabecera(): number {
	const valor = getComputedStyle(document.documentElement).getPropertyValue('--app-alto-cabecera');
	return Math.round(Number.parseFloat(valor) || 0);
}
