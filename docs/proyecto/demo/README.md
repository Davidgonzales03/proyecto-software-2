# Catálogo de Demostración (Ferretería)

Este catálogo proporciona los datos iniciales para la tienda en las revisiones
de sprint y en la defensa académica del proyecto (issue #25). Contiene 20
productos organizados en 5 categorías de ferretería, herramientas y hogar.

Los productos con modelo tridimensional utilizan activos gratuitos con
licencia **CC0** de [Poly Haven](https://polyhaven.com/models), preparados para
las historias de usuario HU-15 (subida de modelos 3D) y HU-16 (visor 3D y realidad
aumentada).

## Estructura de `catalogo.csv`

El archivo `catalogo.csv` contiene las siguientes columnas:

```
categoria,nombre,descripcion,precio_bs,stock_inicial,imagenes,modelo_3d,destacado,unidad
```

* **Sin comas internas:** Ningún campo (tampoco las descripciones) utiliza comas,
  garantizando un procesamiento robusto en el backend.
* **Precios:** En bolivianos (Bs.), con punto decimal.
* **Destacados:** Marcados con `si` para los productos que se muestran en el
  carrusel principal de la portada (hasta 12 productos).
* **Unidad:** Indica la unidad de medida (`caja`, `metro`, `litro`, `rollo`) o
  vacío cuando se comercializa por pieza individual.
* **Imágenes y modelos 3D:** Los archivos no se versionan en el repositorio para
  mantenerlo ligero (decisiones.md → Imágenes subidas); se cargan mediante la
  carpeta compartida o el directorio `TIENDA_DEMO_DIRECTORIO`.

---

## Fuentes de Modelos 3D y Fotografías

Todos los recursos provienen de repositorios abiertos bajo licencias libres
permisivas:

### Modelos 3D (Formato GLB)

| Archivo | Producto asociado | Fuente | Licencia |
|---|---|---|---|
| `martillo_curvo.glb` | Martillo de uña curva mango de fibra | [Poly Haven - Claw Hammer](https://polyhaven.com/a/claw_hammer) | CC0 (Dominio Público) |
| `llave_inglesa.glb` | Juego de llaves combinadas 8 a 19 mm | [Poly Haven - Combination Wrench](https://polyhaven.com/a/combination_wrench) | CC0 (Dominio Público) |
| `alicate_universal.glb` | Alicate universal aislado 8 pulgadas | [Poly Haven - Pliers](https://polyhaven.com/a/pliers) | CC0 (Dominio Público) |
| `destornillador.glb` | Juego de destornilladores magneticos 6 piezas | [Poly Haven - Screwdriver](https://polyhaven.com/a/screwdriver) | CC0 (Dominio Público) |
| `taladro_percutor.glb` | Taladro percutor 750W velocidad variable | [Poly Haven - Power Drill](https://polyhaven.com/a/power_drill) | CC0 (Dominio Público) |
| `regadera_jardin.glb` | Regadera plastica de jardin 10 litros | [Poly Haven - Watering Can](https://polyhaven.com/a/watering_can) | CC0 (Dominio Público) |
| `escalera_aluminio.glb` | Escalera de aluminio tijera 5 peldanos | [Poly Haven - Step Ladder](https://polyhaven.com/a/step_ladder) | CC0 (Dominio Público) |
| `carretilla_obra.glb` | Carretilla de construccion rueda neumatica 90 litros | [Poly Haven - Wheelbarrow](https://polyhaven.com/a/wheelbarrow) | CC0 (Dominio Público) |
| `pala_forjada.glb` | Pala de punta forjada con cabo de madera | [Poly Haven - Shovel](https://polyhaven.com/a/shovel) | CC0 (Dominio Público) |

### Fotografías de Productos (JPG / PNG / WebP hasta 2 MB)

| Archivos de Imagen | Producto | Fuente | Licencia |
|---|---|---|---|
| `martillo-una-curva-1.jpg`, `martillo-una-curva-2.jpg` | Martillo de uña curva mango de fibra | Unsplash | Licencia Unsplash (Libre) |
| `llaves-combinadas-1.jpg` | Juego de llaves combinadas 8 a 19 mm | Unsplash | Licencia Unsplash (Libre) |
| `alicate-universal-1.jpg` | Alicate universal aislado 8 pulgadas | Pexels | Licencia Pexels (Libre) |
| `juego-destornilladores-1.jpg` | Juego de destornilladores magneticos 6 piezas | Unsplash | Licencia Unsplash (Libre) |
| `cinta-metrica-5m-1.jpg` | Cinta metrica resistente a impactos 5 metros | Unsplash | Licencia Unsplash (Libre) |
| `taladro-percutor-750w-1.jpg`, `taladro-percutor-750w-2.jpg` | Taladro percutor 750W velocidad variable | Unsplash | Licencia Unsplash (Libre) |
| `amoladora-angular-850w-1.jpg` | Amoladora angular 4 y media pulgadas 850W | Pexels | Licencia Pexels (Libre) |
| `sierra-circular-1400w-1.jpg` | Sierra circular de mano 7 y un cuarto pulgadas 1400W | Unsplash | Licencia Unsplash (Libre) |
| `lijadora-orbital-1.jpg` | Lijadora orbital de palma 200W | Unsplash | Licencia Unsplash (Libre) |
| `regadera-jardin-10l-1.jpg` | Regadera plastica de jardin 10 litros | Pexels | Licencia Pexels (Libre) |
| `tijera-podar-bypass-1.jpg` | Tijera de podar profesional bypass 8 pulgadas | Unsplash | Licencia Unsplash (Libre) |
| `manguera-reforzada-20m-1.jpg` | Manguera reforzada tricapa media pulgada por 20 metros | Pexels | Licencia Pexels (Libre) |
| `escalera-aluminio-5p-1.jpg` | Escalera de aluminio tijera 5 peldanos | Unsplash | Licencia Unsplash (Libre) |
| `carretilla-construccion-90l-1.jpg` | Carretilla de construccion rueda neumatica 90 litros | Unsplash | Licencia Unsplash (Libre) |
| `pala-punta-forjada-1.jpg` | Pala de punta forjada con cabo de madera | Unsplash | Licencia Unsplash (Libre) |
| `tornillos-yeso-carton-1.jpg` | Caja de tornillos para yeso carton 6x1 pulgada mil unidades | Pexels | Licencia Pexels (Libre) |
| `cable-acero-galvanizado-1.jpg` | Cable de acero galvanizado un cuarto de pulgada por metro | Pexels | Licencia Pexels (Libre) |
| `pintura-latex-blanco-4l-1.jpg` | Pintura latex lavable para interior blanco 4 litros | Unsplash | Licencia Unsplash (Libre) |
| `rodillo-antigota-mango-1.jpg` | Rodillo antigota con mango telescopico extensible | Unsplash | Licencia Unsplash (Libre) |
| `silicona-transparente-280ml-1.jpg` | Silicona transparente multiuso cartucho 280 ml | Pexels | Licencia Pexels (Libre) |
