package bo.edu.uagrm.tienda.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.edu.uagrm.tienda.service.ProductoService;
import lombok.RequiredArgsConstructor;

// Lo que lee el catálogo, con o sin sesión (SecurityConfig). En HU-07, solo las imágenes; la lista y el detalle
// llegan con HU-09 y HU-10
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductosController {

	private final ProductoService productoService;

	@GetMapping("/{idProducto}/imagenes/{imagen}")
	public ResponseEntity<byte[]> imagen(@PathVariable Long idProducto, @PathVariable String imagen) {
		return productoService.imagen(idProducto, imagen)
				.map(guardada -> ResponseEntity.ok()
						.contentType(MediaType.parseMediaType(guardada.formato().tipoDeContenido()))
						.body(guardada.contenido()))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}
}
