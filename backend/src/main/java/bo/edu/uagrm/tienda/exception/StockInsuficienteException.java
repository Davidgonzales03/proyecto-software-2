package bo.edu.uagrm.tienda.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;

// Las líneas que impiden confirmar, todas y en el orden del carrito (HU-12 RF-3). Dice por qué, nunca cuánto stock hay
public class StockInsuficienteException extends ErrorNegocioException {

	private final List<LineaRechazada> lineas;

	public StockInsuficienteException(List<LineaRechazada> lineas) {
		super("STOCK_INSUFICIENTE", HttpStatus.CONFLICT, "Algunos productos del carrito no se pueden confirmar.");
		this.lineas = List.copyOf(lineas);
	}

	@Override
	public Map<String, Object> propiedades() {
		return Map.of("lineas", lineas);
	}

	public record LineaRechazada(Long idProducto, Motivo motivo) {
	}

	// Los tres primeros, como en el carrito (HU-11 RF-9); NO_ALCANZA: pide más de lo que hay
	public enum Motivo {
		AGOTADO, NO_DISPONIBLE, CANTIDAD_INVALIDA, NO_ALCANZA
	}
}
