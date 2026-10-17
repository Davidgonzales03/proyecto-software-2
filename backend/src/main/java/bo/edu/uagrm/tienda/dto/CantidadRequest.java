package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

// Cambiar la cantidad de una línea del carrito (HU-11 RF-10), con las mismas reglas que al agregar
public record CantidadRequest(

		@NotNull(message = "es obligatoria")
		@DecimalMin(value = "0", inclusive = false, message = "tiene que ser mayor que 0")
		@DecimalMax(value = "999", message = "admite hasta 999")
		@Digits(integer = 3, fraction = 2, message = "admite hasta dos decimales")
		BigDecimal cantidad) {
}
