package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class CategoriaConProductosException extends ErrorNegocioException {

	public CategoriaConProductosException() {
		super("CATEGORIA_CON_PRODUCTOS", HttpStatus.CONFLICT, "La categoría tiene productos: en lugar de borrarla, ocúltela.");
	}
}
