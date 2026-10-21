import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideCircleCheck } from '@lucide/angular';

import { AutenticacionService } from '../../core/api/autenticacion.service';
import { mensajeDeError } from '../../core/api/mensajes-error';
import { comoError } from '../../core/modelos/error-api';
import { SesionService } from '../../core/sesion/sesion.service';

@Component({
	selector: 'app-verificar-correo',
	imports: [RouterLink, LucideCircleCheck],
	templateUrl: './verificar-correo.page.html',
	styleUrl: './verificar-correo.page.scss',
	changeDetection: ChangeDetectionStrategy.OnPush,
})
export class VerificarCorreoPage implements OnInit {
	private readonly autenticacion = inject(AutenticacionService);
	private readonly sesion = inject(SesionService);
	private readonly ruta = inject(ActivatedRoute);

	protected readonly cargando = signal(true);
	protected readonly verificado = signal(false);
	protected readonly error = signal<string | null>(null);

	protected readonly reenviando = signal(false);
	protected readonly reenvioExitoso = signal(false);
	protected readonly errorReenvio = signal<string | null>(null);

	protected readonly autenticado = this.sesion.autenticado;

	async ngOnInit(): Promise<void> {
		this.cargando.set(true);
		this.verificado.set(false);
		this.error.set(null);

		const token = (this.ruta.snapshot.queryParamMap.get('token') ?? '').trim();
		if (token === '') {
			this.cargando.set(false);
			this.error.set('El enlace de verificación no es válido o no contiene un token.');
			return;
		}

		try {
			await this.autenticacion.verificarCorreo(token);
			this.verificado.set(true);
			const usuario = this.sesion.usuario();
			if (usuario !== null) {
				this.sesion.actualizarUsuario({ ...usuario, correoVerificado: true });
			}
		} catch (err: unknown) {
			this.error.set(mensajeDeError(comoError(err)));
		} finally {
			this.cargando.set(false);
		}
	}

	protected async reenviarVerificacion(): Promise<void> {
		if (this.reenviando()) {
			return;
		}
		this.reenviando.set(true);
		this.reenvioExitoso.set(false);
		this.errorReenvio.set(null);
		try {
			await this.autenticacion.reenviarVerificacion();
			this.reenvioExitoso.set(true);
		} catch (err: unknown) {
			this.errorReenvio.set(mensajeDeError(comoError(err)));
		} finally {
			this.reenviando.set(false);
		}
	}
}
