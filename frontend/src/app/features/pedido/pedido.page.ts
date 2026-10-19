import { ChangeDetectionStrategy, Component, computed, DestroyRef, effect, inject, linkedSignal, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideClock, LucideMapPin, LucideStore } from '@lucide/angular';

import { comoErrorApi } from '../../core/api/autenticacion.service';
import { mensajeDeError } from '../../core/api/mensajes-error';
import { PedidosService } from '../../core/api/pedidos.service';
import { comoError } from '../../core/modelos/error-api';
import { LineaDePedido, NOMBRES_DE_ESTADO } from '../../core/modelos/pedido';
import { DialogoConfirmacion } from '../../shared/dialogo-confirmacion/dialogo-confirmacion';
import { horaCorta } from '../../shared/formato/fechas';
import { cantidad, precio } from '../../shared/formato/precios';
import { idDeLaUrl } from '../../shared/navegacion/id-de-la-url';

// El pedido del Cliente (HU-12): lo que se confirmó, quién lo retira y dónde,
// y mientras espera el pago, el tiempo que queda y «Cancelar pedido». El
// tiempo lo cuenta el servidor; la página solo lo descuenta
@Component({
	selector: 'app-pedido',
	imports: [RouterLink, DialogoConfirmacion, LucideClock, LucideMapPin, LucideStore],
	templateUrl: './pedido.page.html',
	styleUrl: './pedido.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PedidoPage {
	private readonly pedidos = inject(PedidosService);
	private readonly dialogo = viewChild.required(DialogoConfirmacion);

	private readonly parametros = toSignal(inject(ActivatedRoute).paramMap, { requireSync: true });
	// Lo que no es un número de pedido no llega a la API (RF-13)
	protected readonly idPedido = computed(() => idDeLaUrl(this.parametros().get('id')));

	protected readonly recurso = this.pedidos.pedido(this.idPedido);
	protected readonly pedido = computed(() => (this.recurso.hasValue() ? this.recurso.value() : undefined));
	private readonly estadoDeError = computed(() => {
		const error = this.recurso.error();
		return error === undefined ? null : comoErrorApi(error);
	});
	// De otro cliente o inexistente responde igual (RF-13)
	protected readonly noEncontrado = computed(() => this.idPedido() === null || this.estadoDeError()?.status === 404);
	protected readonly errorDeCarga = computed(() => {
		const error = this.estadoDeError();
		return error === null || error.status === 404 ? '' : mensajeDeError(error);
	});

	// --- El tiempo para pagar (RF-8, RF-10) ---

	// Cuándo llegó la respuesta, en el reloj de este dispositivo. El contador
	// parte de esa misma hora y la adelanta cada segundo
	private readonly recibido = linkedSignal({ source: this.pedido, computation: () => Date.now() });
	private readonly ahora = linkedSignal({ source: this.recibido, computation: (recibido) => recibido });
	private readonly limite = computed(() => {
		const pedido = this.pedido();
		return pedido?.estado === 'PENDIENTE_PAGO' ? this.recibido() + pedido.segundosParaPagar * 1000 : null;
	});
	protected readonly segundos = computed(() => {
		const limite = this.limite();
		return limite === null ? null : Math.max(0, Math.ceil((limite - this.ahora()) / 1000));
	});
	protected readonly reloj = computed(() => {
		const segundos = this.segundos() ?? 0;
		return `${dosCifras(Math.floor(segundos / 60))}:${dosCifras(segundos % 60)}`;
	});
	// Cuándo se pidió de nuevo por última vez al llegar a cero
	private ultimoPedido = Number.NEGATIVE_INFINITY;

	// --- Cancelar (RF-9, RF-16) ---

	protected readonly cancelando = signal(false);
	protected readonly aviso = signal('');
	protected readonly noVolvieron = signal(0);

	protected readonly nombresDeEstado = NOMBRES_DE_ESTADO;
	protected readonly precio = precio;
	protected readonly cantidad = cantidad;

	constructor() {
		const intervalo = setInterval(() => this.ahora.set(Date.now()), 1000);
		inject(DestroyRef).onDestroy(() => clearInterval(intervalo));
		// Al llegar a cero se pide otra vez: el servidor lo vence en el acto y responde «Vencido».
		// Si le faltaba menos de un segundo y todavía responde «Pendiente», se reintenta cada segundo
		effect(() => {
			const ahora = this.ahora();
			if (this.segundos() === 0 && ahora - this.ultimoPedido >= 1000) {
				this.ultimoPedido = ahora;
				this.recurso.reload();
			}
		});
	}

	protected hora(fechaHora: string): string {
		return horaCorta(fechaHora.slice(11));
	}

	// «Unidad» no se escribe, como en el carrito
	protected unidad(linea: LineaDePedido): string {
		return linea.unidad === 'UNIDAD' ? '' : ` / ${linea.unidad.toLowerCase()}`;
	}

	protected async cancelar(): Promise<void> {
		const pedido = this.pedido();
		if (pedido === undefined || this.cancelando()) {
			return;
		}
		const confirmado = await this.dialogo().abrir({
			titulo: '¿Cancelar el pedido?',
			mensaje: 'Los productos dejan de estar reservados y vuelven a su carrito.',
			confirmar: 'Cancelar pedido',
			desistir: 'Volver',
		});
		if (!confirmado) {
			return;
		}
		this.aviso.set('');
		this.cancelando.set(true);
		try {
			const cancelacion = await this.pedidos.cancelar(pedido.idPedido);
			this.recurso.value.set(cancelacion.pedido);
			this.noVolvieron.set(cancelacion.lineasQueNoVolvieron);
		} catch (rechazo: unknown) {
			const error = comoError(rechazo);
			this.aviso.set(mensajeDeError(error));
			// Se canceló o venció en otra pestaña: se muestra como está ahora (RF-12)
			if (error.codigo === 'PEDIDO_NO_PENDIENTE') {
				this.recurso.reload();
			}
		} finally {
			this.cancelando.set(false);
		}
	}
}

function dosCifras(numero: number): string {
	return `${numero}`.padStart(2, '0');
}
