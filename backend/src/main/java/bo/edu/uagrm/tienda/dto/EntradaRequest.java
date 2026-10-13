package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Una entrada de stock (HU-08 RF-3, RF-5). Los decimales según la unidad los valida InventarioService
public record EntradaRequest(

		@NotNull(message = "es obligatoria")
		@DecimalMin(value = "0", inclusive = false, message = "tiene que ser mayor que 0")
		@DecimalMax(value = "99999.99", message = "admite hasta 99 999,99")
		@Digits(integer = 5, fraction = 2, message = "admite hasta dos decimales")
		BigDecimal cantidad,

		@Size(max = 200, message = "admite hasta 200 caracteres")
		String nota) {

	public EntradaRequest {
		nota = nota == null ? null : nota.strip();
	}
}
