import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Carrusel } from './carrusel';

@Component({
  imports: [Carrusel],
  template: `
    <app-carrusel titulo="Jardín" enlace="/catalogo" [parametros]="{ categoria: 4 }">
      <div class="item">uno</div>
      <div class="item">dos</div>
    </app-carrusel>
  `,
})
class Anfitrion {}

// HU-09 RF-5: jsdom no desplaza de verdad, así que se fijan a mano las medidas de
// la pista. El arrastre, la rueda y el teclado se prueban en el navegador
describe('Carrusel', () => {
  let fijo: ComponentFixture<Anfitrion>;
  let desplazamientos: ScrollToOptions[];

  beforeEach(async () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    fijo = TestBed.createComponent(Anfitrion);
    fijo.autoDetectChanges();
    await fijo.whenStable();
    desplazamientos = [];
    pista().scrollBy = ((opciones: ScrollToOptions) => desplazamientos.push(opciones)) as typeof Element.prototype.scrollBy;
  });

  function pista(): HTMLElement {
    return fijo.nativeElement.querySelector('.pista');
  }

  function flecha(nombre: string): HTMLButtonElement | null {
    return fijo.nativeElement.querySelector(`button[aria-label="${nombre}"]`);
  }

  // Pone las medidas y avisa a la pista, como haría el navegador al desplazarse
  async function medidas(ancho: number, contenido: number, desplazado: number): Promise<void> {
    Object.defineProperty(pista(), 'clientWidth', { configurable: true, value: ancho });
    Object.defineProperty(pista(), 'scrollWidth', { configurable: true, value: contenido });
    pista().scrollLeft = desplazado;
    pista().dispatchEvent(new Event('scroll'));
    await fijo.whenStable();
  }

  it('muestra el título, el contenido y «Ver todo» con su filtro', () => {
    const verTodo: HTMLAnchorElement = fijo.nativeElement.querySelector('a.ver-todo');

    expect(fijo.nativeElement.querySelector('h2').textContent).toContain('Jardín');
    expect(fijo.nativeElement.querySelectorAll('.pista .item')).toHaveLength(2);
    expect(verTodo.getAttribute('href')).toBe('/catalogo?categoria=4');
  });

  // HU-09 RF-5
  it('flechas con nombre accesible', async () => {
    await medidas(400, 1200, 0);

    expect(flecha('Anteriores de Jardín')).not.toBeNull();
    expect(flecha('Siguientes de Jardín')).not.toBeNull();
  });

  // HU-09 RF-5
  it('sin flechas si todo entra', async () => {
    await medidas(1200, 1200, 0);

    expect(flecha('Anteriores de Jardín')).toBeNull();
    expect(flecha('Siguientes de Jardín')).toBeNull();
  });

  // HU-09 RF-5
  it('flechas desactivadas en los extremos', async () => {
    await medidas(400, 1200, 0);
    expect(flecha('Anteriores de Jardín')?.getAttribute('aria-disabled')).toBe('true');
    expect(flecha('Siguientes de Jardín')?.getAttribute('aria-disabled')).toBe('false');

    await medidas(400, 1200, 800);
    expect(flecha('Anteriores de Jardín')?.getAttribute('aria-disabled')).toBe('false');
    expect(flecha('Siguientes de Jardín')?.getAttribute('aria-disabled')).toBe('true');

    // En el extremo, la flecha no hace nada pero conserva el foco
    flecha('Siguientes de Jardín')?.click();
    expect(desplazamientos).toEqual([]);
  });

  // HU-09 RF-5: avanza casi una pantalla, y el freno en cada tarjeta lo pone el CSS
  it('una flecha avanza una pantalla', async () => {
    await medidas(400, 1200, 0);

    flecha('Siguientes de Jardín')?.click();
    await medidas(400, 1200, 400);
    flecha('Anteriores de Jardín')?.click();

    expect(desplazamientos.map((cada) => cada.left)).toEqual([360, -360]);
  });
});
