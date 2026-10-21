package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// HU-20 RF-3: token de verificación inexistente, vencido o ya usado
public class TokenVerificacionInvalidoException extends ErrorNegocioException {

	public TokenVerificacionInvalidoException() {
		super("TOKEN_VERIFICACION_INVALIDO", HttpStatus.BAD_REQUEST,
				"El enlace de verificación no es válido, ya venció o ya fue utilizado.");
	}
}
