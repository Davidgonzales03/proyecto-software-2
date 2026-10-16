import { HttpErrorResponse } from '@angular/common/http';
import { effect, signal, Signal, untracked } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { vi } from 'vitest';

import { CatalogoService } from '../../core/api/catalogo.service';
import { ProductoEnDetalle, ProductoEnTienda } from '../../core/modelos/catalogo';
import { Rol } from '../../core/modelos/usuario';
import { SesionService } from '../../core/sesion/sesion.service';
import { DetallePage } from './detalle.page';

function tarjeta(idProducto: number, nombre: string): ProductoEnTienda {
  return { idProducto, nombre, marca: null, precio: 85, unidad: 'UNIDAD', imagenPrincipal: null, disponible: true };
}

// Como responde GET /api/productos/7 (CatalogoIntegracionTest)
const TALADRO: ProductoEnDetalle = {
  idProducto: 7,
  codigo: 'HER-0007',
  nombre: 'Taladro percutor 750 W',
  marca: 'Bosch',
  idCategoria: 3,
  categoria: 'Herramientas',
  precio: 420,
  unidad: 'UNIDAD',
  descripcion: 'Para concreto y madera.\nCon maletín.',
  ficha: [
    { nombre: 'Potencia', valor: '750 W' },
    { nombre: 'Mandril', valor: '13 mm' },
  ],
  imagenes: ['a.webp', 'b.webp'],
  disponible: true,
  relacionados: [tarjeta(8, 'Martillo')],
};

const MARTILLO: ProductoEnDetalle = {
  ...TALADRO,
  idProducto: 8,
  codigo: 'HER-0008',
  nombre: 'Martillo',
  imagenes: [],
  relacionados: [tarjeta(7, 'Taladro percutor 750 W')],
};

class RecursoFalso<T> {
  readonly value = signal<T | undefined>(undefined);
  readonly isLoading = signal(false);
  readonly error = signal<unknown>(undefined);
  readonly reload = vi.fn(() => true);
  hasValue(): boolean {
    return this.value() !== undefined;
  }
}

class CatalogoFalso {
  readonly recurso = new RecursoFalso<ProductoEnDetalle>();
  id: Signal<number | null> | null = null;
  pedidos = 0;
  // Contesta según el id, como la API
  respuestas = new Map<number, ProductoEnDetalle>([
    [7, TALADRO],
    [8, MARTILLO],
  ]);
  // Como httpResource: con otro id, el valor y el error anteriores se van
  detalle(id: Signal<number | null>) {
    this.id = id;
    effect(() => {
      id();
      untracked(() => {
        this.recurso.value.set(undefined);
        this.recurso.error.set(undefined);
      });
    });
    return this.recurso;
  }
  responder(): void {
    const id = this.id?.() ?? null;
    if (id !== null) {
      this.pedidos++;
      this.recurso.value.set(this.respuestas.get(id));
    }
  }
}

