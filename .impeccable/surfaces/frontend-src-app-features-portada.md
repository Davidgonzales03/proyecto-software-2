---
version: 1
slug: "frontend-src-app-features-portada"
primary_target: "frontend/src/app/features/portada"
related_targets: ["frontend/src/app/features/catalogo"]
---

# Tienda pública: portada (`/`) y catálogo (`/catalogo`)

Modo: persuadir. Público, tarea y reglas: `PRODUCT.md` y `docs/historias/HU-09.md`.
La acción principal es buscar y entrar a un producto; el carrito llega con HU-11.

## Direction contract

THESIS: La tienda se lee como el letrero de ruta de un micro cruceño: una ventana fija muestra una categoría por vez en letras amarillas enormes y las demás esperan en la cinta. Rechaza el banner con titular y la fila de tarjetas blancas iguales.

OWN-WORLD: Tres tintes: lona verde botella a sangre como fondo, amarillo cromo solo para las leyendas y la acción principal, lona clara para el texto corrido y las tarjetas. Una condensada en mayúsculas para leyendas; paneles y botones cortados en sesgo de 12° con el texto derecho adentro; esquinas rectas; costuras de un pelo amarillo entre paneles. El estado se marca con costura, troquel y relieve, no con otro color.

STORY: El visitante ve de inmediato que es una ferretería de Montero con herramienta real, encuentra la categoría o busca, y entra al producto.

FIRST VIEWPORT: Encabezado verde oscuro con la marca en amarillo y la búsqueda ancha en lona clara con «Buscar» amarillo. Debajo, la ventana: un marco negro a todo el ancho con el nombre de la sección que se está mirando en condensada de unos 140 px y su posición en el rollo («1 / 5») a la derecha; fijo bajo el encabezado, se achica al bajar. «Ver todo» va en cada carrusel (HU-09 RF-3). Bajo la ventana, la cinta de categorías en sesgo, la activa en lona clara. Abajo, el carrusel de esa categoría sobre una placa de lona clara, con el borde de la sección siguiente visible.

FORM: Letrero de ruta (depot destination blind), tercera carta del reparto audaz, elegida por el usuario. seed: none (unavailable)

Interacción distintiva: al elegir otra categoría en la cinta, la leyenda de la ventana avanza un paso entero (sube una línea, se pasa un poco y se asienta) y el carrusel cambia a esa categoría; con movimiento reducido cambia sin desplazarse. Un error de carga deja la ventana a medio paso, con dos medias leyendas en la costura.

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
