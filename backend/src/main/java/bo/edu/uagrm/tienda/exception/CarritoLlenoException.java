package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class CarritoLlenoException extends ErrorNegocioException {

	public CarritoLlenoException() {
		super("CARRITO_LLENO", HttpStatus.CONFLICT, "El carrito admite hasta 50 productos distintos.");
	}
}
