import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { LucideMinus, LucidePlus } from '@lucide/angular';

// Los botones − y + a los lados del campo de la cantidad (HU-11 RF-15). El
// campo lo pone quien lo usa, y también decide el límite. Al llegar al límite
// el botón no se deshabilita: así no pierde el foco
@Component({
	selector: 'app-paso-de-cantidad',
	imports: [LucideMinus, LucidePlus],
	templateUrl: './paso-de-cantidad.html',
	styleUrl: './paso-de-cantidad.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasoDeCantidad {
	// El nombre del producto, cuando hay varios en la pantalla: «Sumar uno a Martillo»
	readonly de = input<string>();
	readonly puedeRestar = input.required<boolean>();
	readonly puedeSumar = input.required<boolean>();
	readonly restar = output<void>();
	readonly sumar = output<void>();

	protected etiqueta(accion: string): string {
		const de = this.de();
		return de === undefined ? accion : `${accion} a ${de}`;
	}

	protected tocarRestar(): void {
		if (this.puedeRestar()) {
			this.restar.emit();
		}
	}

	protected tocarSumar(): void {
		if (this.puedeSumar()) {
			this.sumar.emit();
		}
	}
}
