package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// Un producto sin stock no entra al carrito (HU-11 RF-4). El mensaje no dice cuánto hay (RF-14)
public class ProductoAgotadoException extends ErrorNegocioException {

	public ProductoAgotadoException() {
		super("PRODUCTO_AGOTADO", HttpStatus.CONFLICT, "El producto está agotado.");
	}
}
