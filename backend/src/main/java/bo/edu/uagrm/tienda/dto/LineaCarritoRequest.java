package bo.edu.uagrm.tienda.dto;

import java.math.BigDecimal;

import bo.edu.uagrm.tienda.dto.validacion.CantidadDeCarrito;
import jakarta.validation.constraints.NotNull;

// Agregar un producto al carrito (HU-11 RF-1, RF-3). Los decimales según la unidad los revisa CarritoService
public record LineaCarritoRequest(

		@NotNull(message = "es obligatorio")
		Long idProducto,

		@CantidadDeCarrito
		BigDecimal cantidad) {
}
