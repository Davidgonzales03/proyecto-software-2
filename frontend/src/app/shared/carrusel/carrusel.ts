import {
	ChangeDetectionStrategy,
	Component,
	DestroyRef,
	ElementRef,
	afterNextRender,
	inject,
	input,
	signal,
	viewChild,
} from '@angular/core';
import { Params, RouterLink } from '@angular/router';
import { LucideArrowRight, LucideChevronLeft, LucideChevronRight } from '@lucide/angular';

let siguienteId = 0;

// Lo que avanza una flecha: casi una pantalla, para que la última tarjeta
// visible siga a la vista y se sepa dónde se estaba
const AVANCE = 0.9;

// Una fila de tarjetas que se recorre a pedido (HU-09 RF-5): flechas, rueda o
// trackpad, teclado y arrastre con el dedo. No avanza sola. El freno en el
// borde de cada tarjeta lo pone el CSS (scroll-snap); las flechas solo
// aparecen si algo queda afuera y se desactivan en los extremos
@Component({
	selector: 'app-carrusel',
	imports: [RouterLink, LucideArrowRight, LucideChevronLeft, LucideChevronRight],
	templateUrl: './carrusel.html',
	styleUrl: './carrusel.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Carrusel {
	readonly titulo = input.required<string>();
	// «Ver todo» lleva al catálogo con el filtro de esta sección
	readonly enlace = input<string | null>(null);
	readonly parametros = input<Params>({});

	private readonly pista = viewChild.required<ElementRef<HTMLElement>>('pista');

	protected readonly id = `carrusel-${siguienteId++}`;
	protected readonly desborda = signal(false);
	protected readonly alInicio = signal(true);
	protected readonly alFinal = signal(true);

	constructor() {
		const destruccion = inject(DestroyRef);
		afterNextRender(() => {
			this.medir();
			// Cambia el ancho de la pantalla o entran las fotos: se vuelve a medir
			if (typeof ResizeObserver !== 'undefined') {
				const observador = new ResizeObserver(() => this.medir());
				observador.observe(this.pista().nativeElement);
				destruccion.onDestroy(() => observador.disconnect());
			}
		});
	}

	protected medir(): void {
		const pista = this.pista().nativeElement;
		const sobra = pista.scrollWidth - pista.clientWidth;
		this.desborda.set(sobra > 1);
		this.alInicio.set(pista.scrollLeft <= 1);
		this.alFinal.set(pista.scrollLeft >= sobra - 1);
	}

	// Desactivada con aria-disabled y no con disabled: así el foco no se pierde
	// cuando la flecha llega al extremo con el teclado
	protected avanzar(sentido: 1 | -1): void {
		if (sentido === 1 ? this.alFinal() : this.alInicio()) {
			return;
		}
		const pista = this.pista().nativeElement;
		const reducido = typeof matchMedia === 'function' && matchMedia('(prefers-reduced-motion: reduce)').matches;
		pista.scrollBy({ left: sentido * Math.round(pista.clientWidth * AVANCE), behavior: reducido ? 'auto' : 'smooth' });
	}
}
