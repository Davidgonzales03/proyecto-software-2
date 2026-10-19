package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class PedidoNoPendienteException extends ErrorNegocioException {

	public PedidoNoPendienteException() {
		super("PEDIDO_NO_PENDIENTE", HttpStatus.CONFLICT, "El pedido ya no espera el pago.");
	}
}
