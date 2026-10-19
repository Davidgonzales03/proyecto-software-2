package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// También para el pedido de otro cliente: no se distingue de uno que no existe (HU-12 RF-13)
public class PedidoNoEncontradoException extends ErrorNegocioException {

	public PedidoNoEncontradoException() {
		super("PEDIDO_NO_ENCONTRADO", HttpStatus.NOT_FOUND, "No existe el pedido indicado.");
	}
}
