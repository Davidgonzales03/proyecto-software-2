package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.entity.MotivoAjuste;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Un ajuste de stock: la cantidad contada (HU-08 RF-4, RF-5, RF-7). La nota obligatoria con OTRO y los decimales
// según la unidad los valida InventarioService
public record AjusteRequest(

		@NotNull(message = "es obligatoria")
		@DecimalMin(value = "0", message = "no puede ser negativa")
		@DecimalMax(value = "9999999.99", message = "admite hasta 9 999 999,99")
		@Digits(integer = 7, fraction = 2, message = "admite hasta dos decimales")
		BigDecimal contada,

		@NotNull(message = "es obligatorio")
		MotivoAjuste motivo,

		@Size(max = 200, message = "admite hasta 200 caracteres")
		String nota) {

	public AjusteRequest {
		nota = nota == null ? null : nota.strip();
	}
}
