// La regla que el backend aplica y que @angular/forms/signals no trae de
// fábrica. Es una función pura para poder probarla sin formulario.

// La contraseña se limita por bytes porque así la limita BCrypt, y una ñ o un
// emoji ocupan más de uno.
export function bytesUtf8(valor: string): number {
	return new TextEncoder().encode(valor).length;
}
