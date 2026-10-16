import {
	ChangeDetectionStrategy,
	Component,
	computed,
	ElementRef,
	input,
	linkedSignal,
	viewChild,
} from '@angular/core';
import { LucideChevronLeft, LucideChevronRight, LucideWrench, LucideX } from '@lucide/angular';

// Lo que hay que deslizar con el dedo para pasar de foto; menos es un toque
const DESLIZAMIENTO = 40;

// Las fotos de un producto (HU-10 RF-5, RF-6): la elegida grande, miniaturas
// para cambiar y pantalla completa en un <dialog> con showModal(), que atrapa
// el foco y se cierra con Escape. No da la vuelta: se detiene en la primera y
// en la última. Recibe las direcciones ya armadas: shared/ no conoce la API
@Component({
	selector: 'app-galeria',
	imports: [LucideChevronLeft, LucideChevronRight, LucideWrench, LucideX],
	templateUrl: './galeria.html',
	styleUrl: './galeria.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Galeria {
	readonly fotos = input.required<string[]>();
	// Para el texto alternativo: «Taladro, foto 1 de 3»
	readonly nombre = input.required<string>();

	private readonly dialogo = viewChild<ElementRef<HTMLDialogElement>>('dialogo');
	private readonly grande = viewChild<ElementRef<HTMLButtonElement>>('grande');
	private readonly botonCerrar = viewChild<ElementRef<HTMLButtonElement>>('botonCerrar');

	// Con otras fotos (otro producto), vuelve a la primera y olvida las rotas
	protected readonly actual = linkedSignal({ source: this.fotos, computation: () => 0 });
	protected readonly rotas = linkedSignal<string[], ReadonlySet<number>>({
		source: this.fotos,
		computation: () => new Set<number>(),
	});

	protected readonly total = computed(() => this.fotos().length);
	protected readonly alInicio = computed(() => this.actual() === 0);
	protected readonly alFinal = computed(() => this.actual() >= this.total() - 1);

	// El dedo (o el puntero) que está deslizando; con otro dedo, no cuenta
	private inicioDelDedo: { id: number; x: number } | null = null;
	// Un deslizamiento termina con un clic: no es tocar el fondo
	private recienDeslizo = false;

	protected alt(indice: number): string {
		return `${this.nombre()}, foto ${indice + 1} de ${this.total()}`;
	}

	protected rota(indice: number): boolean {
		return this.rotas().has(indice);
	}

	protected marcarRota(indice: number): void {
		this.rotas.update((rotas) => new Set(rotas).add(indice));
	}

	protected elegir(indice: number): void {
		this.actual.set(indice);
	}

	protected ampliar(): void {
		this.dialogo()?.nativeElement.showModal();
		// El foco va a «Cerrar»: la flecha anterior empieza desactivada
		this.botonCerrar()?.nativeElement.focus();
	}

	protected cerrar(): void {
		this.dialogo()?.nativeElement.close();
	}

	// Por cualquier cierre (botón, fondo o Escape): el foco vuelve a la foto
	protected alCerrar(): void {
		this.grande()?.nativeElement.focus();
	}

	protected anterior(): void {
		if (!this.alInicio()) {
			this.actual.update((indice) => indice - 1);
		}
	}

	protected siguiente(): void {
		if (!this.alFinal()) {
			this.actual.update((indice) => indice + 1);
		}
	}

	protected alTeclear(evento: KeyboardEvent): void {
		if (evento.key === 'ArrowLeft') {
			evento.preventDefault();
			this.anterior();
		} else if (evento.key === 'ArrowRight') {
			evento.preventDefault();
			this.siguiente();
		}
	}

	// Lo oscuro alrededor de la foto es el fondo: todo lo que no es la foto ni
	// un botón cierra
	protected alTocar(evento: MouseEvent): void {
		if (this.recienDeslizo) {
			this.recienDeslizo = false;
			return;
		}
		const destino = evento.target;
		if (destino instanceof Element && destino.closest('img, button, .icono-foto') === null) {
			this.cerrar();
		}
	}

	protected alApoyar(evento: PointerEvent): void {
		if (evento.isPrimary === false) {
			return;
		}
		this.recienDeslizo = false;
		this.inicioDelDedo = { id: evento.pointerId, x: evento.clientX };
		// El final llega aunque el dedo salga de la foto
		const zona = evento.currentTarget;
		if (zona instanceof Element && typeof zona.setPointerCapture === 'function' && evento.pointerId !== undefined) {
			zona.setPointerCapture(evento.pointerId);
		}
	}

	protected alSoltar(evento: PointerEvent): void {
		if (this.inicioDelDedo === null || evento.pointerId !== this.inicioDelDedo.id) {
			return;
		}
		const recorrido = evento.clientX - this.inicioDelDedo.x;
		this.inicioDelDedo = null;
		if (Math.abs(recorrido) < DESLIZAMIENTO) {
			return;
		}
		this.recienDeslizo = true;
		if (recorrido < 0) {
			this.siguiente();
		} else {
			this.anterior();
		}
	}

	protected alCancelar(): void {
		this.inicioDelDedo = null;
	}
}
