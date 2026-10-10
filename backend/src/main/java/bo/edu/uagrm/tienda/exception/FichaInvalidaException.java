package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

public class FichaInvalidaException extends ErrorNegocioException {

	public FichaInvalidaException() {
		super("FICHA_INVALIDA", HttpStatus.BAD_REQUEST, "La ficha técnica tiene dos características con el mismo nombre.");
	}
}
