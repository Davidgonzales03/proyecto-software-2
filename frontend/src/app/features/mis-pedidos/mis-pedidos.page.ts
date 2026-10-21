import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideArrowRight, LucideClock, LucideShoppingBag } from '@lucide/angular';

import { mensajeDeError } from '../../core/api/mensajes-error';
import { PedidosService } from '../../core/api/pedidos.service';
import { comoErrorApi } from '../../core/modelos/error-api';
import { NOMBRES_DE_ESTADO } from '../../core/modelos/pedido';
import { fechaCorta, horaCorta } from '../../shared/formato/fechas';
import { cantidad, precio } from '../../shared/formato/precios';

// Consulta y seguimiento de pedidos del Cliente (HU-14).
// Lista todos los pedidos del usuario ordenados del más reciente al más antiguo.
@Component({
	selector: 'app-mis-pedidos',
	imports: [RouterLink, LucideClock, LucideShoppingBag, LucideArrowRight],
	templateUrl: './mis-pedidos.page.html',
	styleUrl: './mis-pedidos.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MisPedidosPage {
	private readonly pedidosService = inject(PedidosService);

	protected readonly recurso = this.pedidosService.misPedidos();
	protected readonly pedidos = computed(() => this.recurso.value() ?? []);
	protected readonly cargando = computed(() => this.recurso.isLoading() && !this.recurso.hasValue());
	protected readonly error = computed(() => {
		const err = this.recurso.error();
		return err ? mensajeDeError(comoErrorApi(err)) : null;
	});

	protected readonly nombresDeEstado = NOMBRES_DE_ESTADO;
	protected readonly precio = precio;
	protected readonly cantidad = cantidad;
	protected readonly fecha = fechaCorta;
	protected readonly hora = (iso: string) => horaCorta(iso.slice(11));

	protected tiempoRestante(segundos: number): string {
		const mins = Math.floor(segundos / 60);
		const segs = segundos % 60;
		return `${mins}m ${segs < 10 ? '0' : ''}${segs}s`;
	}
}
