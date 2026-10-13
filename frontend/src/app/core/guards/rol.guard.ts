import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Rol } from '../modelos/usuario';
import { SesionService } from '../sesion/sesion.service';

// La ruta declara el rol que necesita, { data: { rol: 'ADMINISTRADOR' } }, o
// los roles que pueden entrar, { data: { roles: ['EMPLEADO', 'CONTADOR'] } }.
// Igual que sesionGuard, esto es usabilidad, no control de acceso.
export const rolGuard: CanActivateFn = (ruta, estado) => {
	const sesion = inject(SesionService);
	const router = inject(Router);

	if (!sesion.autenticado()) {
		return router.createUrlTree(['/inicio-sesion'], {
			queryParams: { destino: estado.url },
		});
	}

	// Con sesión pero sin el rol, vuelve al inicio: no es una sesión caída
	const uno = ruta.data['rol'] as Rol | undefined;
	const permitidos = (ruta.data['roles'] as readonly Rol[] | undefined) ?? (uno === undefined ? undefined : [uno]);
	const rol = sesion.rol();
	return permitidos === undefined || (rol !== null && permitidos.includes(rol)) ? true : router.createUrlTree(['/']);
};
