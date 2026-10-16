import { ViewportScroller } from '@angular/common';
import { EnvironmentProviders, inject, makeEnvironmentProviders, provideEnvironmentInitializer } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';

// Dónde queda la página al navegar, para toda la app (revisión de HU-10):
// - otra pantalla (otra ruta, como de la portada a un producto): arriba;
// - la misma ruta con otros parámetros (los filtros del catálogo): no se mueve;
// - atrás y adelante: lo restaura el navegador, como siempre.
// No usa withInMemoryScrolling: con 'enabled' también subía al cambiar un filtro
export function provideDesplazamiento(): EnvironmentProviders {
	return makeEnvironmentProviders([
		provideEnvironmentInitializer(() => {
			const router = inject(Router);
			const ventana = inject(ViewportScroller);
			let rutaAnterior: string | null = null;
			let porHistorial = false;
			router.events.subscribe((evento) => {
				if (evento instanceof NavigationStart) {
					porHistorial = evento.navigationTrigger === 'popstate';
				} else if (evento instanceof NavigationEnd) {
					const ruta = sinParametros(evento.urlAfterRedirects);
					if (!porHistorial && rutaAnterior !== null && ruta !== rutaAnterior) {
						ventana.scrollToPosition([0, 0]);
					}
					rutaAnterior = ruta;
				}
			});
		}),
	]);
}

function sinParametros(url: string): string {
	return url.split(/[?#]/)[0];
}
