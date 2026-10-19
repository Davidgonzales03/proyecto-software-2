package bo.edu.uagrm.tienda.exception;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.HttpStatus;

// Cambió un precio entre que el cliente vio la confirmación y la confirmó (HU-12 RF-15): lleva el total nuevo
public class TotalCambiadoException extends ErrorNegocioException {

	private final BigDecimal total;

	public TotalCambiadoException(BigDecimal total) {
		super("TOTAL_CAMBIADO", HttpStatus.CONFLICT, "El total del pedido cambió. Revíselo y confirme de nuevo.");
		this.total = total;
	}

	@Override
	public Map<String, Object> propiedades() {
		return Map.of("total", total);
	}
}
