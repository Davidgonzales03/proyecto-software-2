import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { vi } from 'vitest';

import { AutenticacionService } from '../../core/api/autenticacion.service';
import { Usuario } from '../../core/modelos/usuario';
import { SesionService } from '../../core/sesion/sesion.service';
import { VerificarCorreoPage } from './verificar-correo.page';

const USUARIO_PRUEBA: Usuario = {
  idUsuario: 5,
  nombre: 'Carlos',
  apellido: 'Mendoza',
  correo: 'carlos@mail.com',
  telefono: '71234567',
  rol: 'CLIENTE',
  estado: 'ACTIVA',
  fechaRegistro: '2026-10-20T12:00:00',
  correoVerificado: false,
};

class AutenticacionFalsa {
  verificarCorreo = vi.fn((_token: string): Promise<void> => Promise.resolve());
  reenviarVerificacion = vi.fn((): Promise<void> => Promise.resolve());
}

describe('VerificarCorreoPage', () => {
  let fijo: ComponentFixture<VerificarCorreoPage>;
  let autenticacion: AutenticacionFalsa;
  let token: string | null;
  let usuarioActual: Usuario | null = null;
  let actualizarUsuarioFn = vi.fn();

  async function preparar() {
    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [VerificarCorreoPage],
      providers: [
        provideRouter([
          { path: 'catalogo', children: [] },
          { path: 'carrito', children: [] },
          { path: 'inicio-sesion', children: [] },
        ]),
        { provide: AutenticacionService, useValue: autenticacion },
        {
          provide: SesionService,
          useValue: {
            usuario: signal(usuarioActual),
            autenticado: signal(usuarioActual !== null),
            actualizarUsuario: actualizarUsuarioFn,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap(token === null ? {} : { token }),
            },
          },
        },
      ],
    }).compileComponents();

    fijo = TestBed.createComponent(VerificarCorreoPage);
    fijo.detectChanges();
    await fijo.whenStable();
    fijo.detectChanges();
  }

  function texto(): string {
    return (fijo.nativeElement.textContent ?? '').replace(/\s+/g, ' ');
  }

  beforeEach(() => {
    autenticacion = new AutenticacionFalsa();
    token = 'token-valido-123';
    usuarioActual = null;
    actualizarUsuarioFn = vi.fn();
  });

  // HU-20 RF-2
  it('verifica el correo con el token de la URL y muestra éxito', async () => {
    await preparar();

    expect(autenticacion.verificarCorreo).toHaveBeenCalledWith('token-valido-123');
    expect(texto()).toContain('¡Correo verificado con éxito!');
    expect(texto()).toContain('Ya puede realizar y confirmar pedidos');
  });

  // HU-20 RF-2
  it('actualiza la sesión activa del usuario a correo verificado si está autenticado', async () => {
    usuarioActual = USUARIO_PRUEBA;
    await preparar();

    expect(actualizarUsuarioFn).toHaveBeenCalledWith({
      ...USUARIO_PRUEBA,
      correoVerificado: true,
    });
  });

  // HU-20 RF-3
  it('muestra aviso si no hay token en la URL y no llama a la API', async () => {
    token = null;
    await preparar();

    expect(autenticacion.verificarCorreo).not.toHaveBeenCalled();
    expect(texto()).toContain('El enlace de verificación no es válido');
  });

  // HU-20 RF-3
  it('muestra mensaje de error cuando el token es inválido o venció', async () => {
    token = 'token-vencido';
    autenticacion.verificarCorreo.mockRejectedValue({
      status: 400,
      codigo: 'TOKEN_VERIFICACION_INVALIDO',
      detail: 'El enlace de verificación no es válido o ya fue utilizado',
    });
    await preparar();

    expect(texto()).toContain('Este enlace ya no sirve o ya fue utilizado');
  });

  // HU-20 RF-5
  it('permite reenviar el enlace si el usuario está autenticado', async () => {
    token = 'token-invalido';
    usuarioActual = USUARIO_PRUEBA;
    autenticacion.verificarCorreo.mockRejectedValue({
      status: 400,
      codigo: 'TOKEN_VERIFICACION_INVALIDO',
      detail: 'El enlace de verificación no es válido o ya fue utilizado',
    });
    await preparar();

    const boton = fijo.nativeElement.querySelector('button') as HTMLButtonElement;
    expect(boton).not.toBeNull();
    expect(boton.textContent).toContain('Reenviar correo de verificación');

    boton.click();
    await fijo.whenStable();
    fijo.detectChanges();

    expect(autenticacion.reenviarVerificacion).toHaveBeenCalled();
    expect(texto()).toContain('Hemos enviado un nuevo enlace de verificación');
  });

  // HU-20 RF-6
  it('muestra error cuando el reenvío supera el límite de solicitudes', async () => {
    token = 'token-invalido';
    usuarioActual = USUARIO_PRUEBA;
    autenticacion.verificarCorreo.mockRejectedValue({
      status: 400,
      codigo: 'TOKEN_VERIFICACION_INVALIDO',
      detail: 'El enlace de verificación no es válido o ya fue utilizado',
    });
    autenticacion.reenviarVerificacion.mockRejectedValue({
      status: 429,
      codigo: 'SOLICITUDES_EXCEDIDAS',
      detail: 'Ya pidió esto varias veces. Espere unos minutos y vuelva a intentar.',
    });
    await preparar();

    const boton = fijo.nativeElement.querySelector('button') as HTMLButtonElement;
    boton.click();
    await fijo.whenStable();
    fijo.detectChanges();

    expect(texto()).toContain('Ya pidió esto varias veces');
  });
});
