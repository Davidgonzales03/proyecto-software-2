import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Galeria } from './galeria';

const FOTOS = ['/fotos/a.webp', '/fotos/b.webp', '/fotos/c.webp'];

// HU-10: las fotos del producto y su pantalla completa
describe('Galeria', () => {
  let fijo: ComponentFixture<Galeria>;

  async function abrir(fotos: string[] = FOTOS) {
    TestBed.resetTestingModule();
    fijo = TestBed.createComponent(Galeria);
    fijo.componentRef.setInput('fotos', fotos);
    fijo.componentRef.setInput('nombre', 'Taladro');
    fijo.autoDetectChanges();
    await fijo.whenStable();
  }

  function raiz(): HTMLElement {
    return fijo.nativeElement as HTMLElement;
  }

  function grande(): HTMLButtonElement {
    return raiz().querySelector('.foto-grande') as HTMLButtonElement;
  }

  function fotoGrande(): string | null | undefined {
    return grande().querySelector('img')?.getAttribute('src');
  }

  function miniaturas(): HTMLButtonElement[] {
    return Array.from(raiz().querySelectorAll('.miniaturas button'));
  }

  function dialogo(): HTMLDialogElement {
    return raiz().querySelector('dialog') as HTMLDialogElement;
  }

  function fotoAmpliada(): string | null | undefined {
    return dialogo().querySelector('.ampliada img')?.getAttribute('src');
  }

  function boton(clase: string): HTMLButtonElement {
    return dialogo().querySelector(`.${clase}`) as HTMLButtonElement;
  }

  async function tecla(key: string) {
    dialogo().dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true }));
    await fijo.whenStable();
  }

  async function ampliar() {
    grande().click();
    await fijo.whenStable();
  }

  // HU-10 RF-5
  it('muestra la principal y las miniaturas', async () => {
    await abrir();

    expect(fotoGrande()).toBe('/fotos/a.webp');
    expect(grande().querySelector('img')?.getAttribute('alt')).toBe('Taladro, foto 1 de 3');
    expect(miniaturas()).toHaveLength(3);
    expect(miniaturas()[0].getAttribute('aria-pressed')).toBe('true');
    expect(miniaturas()[1].getAttribute('aria-label')).toBe('Ver la foto 2 de 3');
  });

  // HU-10 RF-5
  it('elegir una miniatura la muestra grande', async () => {
    await abrir();

    miniaturas()[2].click();
    await fijo.whenStable();

    expect(fotoGrande()).toBe('/fotos/c.webp');
    expect(miniaturas()[2].getAttribute('aria-pressed')).toBe('true');
    expect(miniaturas()[0].getAttribute('aria-pressed')).toBe('false');
  });

  // HU-10 RF-5: sin fotos o con una que no carga, el ícono, también en pantalla completa
  it('sin fotos o con una foto rota muestra el ícono', async () => {
    await abrir([]);
    expect(raiz().querySelector('img')).toBeNull();
    expect(raiz().querySelector('.icono-foto')).not.toBeNull();
    expect(raiz().querySelector('.foto-grande')).toBeNull();

    await abrir();
    grande().querySelector('img')?.dispatchEvent(new Event('error'));
    await fijo.whenStable();
    expect(grande().querySelector('img')).toBeNull();
    expect(grande().querySelector('.icono-foto')).not.toBeNull();

    await ampliar();
    expect(dialogo().querySelector('.ampliada img')).toBeNull();
    expect(dialogo().querySelector('.ampliada .icono-foto')).not.toBeNull();

    // La siguiente sí carga
    boton('siguiente').click();
    await fijo.whenStable();
    expect(fotoAmpliada()).toBe('/fotos/b.webp');
  });

  // HU-10 RF-5, RF-6
  it('con una sola foto no hay miniaturas ni flechas', async () => {
    await abrir(['/fotos/a.webp']);

    expect(miniaturas()).toHaveLength(0);
    await ampliar();
    expect(boton('anterior')).toBeNull();
    expect(boton('siguiente')).toBeNull();
  });

  // HU-10 RF-6
  it('abre a pantalla completa y pasa con flechas y teclado', async () => {
    await abrir();
    miniaturas()[1].click();
    await fijo.whenStable();

    await ampliar();
    expect(dialogo().hasAttribute('open')).toBe(true);
    expect(fotoAmpliada()).toBe('/fotos/b.webp');
    // showModal() da el foco al primer botón, «Cerrar» (jsdom no lo aplica: se
    // verifica en el navegador)
    expect(dialogo().querySelector('button')).toBe(boton('cerrar'));

    boton('siguiente').click();
    await fijo.whenStable();
    expect(fotoAmpliada()).toBe('/fotos/c.webp');

    await tecla('ArrowLeft');
    await tecla('ArrowLeft');
    expect(fotoAmpliada()).toBe('/fotos/a.webp');
    await tecla('ArrowRight');
    expect(fotoAmpliada()).toBe('/fotos/b.webp');
    expect(dialogo().textContent).toContain('2 / 3');
    expect(dialogo().querySelector('.posicion .solo-lectores')?.textContent?.trim()).toBe('Foto 2 de 3');
  });

  // HU-10 RF-6: se detiene en la primera y en la última
  it('no da la vuelta en los extremos', async () => {
    await abrir();
    await ampliar();

    expect(boton('anterior').getAttribute('aria-disabled')).toBe('true');
    boton('anterior').click();
    await tecla('ArrowLeft');
    expect(fotoAmpliada()).toBe('/fotos/a.webp');

    await tecla('ArrowRight');
    await tecla('ArrowRight');
    await tecla('ArrowRight');
    expect(fotoAmpliada()).toBe('/fotos/c.webp');
    expect(boton('siguiente').getAttribute('aria-disabled')).toBe('true');
    expect(boton('anterior').getAttribute('aria-disabled')).toBe('false');
  });

  // HU-10 RF-6
  it('se cierra y devuelve el foco a la foto, que queda en la que se miraba', async () => {
    await abrir();
    await ampliar();
    await tecla('ArrowRight');

    boton('cerrar').click();
    await fijo.whenStable();

    expect(dialogo().hasAttribute('open')).toBe(false);
    expect(document.activeElement).toBe(grande());
    expect(fotoGrande()).toBe('/fotos/b.webp');
  });

  // HU-10 RF-6
  it('deslizar con el dedo pasa de foto', async () => {
    await abrir();
    await ampliar();
    const ampliada = dialogo().querySelector('.ampliada') as HTMLElement;

    const dedo = (tipo: string, clientX: number, pointerId = 1, isPrimary = true) =>
      ampliada.dispatchEvent(Object.assign(new Event(tipo, { bubbles: true }), { clientX, pointerId, isPrimary }));
    const deslizar = async (desde: number, hasta: number) => {
      dedo('pointerdown', desde);
      dedo('pointerup', hasta);
      await fijo.whenStable();
    };

    await deslizar(300, 200);
    expect(fotoAmpliada()).toBe('/fotos/b.webp');
    await deslizar(200, 300);
    expect(fotoAmpliada()).toBe('/fotos/a.webp');
    // Un toque corto no es un deslizamiento
    await deslizar(200, 180);
    expect(fotoAmpliada()).toBe('/fotos/a.webp');

    // Un segundo dedo (un pellizco) no pasa de foto
    dedo('pointerdown', 300);
    dedo('pointerdown', 100, 2, false);
    dedo('pointerup', 50, 2, false);
    dedo('pointercancel', 300);
    dedo('pointerup', 100);
    await fijo.whenStable();
    expect(fotoAmpliada()).toBe('/fotos/a.webp');

    // El clic que cierra un deslizamiento no cierra la pantalla completa
    await deslizar(300, 200);
    ampliada.click();
    await fijo.whenStable();
    expect(dialogo().hasAttribute('open')).toBe(true);

    // Revisión de HU-10: en el celular un deslizamiento no deja clic; el
    // toque siguiente afuera sí cierra
    await deslizar(300, 200);
    const controles = dialogo().querySelector('.controles') as HTMLElement;
    controles.dispatchEvent(Object.assign(new Event('pointerdown', { bubbles: true }), { pointerId: 2, isPrimary: true }));
    controles.click();
    await fijo.whenStable();
    expect(dialogo().hasAttribute('open')).toBe(false);
  });

  // HU-10 RF-6: lo oscuro alrededor de la foto es el fondo; la foto y los botones no cierran
  it('tocar el fondo cierra', async () => {
    for (const fondo of ['.ampliada', '.controles', 'dialog']) {
      await abrir();
      await ampliar();

      (dialogo().querySelector('.ampliada img') as HTMLElement).click();
      boton('siguiente').click();
      await fijo.whenStable();
      expect(dialogo().hasAttribute('open')).toBe(true);

      (raiz().querySelector(fondo) as HTMLElement).click();
      await fijo.whenStable();
      expect(dialogo().hasAttribute('open')).toBe(false);
    }
  });

  // HU-10 RF-6: Escape lo cierra el navegador (evento close)
  it('con Escape se cierra y el foco vuelve a la foto', async () => {
    await abrir();
    await ampliar();

    dialogo().close();
    await fijo.whenStable();

    expect(document.activeElement).toBe(grande());
  });

  // HU-10 RF-5: al pasar a otro producto no queda la foto del anterior
  it('al cambiar las fotos vuelve a la primera', async () => {
    await abrir();
    miniaturas()[2].click();
    await fijo.whenStable();

    fijo.componentRef.setInput('fotos', ['/fotos/x.webp', '/fotos/y.webp']);
    await fijo.whenStable();

    expect(fotoGrande()).toBe('/fotos/x.webp');
    expect(miniaturas()[0].getAttribute('aria-pressed')).toBe('true');
  });
});
