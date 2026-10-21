import { bytesUtf8 } from './reglas';

describe('bytesUtf8', () => {
  it('cuenta un byte por carácter ASCII', () => {
    expect(bytesUtf8('secreta12')).toBe(9);
  });

  // El backend limita la contraseña por bytes, no por caracteres: BCrypt corta
  // a los 72 bytes
  it('cuenta los caracteres multibyte por lo que ocupan', () => {
    expect(bytesUtf8('ñ')).toBe(2);
    expect(bytesUtf8('á')).toBe(2);
    expect(bytesUtf8('🎉')).toBe(4);
  });

  it('acepta el borde de 72 bytes y detecta el de 73', () => {
    expect(bytesUtf8('a'.repeat(72))).toBe(72);
    expect(bytesUtf8('a'.repeat(73))).toBe(73);
    expect(bytesUtf8(`${'a'.repeat(70)}ñ`)).toBe(72);
    expect(bytesUtf8(`${'a'.repeat(71)}ñ`)).toBe(73);
  });

  it('una cadena vacía no ocupa nada', () => {
    expect(bytesUtf8('')).toBe(0);
  });
});
