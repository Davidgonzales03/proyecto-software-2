import { HttpErrorResponse } from '@angular/common/http';
import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { vi } from 'vitest';

import { PedidosService } from '../../core/api/pedidos.service';
import { Pedido } from '../../core/modelos/pedido';
import { MisPedidosPage } from './mis-pedidos.page';

@Component({ template: '' })
class PantallaVacia {}

const PEDIDO_PENDIENTE: Pedido = {
  idPedido: 12,
  estado: 'PENDIENTE_PAGO',
  lineas: [
    { idProducto: 7, nombre: 'Martillo', unidad: 'UNIDAD', precio: 85.5, cantidad: 2, subtotal: 171 },
  ],
  total: 171,
  nombreRetira: 'Ana Paz',
  telefonoRetira: '70000000',
  nota: null,
  confirmado: '2026-10-21T10:00:00',
  vence: '2026-10-21T10:30:00',
  segundosParaPagar: 1790,
  pagado: null,
  cobro: null,
  retiro: { direccion: 'Av. Principal 123', horario: null },
};

const PEDIDO_PAGADO: Pedido = {
  idPedido: 11,
  estado: 'PAGADO',
  lineas: [
    { idProducto: 9, nombre: 'Cable', unidad: 'METRO', precio: 12, cantidad: 5, subtotal: 60 },
  ],
  total: 60,
  nombreRetira: 'Ana Paz',
  telefonoRetira: '70000000',
  nota: null,
  confirmado: '2026-10-20T15:00:00',
  vence: '2026-10-20T15:30:00',
  segundosParaPagar: 0,
  pagado: '2026-10-20T15:10:00',
  cobro: null,
  retiro: { direccion: 'Av. Principal 123', horario: null },
};

class PedidosFalso {
  readonly recurso = {
    value: signal<Pedido[] | undefined>(undefined),
    error: signal<unknown>(undefined),
    isLoading: signal<boolean>(false),
    reload: vi.fn(() => true),
    hasValue(): boolean {
      return this.value() !== undefined;
    },
  };

  misPedidos() {
    return this.recurso;
  }
}

describe('MisPedidosPage', () => {
  let harness: RouterTestingHarness;
  let pedidos: PedidosFalso;

  beforeEach(async () => {
    pedidos = new PedidosFalso();
    TestBed.configureTestingModule({
      imports: [MisPedidosPage],
      providers: [
        provideRouter([
          { path: 'mis-pedidos', component: MisPedidosPage },
          { path: 'mis-pedidos/:id', component: PantallaVacia },
          { path: 'catalogo', component: PantallaVacia },
        ]),
        { provide: PedidosService, useValue: pedidos },
      ],
    });
    harness = await RouterTestingHarness.create();
  });

  // HU-14 RF-1, RF-2: muestra los pedidos con sus datos principales
  it('muestra la lista de pedidos con número, fecha, estado y total', async () => {
    pedidos.recurso.value.set([PEDIDO_PENDIENTE, PEDIDO_PAGADO]);
    await harness.navigateByUrl('/mis-pedidos');

    const tarjetas = harness.routeNativeElement?.querySelectorAll('.tarjeta-pedido');
    expect(tarjetas?.length).toBe(2);

    expect(tarjetas?.[0].textContent).toContain('Pedido N.º 12');
    expect(tarjetas?.[0].textContent).toContain('Pendiente de pago');
    expect(tarjetas?.[0].textContent).toContain('Martillo');
    expect(tarjetas?.[0].textContent).toContain('171');

    expect(tarjetas?.[1].textContent).toContain('Pedido N.º 11');
    expect(tarjetas?.[1].textContent).toContain('Pagado');
    expect(tarjetas?.[1].textContent).toContain('Cable');
    expect(tarjetas?.[1].textContent).toContain('60');
  });

  // HU-14 RF-3: pedido pendiente de pago muestra aviso de tiempo y botón «Pagar ahora»
  it('el pedido pendiente de pago muestra aviso de tiempo para pagar y botón pagar', async () => {
    pedidos.recurso.value.set([PEDIDO_PENDIENTE]);
    await harness.navigateByUrl('/mis-pedidos');

    const aviso = harness.routeNativeElement?.querySelector('.aviso-pendiente');
    expect(aviso?.textContent).toContain('Tiene');
    expect(aviso?.textContent).toContain('para completar el pago');

    const botonPagar = harness.routeNativeElement?.querySelector('a.boton-pagar');
    expect(botonPagar?.textContent).toContain('Pagar ahora');
    expect(botonPagar?.getAttribute('href')).toBe('/mis-pedidos/12');
  });

  // HU-14 RF-4: pedido finalizado o pagado tiene botón «Ver detalle» a /mis-pedidos/:id
  it('el pedido pagado tiene enlace para ver detalle', async () => {
    pedidos.recurso.value.set([PEDIDO_PAGADO]);
    await harness.navigateByUrl('/mis-pedidos');

    const botonDetalle = harness.routeNativeElement?.querySelector('a.secundario');
    expect(botonDetalle?.textContent).toContain('Ver detalle');
    expect(botonDetalle?.getAttribute('href')).toBe('/mis-pedidos/11');
  });

  // HU-14 RF-8: si no tiene pedidos, muestra estado vacío y enlace a catálogo
  it('sin pedidos muestra mensaje de estado vacío y enlace a catálogo', async () => {
    pedidos.recurso.value.set([]);
    await harness.navigateByUrl('/mis-pedidos');

    expect(harness.routeNativeElement?.textContent).toContain('Aún no tiene pedidos realizados');
    const enlaceCatalogo = harness.routeNativeElement?.querySelector('a[href="/catalogo"]');
    expect(enlaceCatalogo).toBeTruthy();
    expect(enlaceCatalogo?.textContent).toContain('Explorar catálogo');
  });

  // Manejo de error de carga
  it('muestra aviso de error y botón de reintento si falla la carga', async () => {
    pedidos.recurso.error.set(new HttpErrorResponse({ status: 500, statusText: 'Internal Server Error' }));
    await harness.navigateByUrl('/mis-pedidos');

    expect(harness.routeNativeElement?.querySelector('.tarjeta-error')).toBeTruthy();
    const botonReintentar = harness.routeNativeElement?.querySelector<HTMLButtonElement>('.tarjeta-error button');
    expect(botonReintentar).toBeTruthy();
    botonReintentar?.click();
    expect(pedidos.recurso.reload).toHaveBeenCalled();
  });
});
