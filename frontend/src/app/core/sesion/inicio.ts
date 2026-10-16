import { Rol } from '../modelos/usuario';

// El inicio de cada uno (HU-09 RF-16): para el personal, su panel; para el
// cliente y para quien no tiene sesión, la tienda
export function inicioDe(rol: Rol | null): string {
	return rol === 'ADMINISTRADOR' || rol === 'EMPLEADO' || rol === 'CONTADOR' ? '/panel' : '/';
}
