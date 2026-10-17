package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

// Agregar un producto al carrito (HU-11 RF-1, RF-3). Los decimales según la unidad los revisa CarritoService
public record LineaCarritoRequest(

		@NotNull(message = "es obligatorio")
		Long idProducto,

		@NotNull(message = "es obligatoria")
		@DecimalMin(value = "0", inclusive = false, message = "tiene que ser mayor que 0")
		@DecimalMax(value = "999", message = "admite hasta 999")
		@Digits(integer = 3, fraction = 2, message = "admite hasta dos decimales")
		BigDecimal cantidad) {
}
