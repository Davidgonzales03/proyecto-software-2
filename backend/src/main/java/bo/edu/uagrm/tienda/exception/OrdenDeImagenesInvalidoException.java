package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class OrdenDeImagenesInvalidoException extends ErrorNegocioException {

	public OrdenDeImagenesInvalidoException() {
		super("ORDEN_DE_IMAGENES_INVALIDO", HttpStatus.BAD_REQUEST, "El orden tiene que incluir cada imagen del producto una sola vez.");
	}
}
