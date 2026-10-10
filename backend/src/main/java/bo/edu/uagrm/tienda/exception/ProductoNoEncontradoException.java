package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class ProductoNoEncontradoException extends ErrorNegocioException {

	public ProductoNoEncontradoException() {
		super("PRODUCTO_NO_ENCONTRADO", HttpStatus.NOT_FOUND, "No existe el producto indicado.");
	}
}