// HU-10: el detalle del producto
describe('DetallePage', () => {
  let harness: RouterTestingHarness;
  let catalogo: CatalogoFalso;

  async function abrir(url = '/producto/7', rol: Rol | null = null, responder = true) {
    TestBed.resetTestingModule();
    catalogo = new CatalogoFalso();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          { path: 'producto/:id', component: DetallePage },
          { path: 'catalogo', children: [] },
          { path: 'productos/:id', children: [] },
        ]),
        { provide: CatalogoService, useValue: catalogo },
        { provide: SesionService, useValue: { rol: signal(rol) } },
      ],
    });
    harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    harness.fixture.autoDetectChanges();
    if (responder) {
      catalogo.responder();
    }
    await harness.fixture.whenStable();
  }

  function pantalla(): HTMLElement {
    return harness.routeNativeElement as HTMLElement;
  }

  function texto(): string {
    return (pantalla().textContent ?? '').replace(/\s+/g, ' ');
  }

  function enlace(textoDelEnlace: string): HTMLAnchorElement | undefined {
    return Array.from(pantalla().querySelectorAll('a')).find((cada) => cada.textContent?.trim() === textoDelEnlace);
  }

  // HU-10 RF-1: sin guard, para cualquiera
  it('abre sin sesión', async () => {
    await abrir();

    expect(pantalla().querySelector('h1')?.textContent?.trim()).toBe('Taladro percutor 750 W');
  });

  // Revisión de HU-10: al entrar, el foco no se mueve (solo al pasar de un producto a otro)
  it('al entrar no mueve el foco', async () => {
    await abrir();

    expect(document.activeElement).not.toBe(pantalla().querySelector('h1'));
  });

  // HU-10 RF-2
  it('muestra los datos del producto', async () => {
    await abrir();

    expect(texto()).toContain('Bosch');
    expect(texto()).toContain('HER-0007');
    expect(texto()).toContain('Herramientas');
    expect(texto()).toContain('Bs 420,00');
    expect(pantalla().querySelector('.descripcion')?.textContent).toContain('Para concreto y madera.\nCon maletín.');
    const ficha = Array.from(pantalla().querySelectorAll('.ficha dt, .ficha dd')).map((cada) => cada.textContent?.trim());
    expect(ficha).toEqual(['Potencia', '750 W', 'Mandril', '13 mm']);
    const fotos = Array.from(pantalla().querySelectorAll('app-galeria .miniaturas img')).map((cada) =>
      cada.getAttribute('src'),
    );
    expect(fotos).toEqual(['/api/productos/7/imagenes/a.webp', '/api/productos/7/imagenes/b.webp']);
  });

  // HU-10 RF-2: el precio va con su unidad cuando no es «unidad»
  it('el precio lleva la unidad', async () => {
    await abrir('/producto/7', null, false);
    catalogo.recurso.value.set({ ...TALADRO, precio: 12.5, unidad: 'METRO' });
    await harness.fixture.whenStable();

    expect(texto()).toContain('Bs 12,50 / metro');
  });

  // HU-10 RF-2
  it('sin marca, descripción ni ficha esas partes no salen', async () => {
    await abrir('/producto/7', null, false);
    catalogo.recurso.value.set({ ...TALADRO, marca: null, descripcion: null, ficha: [] });
    await harness.fixture.whenStable();

    expect(pantalla().querySelector('.datos .marca')).toBeNull();
    expect(pantalla().querySelector('.descripcion')).toBeNull();
    expect(pantalla().querySelector('.ficha')).toBeNull();
    expect(texto()).toContain('HER-0007');
  });

  // HU-10 RF-3
  it('la categoría lleva al catálogo filtrado', async () => {
    await abrir();

    enlace('Herramientas')?.click();
    await harness.fixture.whenStable();

    expect(TestBed.inject(Router).url).toBe('/catalogo?categoria=3');
  });

  // HU-10 RF-4: nunca la cantidad
  it('dice si está disponible o agotado, sin la cantidad', async () => {
    await abrir();
    expect(pantalla().querySelector('.disponibilidad')?.textContent?.trim()).toBe('Disponible');

    catalogo.recurso.value.set({ ...TALADRO, disponible: false });
    await harness.fixture.whenStable();
    const marca = pantalla().querySelector('.disponibilidad');
    expect(marca?.textContent?.trim()).toBe('Agotado');
    expect(marca?.querySelector('svg')).not.toBeNull();
    expect(texto()).not.toMatch(/stock/i);
  });

  // HU-10 RF-7
  it('muestra «Más de» la categoría', async () => {
    await abrir();

    const carrusel = pantalla().querySelector('app-carrusel');
    expect(carrusel?.querySelector('h2')?.textContent?.trim()).toBe('Más de Herramientas');
    expect(carrusel?.textContent).toContain('Martillo');
  });

  // HU-10 RF-7
  it('sin relacionados no sale el carrusel', async () => {
    await abrir('/producto/7', null, false);
    catalogo.recurso.value.set({ ...TALADRO, relacionados: [] });
    await harness.fixture.whenStable();

    expect(pantalla().querySelector('app-carrusel')).toBeNull();
  });

  // HU-10 RF-8: inactivo, oculto o inexistente responden igual
  it('un producto que no está disponible lo dice', async () => {
    await abrir('/producto/7', null, false);
    catalogo.recurso.error.set(new HttpErrorResponse({ status: 404 }));
    await harness.fixture.whenStable();

    expect(texto()).toContain('Este producto no está disponible');
    expect(enlace('Ver el catálogo')?.getAttribute('href')).toBe('/catalogo');
    expect(pantalla().querySelector('button')).toBeNull();
  });

  // HU-10 RF-9: el aviso de RF-8, sin consultar la API
  it('una dirección basura no consulta la API', async () => {
    for (const basura of ['abc', '-1', '0', '1.5', '99999999999999999999']) {
      await abrir(`/producto/${basura}`);

      expect(catalogo.id?.()).toBeNull();
      expect(catalogo.pedidos).toBe(0);
      expect(texto()).toContain('Este producto no está disponible');
    }
  });

  // HU-10 RF-10: usabilidad; el permiso lo valida el backend
  it('«Editar producto» solo para el Administrador', async () => {
    await abrir('/producto/7', 'ADMINISTRADOR');
    expect(enlace('Editar producto')?.getAttribute('href')).toBe('/productos/7');

    for (const rol of [null, 'CLIENTE', 'EMPLEADO', 'CONTADOR'] as const) {
      await abrir('/producto/7', rol);
      expect(enlace('Editar producto')).toBeUndefined();
    }
  });

  // HU-10 RF-11
  it('pone el nombre en la pestaña', async () => {
    await abrir();

    expect(TestBed.inject(Title).getTitle()).toBe('Taladro percutor 750 W · Tienda Montero');
  });

  // Caso límite: un error que no es 404 se puede reintentar
  it('un error de red ofrece reintentar', async () => {
    await abrir('/producto/7', null, false);
    catalogo.recurso.error.set(new HttpErrorResponse({ status: 0 }));
    await harness.fixture.whenStable();

    expect(texto()).not.toContain('Este producto no está disponible');
    const reintentar = Array.from(pantalla().querySelectorAll('button')).find(
      (cada) => cada.textContent?.trim() === 'Reintentar',
    );
    reintentar?.click();
    expect(catalogo.recurso.reload).toHaveBeenCalled();
  });

  // Caso límite: de un producto a otro, desde «Más de…»
  it('pasar a otro producto carga el nuevo y lleva el foco al título', async () => {
    await abrir();

    pantalla().querySelector<HTMLAnchorElement>('app-tarjeta-producto a')?.click();
    await harness.fixture.whenStable();
    // Mientras llega el nuevo, no queda el anterior
    expect(texto()).toContain('Cargando el producto');
    expect(pantalla().querySelector('h1')).toBeNull();
    catalogo.responder();
    await harness.fixture.whenStable();

    expect(catalogo.id?.()).toBe(8);
    const titulo = pantalla().querySelector('h1');
    expect(titulo?.textContent?.trim()).toBe('Martillo');
    expect(document.activeElement).toBe(titulo);
    expect(pantalla().querySelector('app-galeria .miniaturas')).toBeNull();
  });
});
