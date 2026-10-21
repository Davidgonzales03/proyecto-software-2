package bo.edu.uagrm.tienda.exception;

import org.springframework.http.HttpStatus;

// El sistema de pago no pudo generar el cobro (HU-13 RF-3): no se guardó nada y el pedido sigue esperando el pago
public class CobroNoDisponibleException extends ErrorNegocioException {

	public CobroNoDisponibleException() {
		super("COBRO_NO_DISPONIBLE", HttpStatus.SERVICE_UNAVAILABLE, "No pudimos generar el cobro. Intente de nuevo.");
	}
}
