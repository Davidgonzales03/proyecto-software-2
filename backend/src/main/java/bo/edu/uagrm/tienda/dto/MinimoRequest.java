package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

// El stock mínimo de un producto (HU-08 RF-9); null lo quita
public record MinimoRequest(

		@DecimalMin(value = "0", message = "no puede ser negativo")
		@DecimalMax(value = "9999999.99", message = "admite hasta 9 999 999,99")
		@Digits(integer = 7, fraction = 2, message = "admite hasta dos decimales")
		BigDecimal minimo) {
}
