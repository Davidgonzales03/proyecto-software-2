import { HttpErrorResponse } from '@angular/common/http';
import { Signal, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { vi } from 'vitest';

import { PedidosService } from '../../core/api/pedidos.service';
import { Cancelacion, Pedido } from '../../core/modelos/pedido';
import { PedidoPage } from './pedido.page';

// Como responde GET /api/pedidos/12 (PedidoIntegracionTest)
const PENDIENTE: Pedido = {
  idPedido: 12,
  estado: 'PENDIENTE_PAGO',
  lineas: [
    { idProducto: 7, nombre: 'Martillo', unidad: 'UNIDAD', precio: 85.5, cantidad: 2, subtotal: 171 },
    { idProducto: 9, nombre: 'Cable', unidad: 'METRO', precio: 12, cantidad: 3.5, subtotal: 42 },
  ],
  total: 213,
  nombreRetira: 'Luis Rojas',
  telefonoRetira: '71234567',
  nota: 'Retira mi hermano',
  confirmado: '2026-10-19T10:00:00',
  vence: '2026-10-19T10:30:00',
  segundosParaPagar: 1795,
  pagado: null,
  cobro: null,
  retiro: { direccion: 'Av. Principal 123, Montero', horario: null },
};

class PedidosFalso {
  id: Signal<number | null> | null = null;
  readonly recurso = {
    value: signal<Pedido | undefined>(undefined),
    error: signal<unknown>(undefined),
    reload: vi.fn(() => true),
    hasValue(): boolean {
      return this.value() !== undefined;
    },
  };
  readonly cancelar = vi.fn(
    (_id: number): Promise<Cancelacion> =>
      Promise.resolve({ pedido: { ...PENDIENTE, estado: 'CANCELADO', segundosParaPagar: 0 }, lineasQueNoVolvieron: 0 }),
  );
  pedido(id: Signal<number | null>) {
    this.id = id;
    return this.recurso;
  }
}

// HU-12: la página del pedido del Cliente
describe('PedidoPage', () => {
  let harness: RouterTestingHarness;
  let pedidos: PedidosFalso;

  async function abrir(url = '/mis-pedidos/12', configurar: (falso: PedidosFalso) => void = () => {}): Promise<void> {
    TestBed.resetTestingModule();
    pedidos = new PedidosFalso();
    pedidos.recurso.value.set(PENDIENTE);
    configurar(pedidos);
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'mis-pedidos/:id', component: PedidoPage },
          { path: 'carrito', children: [] },
        ]),
        { provide: PedidosService, useValue: pedidos },
      ],
    });
    harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    harness.fixture.autoDetectChanges();
    await harness.fixture.whenStable();
  }

  afterEach(() => vi.useRealTimers());

  function pantalla(): HTMLElement {
    return harness.routeNativeElement as HTMLElement;
  }

  // El formato de precios separa «Bs» con un espacio duro
  function texto(): string {
    return (pantalla().textContent ?? '').replace(/\s+/g, ' ');
  }

  function botonCancelar(): HTMLButtonElement | undefined {
    return Array.from(pantalla().querySelectorAll<HTMLButtonElement>('button')).find(
      (cada) => cada.textContent?.trim() === 'Cancelar pedido' && cada.closest('dialog') === null,
    );
  }

  const dialogo = () => pantalla().querySelector('dialog')!;

  async function cancelarConfirmando(): Promise<void> {
    botonCancelar()!.click();
    await harness.fixture.whenStable();
    (dialogo().querySelector('button.principal') as HTMLButtonElement).click();
    // La respuesta del diálogo y la del servicio son dos promesas seguidas
    await harness.fixture.whenStable();
    await harness.fixture.whenStable();
    // El foco llega después de dibujar el resultado
    await new Promise((listo) => setTimeout(listo));
    await harness.fixture.whenStable();
  }

  // HU-12 RF-8, RF-17
  it('muestra el pedido y el tiempo para pagar', async () => {
    await abrir();

    expect(pedidos.id!()).toBe(12);
    expect(texto()).toContain('Pedido N.º 12');
    expect(texto()).toContain('Pendiente de pago');
    expect(texto()).toContain('29:55');
    expect(texto()).toContain('10:30');
    expect(texto()).toContain('Martillo');
    expect(texto()).toContain('3,5 × Bs 12,00 / metro');
    expect(texto()).toContain('Bs 213,00');
    expect(texto()).toContain('Luis Rojas');
    expect(texto()).toContain('71234567');
    expect(texto()).toContain('Retira mi hermano');
    expect(texto()).toContain('Av. Principal 123, Montero');
    expect(texto()).not.toContain('Horario');
    // El contador no se anuncia cada segundo
    expect(pantalla().querySelector('[role="timer"]')?.getAttribute('aria-live')).toBe('off');
  });

  // HU-12 RF-9
  it('cancelar pide confirmación y deja el pedido cancelado', async () => {
    await abrir();

    botonCancelar()!.click();
    await harness.fixture.whenStable();
    expect(dialogo().hasAttribute('open')).toBe(true);
    (dialogo().querySelector('button.secundario') as HTMLButtonElement).click();
    await harness.fixture.whenStable();
    expect(pedidos.cancelar).not.toHaveBeenCalled();

    await cancelarConfirmando();

    expect(pedidos.cancelar).toHaveBeenCalledWith(12);
    expect(texto()).toContain('Cancelado');
    expect(texto()).toContain('Los productos volvieron a su carrito.');
    expect(pantalla().querySelector('a[href="/carrito"]')).not.toBeNull();
    expect(botonCancelar()).toBeUndefined();
    // Revisión de HU-12 (interfaz): el botón ya no está; el foco va al resultado
    expect(document.activeElement).toBe(pantalla().querySelector('.cerrado'));
  });

  // HU-12 RF-16
  it('avisa cuántas líneas no volvieron al carrito', async () => {
    await abrir();
    pedidos.cancelar.mockResolvedValueOnce({
      pedido: { ...PENDIENTE, estado: 'CANCELADO', segundosParaPagar: 0 },
      lineasQueNoVolvieron: 1,
    });

    await cancelarConfirmando();

    expect(texto()).toContain('Los demás productos volvieron a su carrito.');
    expect(texto()).toContain('1 producto no volvió porque su carrito ya tiene 50.');

    // Revisión de HU-12 (/code-review): otro pedido en la misma pantalla no hereda el aviso
    pedidos.recurso.value.set({ ...PENDIENTE, idPedido: 5, estado: 'VENCIDO', segundosParaPagar: 0 });
    await harness.navigateByUrl('/mis-pedidos/5');
    await harness.fixture.whenStable();
    expect(texto()).toContain('Pedido N.º 5');
    expect(texto()).not.toContain('no volvió');
    expect(texto()).toContain('Los productos volvieron a su carrito.');
  });

  // Revisión de HU-12 (/code-review): si no volvió ninguno, no dice «los demás»
  it('si no volvió ningún producto, no dice que volvieron', async () => {
    await abrir();
    pedidos.cancelar.mockResolvedValueOnce({
      pedido: { ...PENDIENTE, estado: 'CANCELADO', segundosParaPagar: 0 },
      lineasQueNoVolvieron: 2,
    });

    await cancelarConfirmando();

    expect(texto()).not.toContain('volvieron a su carrito');
    expect(texto()).toContain('2 productos no volvieron porque su carrito ya tiene 50.');
  });

  // Revisión de HU-12 (/code-review): la respuesta de una cancelación vieja no pisa otro pedido
  it('si ya se abrió otro pedido, la respuesta de la cancelación no lo pisa', async () => {
    await abrir();
    let responder!: (cancelacion: Cancelacion) => void;
    pedidos.cancelar.mockReturnValueOnce(new Promise((listo) => (responder = listo)));
    botonCancelar()!.click();
    await harness.fixture.whenStable();
    (dialogo().querySelector('button.principal') as HTMLButtonElement).click();
    await harness.fixture.whenStable();

    pedidos.recurso.value.set({ ...PENDIENTE, idPedido: 5 });
    await harness.navigateByUrl('/mis-pedidos/5');
    responder({ pedido: { ...PENDIENTE, estado: 'CANCELADO', segundosParaPagar: 0 }, lineasQueNoVolvieron: 1 });
    await harness.fixture.whenStable();
    await harness.fixture.whenStable();

    expect(texto()).toContain('Pedido N.º 5');
    expect(texto()).toContain('Pendiente de pago');
    expect(texto()).not.toContain('no volvió');
  });

  // HU-12 RF-12: se canceló o venció en otra pestaña
  it('si ya no está pendiente al cancelar, lo dice y pide el pedido de nuevo', async () => {
    await abrir();
    pedidos.cancelar.mockRejectedValueOnce({ status: 409, detail: '', codigo: 'PEDIDO_NO_PENDIENTE' });

    await cancelarConfirmando();

    expect(texto()).toContain('Este pedido ya no espera el pago.');
    expect(pedidos.recurso.reload).toHaveBeenCalled();
    expect(document.activeElement).toBe(pantalla().querySelector('.aviso'));
  });

  // HU-12 RF-10
  it('al llegar a cero pide el pedido otra vez', async () => {
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval', 'Date'] });
    await abrir('/mis-pedidos/12', (falso) => falso.recurso.value.set({ ...PENDIENTE, segundosParaPagar: 2 }));
    expect(texto()).toContain('00:02');

    vi.advanceTimersByTime(1000);
    await harness.fixture.whenStable();
    expect(texto()).toContain('00:01');
    expect(pedidos.recurso.reload).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1000);
    await harness.fixture.whenStable();
    expect(pedidos.recurso.reload).toHaveBeenCalledTimes(1);
    // Si todavía responde «Pendiente», reintenta una vez por segundo, no en cada vuelta
    vi.advanceTimersByTime(500);
    await harness.fixture.whenStable();
    expect(pedidos.recurso.reload).toHaveBeenCalledTimes(1);
  });

  // HU-12 RF-10, RF-12
  it('un pedido que no está pendiente no ofrece cancelar ni cuenta el tiempo', async () => {
    await abrir('/mis-pedidos/12', (falso) =>
      falso.recurso.value.set({ ...PENDIENTE, estado: 'VENCIDO', segundosParaPagar: 0 }),
    );

    expect(texto()).toContain('Vencido');
    expect(texto()).toContain('Los productos volvieron a su carrito.');
    expect(pantalla().querySelector('[role="timer"]')).toBeNull();
    expect(botonCancelar()).toBeUndefined();
  });

  // HU-12 RF-13
  it('el pedido de otro o que no existe dice que no se encontró', async () => {
    await abrir('/mis-pedidos/99', (falso) => {
      falso.recurso.value.set(undefined);
      falso.recurso.error.set(new HttpErrorResponse({ status: 404, error: { status: 404, detail: '', codigo: 'PEDIDO_NO_ENCONTRADO' } }));
    });

    expect(texto()).toContain('No encontramos ese pedido.');
    // Revisión de HU-12 (interfaz): con un camino para seguir
    expect(pantalla().querySelector('a[href="/carrito"]')).not.toBeNull();
  });

  // HU-12 RF-13 (caso límite: datos basura)
  it('una dirección que no es la de un pedido no consulta la API', async () => {
    await abrir('/mis-pedidos/abc', (falso) => falso.recurso.value.set(undefined));

    expect(pedidos.id!()).toBeNull();
    expect(texto()).toContain('No encontramos ese pedido.');
  });
});
