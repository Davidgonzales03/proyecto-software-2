package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// HU-20 RF-4: un cliente con correo no verificado no puede confirmar pedidos
public class CorreoNoVerificadoException extends ErrorNegocioException {

	public CorreoNoVerificadoException() {
		super("CORREO_NO_VERIFICADO", HttpStatus.CONFLICT,
				"Debe verificar su correo electrónico antes de poder confirmar un pedido.");
	}
}
