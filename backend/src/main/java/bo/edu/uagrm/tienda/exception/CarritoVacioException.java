package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class CarritoVacioException extends ErrorNegocioException {

	public CarritoVacioException() {
		super("CARRITO_VACIO", HttpStatus.CONFLICT, "El carrito está vacío.");
	}
}
