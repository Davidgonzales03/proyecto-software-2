package bo.edu.uagrm.tienda.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

// Un solo pedido esperando el pago por cliente (HU-12 RF-5): la pantalla enlaza al que ya tiene
public class PedidoPendienteException extends ErrorNegocioException {

	private final Long idPedido;

	public PedidoPendienteException(Long idPedido) {
		super("PEDIDO_PENDIENTE", HttpStatus.CONFLICT, "Ya tiene un pedido esperando el pago.");
		this.idPedido = idPedido;
	}

	@Override
	public Map<String, Object> propiedades() {
		return Map.of("idPedido", idPedido);
	}
}
