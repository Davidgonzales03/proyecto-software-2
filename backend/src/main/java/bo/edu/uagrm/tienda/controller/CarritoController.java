package bo.edu.uagrm.tienda.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.edu.uagrm.tienda.dto.CantidadRequest;
import bo.edu.uagrm.tienda.dto.CarritoResponse;
import bo.edu.uagrm.tienda.dto.LineaCarritoRequest;
import bo.edu.uagrm.tienda.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// El carrito del Cliente que inició sesión (HU-11). Solo el rol Cliente llega acá (SecurityConfig, RF-7), y el cliente
// sale del token, nunca de la petición. Cada operación devuelve el carrito entero
@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
public class CarritoController {

	private final CarritoService carritoService;

	@GetMapping
	public CarritoResponse ver(@AuthenticationPrincipal Long idUsuario) {
		return carritoService.ver(idUsuario);
	}

	@PostMapping("/lineas")
	public CarritoResponse agregar(@AuthenticationPrincipal Long idUsuario,
			@Valid @RequestBody LineaCarritoRequest solicitud) {
		return carritoService.agregar(idUsuario, solicitud);
	}

	@PutMapping("/lineas/{idProducto}")
	public CarritoResponse cambiarCantidad(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idProducto,
			@Valid @RequestBody CantidadRequest solicitud) {
		return carritoService.cambiarCantidad(idUsuario, idProducto, solicitud);
	}

	@DeleteMapping("/lineas/{idProducto}")
	public CarritoResponse quitar(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idProducto) {
		return carritoService.quitar(idUsuario, idProducto);
	}

	@DeleteMapping
	public CarritoResponse vaciar(@AuthenticationPrincipal Long idUsuario) {
		return carritoService.vaciar(idUsuario);
	}
}
