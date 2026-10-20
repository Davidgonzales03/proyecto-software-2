package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// El pedido no tiene un cobro pendiente que simular (HU-13 RF-12)
public class CobroNoEncontradoException extends ErrorNegocioException {

	public CobroNoEncontradoException() {
		super("COBRO_NO_ENCONTRADO", HttpStatus.NOT_FOUND, "El pedido no tiene un cobro pendiente.");
	}
}
