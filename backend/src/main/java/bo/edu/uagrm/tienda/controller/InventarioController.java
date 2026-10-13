package bo.edu.uagrm.tienda.controller;

import java.util.List;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import bo.edu.uagrm.tienda.dto.AjusteRequest;
import bo.edu.uagrm.tienda.dto.CategoriaResponse;
import bo.edu.uagrm.tienda.dto.EntradaRequest;
import bo.edu.uagrm.tienda.dto.InventarioResponse;
import bo.edu.uagrm.tienda.dto.MinimoRequest;
import bo.edu.uagrm.tienda.dto.MovimientoResponse;
import bo.edu.uagrm.tienda.dto.PaginaResponse;
import bo.edu.uagrm.tienda.service.CategoriaService;
import bo.edu.uagrm.tienda.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// HU-08. Leen el Empleado, el Administrador y el Contador; registran solo los dos primeros (SecurityConfig). No hay
// forma de editar ni borrar un movimiento (RF-12)
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

	private final InventarioService inventarioService;
	private final CategoriaService categoriaService;

	@GetMapping
	public PaginaResponse<InventarioResponse> listar(@RequestParam(required = false) String texto,
			@RequestParam(required = false) Long categoria, @RequestParam(required = false) Boolean activo,
			@RequestParam(defaultValue = "false") boolean bajo, @RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "20") int tamano) {
		return PaginaResponse.from(inventarioService.listar(texto, categoria, activo, bajo, pagina, tamano),
				Function.identity());
	}

	// Para el filtro: todas, también las ocultas, porque el inventario lista sus productos. La gestión de categorías
	// es solo del Administrador
	@GetMapping("/categorias")
	public List<CategoriaResponse> categorias() {
		return categoriaService.todas().stream().map(CategoriaResponse::from).toList();
	}

	@GetMapping("/{idProducto}")
	public InventarioResponse obtener(@PathVariable Long idProducto) {
		return inventarioService.obtener(idProducto);
	}

	@GetMapping("/{idProducto}/movimientos")
	public PaginaResponse<MovimientoResponse> historial(@PathVariable Long idProducto,
			@RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "20") int tamano) {
		return PaginaResponse.from(inventarioService.historial(idProducto, pagina, tamano), Function.identity());
	}

	@PostMapping("/{idProducto}/entradas")
	@ResponseStatus(HttpStatus.CREATED)
	public InventarioResponse registrarEntrada(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idProducto,
			@Valid @RequestBody EntradaRequest solicitud) {
		return inventarioService.registrarEntrada(idProducto, idUsuario, solicitud);
	}

	@PostMapping("/{idProducto}/ajustes")
	@ResponseStatus(HttpStatus.CREATED)
	public InventarioResponse registrarAjuste(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idProducto,
			@Valid @RequestBody AjusteRequest solicitud) {
		return inventarioService.registrarAjuste(idProducto, idUsuario, solicitud);
	}

	@PutMapping("/{idProducto}/minimo")
	public InventarioResponse cambiarMinimo(@PathVariable Long idProducto, @Valid @RequestBody MinimoRequest solicitud) {
		return inventarioService.cambiarMinimo(idProducto, solicitud);
	}
}
