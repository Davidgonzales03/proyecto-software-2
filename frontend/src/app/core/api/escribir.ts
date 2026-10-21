import { firstValueFrom, Observable } from 'rxjs';

import { comoErrorApi } from '../modelos/error-api';

// Una escritura con HttpClient como promesa: si el backend rechaza, falla con el
// ErrorApi ya armado, con el código que la pantalla traduce
export async function escribir<T>(pedido: Observable<T>): Promise<T> {
	try {
		return await firstValueFrom(pedido);
	} catch (error) {
		throw comoErrorApi(error);
	}
}
