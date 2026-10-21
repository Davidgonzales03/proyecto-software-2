package bo.edu.uagrm.tienda.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bo.edu.uagrm.tienda.dto.PedidoResponse;
import bo.edu.uagrm.tienda.service.PagoService;
import lombok.RequiredArgsConstructor;

// Pagar el pedido (HU-13). Bajo /api/pedidos/**: solo el Cliente (SecurityConfig), y solo el dueño (PagoService). El id
// con el mismo patrón que PedidoController: lo que no es un número no coincide con ninguna ruta y responde 404 (RF-14)
@RestController
@RequestMapping("/api/pedidos/{idPedido:\\d{1,18}}/cobro")
@RequiredArgsConstructor
public class PagoPedidoController {

	private final PagoService pagoService;

	// Genera el cobro, o devuelve el que ya tiene (RF-1, RF-2)
	@PostMapping
	public PedidoResponse pagar(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idPedido) {
		return pagoService.pagar(idUsuario, idPedido);
	}

	// Pregunta al sistema de pago y devuelve cómo quedó el pedido (RF-4)
	@PostMapping("/consulta")
	public PedidoResponse consultar(@AuthenticationPrincipal Long idUsuario, @PathVariable Long idPedido) {
		return pagoService.consultar(idUsuario, idPedido);
	}
}
