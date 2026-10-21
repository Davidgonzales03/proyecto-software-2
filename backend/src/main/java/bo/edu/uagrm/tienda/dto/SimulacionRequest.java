package bo.edu.uagrm.tienda.dto;

import jakarta.validation.constraints.NotNull;

// El resultado que se fuerza en el simulador (HU-13 RF-12): solo un pago o un error. Otro valor no se lee: 400
public record SimulacionRequest(@NotNull(message = "es obligatorio") Resultado resultado) {

	public enum Resultado {
		PAGADO, ERROR
	}
}
